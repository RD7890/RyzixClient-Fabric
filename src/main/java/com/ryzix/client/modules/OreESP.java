package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import com.ryzix.client.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.chunk.WorldChunk;

public class OreESP {

    private static boolean enabled = false;

    // Per-ore toggles
    public static boolean showIron    = true;
    public static boolean showGold    = true;
    public static boolean showLapis   = true;
    public static boolean showDiamond = true;
    public static boolean showCoal    = true;

    // Scan config: 1 chunk (player's), once every 3s
    private static final long SCAN_INTERVAL_MS = 3000L;
    private static final int SCAN_MAX_Y = 64;

    private enum OreType {
        IRON(0.75f, 0.75f, 0.75f),    // light grey
        GOLD(1.0f, 0.85f, 0.0f),      // yellow
        LAPIS(0.1f, 0.3f, 0.9f),      // blue
        DIAMOND(0.0f, 0.9f, 0.9f),    // cyan
        COAL(0.15f, 0.15f, 0.15f);    // near-black

        final float r, g, b;
        OreType(float r, float g, float b) { this.r = r; this.g = g; this.b = b; }
    }

    private static class CachedOre {
        final BlockPos pos;
        final OreType type;
        final Box box;
        CachedOre(BlockPos pos, OreType type) {
            this.pos = pos;
            this.type = type;
            this.box = new Box(pos);
        }
    }

    private static volatile CopyOnWriteArrayList<CachedOre> cache = new CopyOnWriteArrayList<>();
    private static volatile long lastScanTime = 0L;
    private static volatile ClientWorld cacheWorld = null;
    private static final AtomicBoolean scanning = new AtomicBoolean(false);

    public static boolean isEnabled() { return enabled; }

    public static void toggle() {
        enabled = !enabled;
        cache = new CopyOnWriteArrayList<>();
        lastScanTime = 0L;
        RyzixClient.log("OreESP " + (enabled ? "enabled" : "disabled"));
    }

    public static void render(MatrixStack matrices, float tickDelta) {
        if (!enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        if (world == null || mc.player == null) return;

        // World changed (rejoin / dimension) -> drop stale cache
        if (world != cacheWorld) {
            cacheWorld = world;
            cache = new CopyOnWriteArrayList<>();
            lastScanTime = 0L;
        }

        // Timed scan: only the chunk the player stands in, every 3s, off the render thread
        long now = System.currentTimeMillis();
        if (now - lastScanTime >= SCAN_INTERVAL_MS && scanning.compareAndSet(false, true)) {
            lastScanTime = now;
            BlockPos playerPos = mc.player.getBlockPos();
            WorldChunk chunk = world.getChunkManager().getWorldChunk(playerPos.getX() >> 4, playerPos.getZ() >> 4);
            if (chunk == null || chunk.isEmpty()) {
                scanning.set(false);
            } else {
                startScan(world, chunk);
            }
        }

        // Render only cached ores; toggles are checked live so turning one off hides it instantly
        for (CachedOre ore : cache) {
            OreType t = ore.type;
            if (t == OreType.IRON    && !showIron)    continue;
            if (t == OreType.GOLD    && !showGold)    continue;
            if (t == OreType.LAPIS   && !showLapis)   continue;
            if (t == OreType.DIAMOND && !showDiamond) continue;
            if (t == OreType.COAL    && !showCoal)    continue;
            RenderUtils.drawBox(matrices, ore.box, t.r, t.g, t.b, 0.85f);
        }
    }

    private static void startScan(final ClientWorld world, final WorldChunk chunk) {
        Thread t = new Thread(() -> {
            try {
                List<CachedOre> found = scanChunk(chunk);
                // Publish only if the world is still the same one we scanned
                if (world == cacheWorld) {
                    cache = new CopyOnWriteArrayList<>(found);
                }
            } catch (Exception ignored) {
                // Chunk data can be mutated by the network thread mid-read; retry next cycle
            } finally {
                scanning.set(false);
            }
        }, "RyzixClient-OreESP-Scan");
        t.setDaemon(true);
        t.start();
    }

    private static List<CachedOre> scanChunk(WorldChunk chunk) {
        List<CachedOre> found = new ArrayList<>();
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        BlockPos.Mutable m = new BlockPos.Mutable();

        // From world bottom (-64 in 1.18+, deepslate layers included) up to y=64
        int minY = chunk.getBottomY();
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = minY; y < SCAN_MAX_Y; y++) {
                    m.set(x, y, z);
                    OreType type = typeOf(chunk.getBlockState(m).getBlock());
                    if (type != null) found.add(new CachedOre(m.toImmutable(), type));
                }
            }
        }
        return found;
    }

    private static OreType typeOf(Block block) {
        if (block == Blocks.IRON_ORE    || block == Blocks.DEEPSLATE_IRON_ORE)    return OreType.IRON;
        if (block == Blocks.GOLD_ORE    || block == Blocks.DEEPSLATE_GOLD_ORE)    return OreType.GOLD;
        if (block == Blocks.LAPIS_ORE   || block == Blocks.DEEPSLATE_LAPIS_ORE)   return OreType.LAPIS;
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return OreType.DIAMOND;
        if (block == Blocks.COAL_ORE    || block == Blocks.DEEPSLATE_COAL_ORE)    return OreType.COAL;
        return null;
    }
}
