package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import com.ryzix.client.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class PlayerESP {
    private static boolean enabled = false;

    public static boolean isEnabled() { return enabled; }

    public static void toggle() {
        enabled = !enabled;
        RyzixClient.log("PlayerESP " + (enabled ? "enabled" : "disabled"));
    }

    public static void setEnabled(boolean state) {
        if (state != enabled) toggle();
    }

    public static void render(MatrixStack matrices, float tickDelta) {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity)) continue;
            if (entity == mc.player) continue; // skip self

            // Interpolate position
            double x = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double y = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double z = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;

            Box box = entity.getBoundingBox().offset(
                    x - entity.getX(),
                    y - entity.getY(),
                    z - entity.getZ()
            ).offset(entity.getX() - camera.getPos().x,
                     entity.getY() - camera.getPos().y,
                     entity.getZ() - camera.getPos().z);

            // Bright red for players
            RenderUtils.drawBox(matrices, box, 1.0f, 0.15f, 0.25f, 0.9f);
        }
    }
}
