package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
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
            for (BlockEntity be : world.blockEntities) {
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
}
