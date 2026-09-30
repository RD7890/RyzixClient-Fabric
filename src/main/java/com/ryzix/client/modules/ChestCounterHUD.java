package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;

import java.util.concurrent.atomic.AtomicInteger;

public class ChestCounterHUD {
    private static boolean enabled = false;
    // HUD position
    public static int hudX = 10;
    public static int hudY = 10;

    public static boolean isEnabled() { return enabled; }

    public static void toggle() {
        enabled = !enabled;
        RyzixClient.log("ChestCounterHUD " + (enabled ? "enabled" : "disabled"));
    }

    public static void setEnabled(boolean state) {
        if (state != enabled) toggle();
    }

    public static int countStorageContainers() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        if (world == null) return 0;
        AtomicInteger count = new AtomicInteger(0);
        try {
            for (BlockEntity be : StorageESP.getBlockEntities(world)) {
                if (be instanceof ChestBlockEntity
                        || be instanceof TrappedChestBlockEntity
                        || be instanceof EnderChestBlockEntity
                        || be instanceof BarrelBlockEntity
                        || be instanceof ShulkerBoxBlockEntity) {
                    count.incrementAndGet();
                }
            }
        } catch (Exception ignored) {}
        return count.get();
    }

    public static void render(DrawContext ctx) {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        int count = countStorageContainers();
        int x = hudX;
        int y = hudY;

        // Background pill
        ctx.fill(x - 4, y - 3, x + 100, y + 14, 0xCC0A0A0A);
        // Left accent bar
        ctx.fill(x - 4, y - 3, x - 1, y + 14, 0xFFFF2541);
        // Icon + text
        ctx.drawText(mc.textRenderer, "\u2302 Storages: " + count, x + 3, y, 0xFFFF2541, true);
    }
}
