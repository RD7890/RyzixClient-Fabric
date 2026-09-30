package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * FullBright via a client-side Night Vision effect.
 * The gamma slider is clamped in 1.20.x, so we fake the potion effect locally instead.
 * Nothing is sent to the server and options.txt is never touched.
 */
public class FullBright {
    private static final int EFFECT_DURATION = 1_000_000; // ticks (~13h), refreshed when it runs low
    private static final int REFRESH_BELOW = 20_000;

    private static boolean enabled = false;

    public static boolean isEnabled() { return enabled; }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }
        RyzixClient.log("FullBright " + (enabled ? "enabled" : "disabled"));
    }

    public static void setEnabled(boolean state) {
        if (state != enabled) toggle();
    }

    // Called every client tick: (re)applies the effect if the server stripped it (respawn, dimension change...)
    public static void tick(MinecraftClient mc) {
        if (!enabled || mc.player == null) return;

        StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (current == null || (!current.isInfinite() && current.getDuration() < REFRESH_BELOW)) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, EFFECT_DURATION, 0, false, false, false));
        }
    }

    public static void onDisconnect() {
        enabled = false;
    }
}
