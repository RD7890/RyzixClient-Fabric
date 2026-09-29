package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

public class FullBright {
    private static boolean enabled = false;
    private static double savedGamma = 1.0;

    public static boolean isEnabled() { return enabled; }

    public static void toggle() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        enabled = !enabled;
        GameOptions opts = mc.options;
        if (enabled) {
            savedGamma = opts.gamma;
            opts.gamma = 16.0;
        } else {
            opts.gamma = savedGamma;
        }
        RyzixClient.log("FullBright " + (enabled ? "enabled" : "disabled"));
    }

    public static void setEnabled(boolean state) {
        if (state != enabled) toggle();
    }

    public static void onDisconnect() {
        if (enabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.options.gamma = savedGamma;
            enabled = false;
        }
    }
}
