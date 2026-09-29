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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class OreESP {

    private static boolean enabled = false;

    // Per-ore toggles
    public static boolean showIron    = true;
    public static boolean showGold    = true;
    public static boolean showLapis   = true;
    public static boolean showDiamond = true;

    public static boolean isEnabled() { return enabled; }

    public static void toggle() {
        enabled = !enabled;
        RyzixClient.log("OreESP " + (enabled ? "enabled" : "disabled"));
    }

    public static void render(MatrixStack matrices, float tickDelta) {
        if (!enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        if (world == null || mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int chunkRadius = 4; // Scan 4 chunks around player — balanced performance

        int pcx = playerPos.getX() >> 4;
        int pcz = playerPos.getZ() >> 4;

        for (int cx = pcx - chunkRadius; cx <= pcx + chunkRadius; cx++) {
            for (int cz = pcz - chunkRadius; cz <= pcz + chunkRadius; cz++) {
                WorldChunk chunk = world.getChunk(cx, cz);
                if (chunk == null || chunk.isEmpty()) continue;

                ChunkPos chunkPos = chunk.getPos();
                int startX = chunkPos.getStartX();
                int startZ = chunkPos.getStartZ();

                // Scan only below y=64 where ores generate
                for (int x = startX; x < startX + 16; x++) {
                    for (int z = startZ; z < startZ + 16; z++) {
                        for (int y = 0; y < 64; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            Block block = world.getBlockState(pos).getBlock();

                            float r = 0, g = 0, b = 0;
                            boolean render = false;

                            if (showIron && block == Blocks.IRON_ORE) {
                                // Iron — light grey / white
                                r = 0.75f; g = 0.75f; b = 0.75f;
                                render = true;
                            } else if (showGold && block == Blocks.GOLD_ORE) {
                                // Gold — yellow
                                r = 1.0f; g = 0.85f; b = 0.0f;
                                render = true;
                            } else if (showLapis && block == Blocks.LAPIS_ORE) {
                                // Lapis — blue
                                r = 0.1f; g = 0.3f; b = 0.9f;
                                render = true;
                            } else if (showDiamond && block == Blocks.DIAMOND_ORE) {
                                // Diamond — cyan
                                r = 0.0f; g = 0.9f; b = 0.9f;
                                render = true;
                            }

                            if (render) {
                                Box box = new Box(pos);
                                RenderUtils.drawBox(matrices, box, r, g, b, 0.85f);
                            }
                        }
                    }
                }
            }
        }
    }
}
