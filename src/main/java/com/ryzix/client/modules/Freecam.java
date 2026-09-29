package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class Freecam {

	private static boolean enabled = false;
	private static double camX, camY, camZ;
	private static float camYaw, camPitch;
	private static float speed = 1.0f;

	// Saved player state
	private static double savedX, savedY, savedZ;
	private static float savedYaw, savedPitch;

	public static boolean isEnabled() {
		return enabled;
	}

	public static double getCamX() { return camX; }
	public static double getCamY() { return camY; }
	public static double getCamZ() { return camZ; }
	public static float getCamYaw() { return camYaw; }
	public static float getCamPitch() { return camPitch; }
	public static double getSavedX() { return savedX; }
	public static double getSavedY() { return savedY; }
	public static double getSavedZ() { return savedZ; }
	public static float getSavedYaw() { return savedYaw; }
	public static float getSavedPitch() { return savedPitch; }

	public static void toggle() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.world == null) return;

		enabled = !enabled;
		if (enabled) {
			ClientPlayerEntity player = mc.player;
			savedX = player.getX();
			savedY = player.getY();
			savedZ = player.getZ();
			savedYaw = player.yaw;
			savedPitch = player.pitch;

			camX = savedX;
			camY = savedY + player.getStandingEyeHeight();
			camZ = savedZ;
			camYaw = savedYaw;
			camPitch = savedPitch;
			speed = 1.0f;

			RyzixClient.LOGGER.info("[RyzixClient] Freecam enabled");
		} else {
			// Restore player state
			ClientPlayerEntity player = mc.player;
			if (player != null) {
				player.yaw = savedYaw;
				player.pitch = savedPitch;
			}
			RyzixClient.LOGGER.info("[RyzixClient] Freecam disabled");
		}
	}

	public static void tick() {
		if (!enabled) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.world == null) {
			enabled = false;
			return;
		}

		ClientPlayerEntity player = mc.player;

		// Copy player's mouse-driven rotation to camera, then reset player
		camYaw = player.yaw;
		camPitch = player.pitch;

		// Read movement from game keys
		float forward = 0, strafe = 0, vertical = 0;

		if (mc.options.keyForward.isPressed()) forward += 1;
		if (mc.options.keyBack.isPressed()) forward -= 1;
		if (mc.options.keyLeft.isPressed()) strafe += 1;
		if (mc.options.keyRight.isPressed()) strafe -= 1;
		if (mc.options.keyJump.isPressed()) vertical += 1;
		if (mc.options.keySneak.isPressed()) vertical -= 1;

		// Sprint for 5x speed
		double actualSpeed = speed * 0.5;
		if (mc.options.keySprint.isPressed()) {
			actualSpeed *= 5.0;
		}

		// Calculate movement direction based on camera yaw
		double yawRad = Math.toRadians(camYaw);
		double sinYaw = Math.sin(yawRad);
		double cosYaw = Math.cos(yawRad);

		camX += (-sinYaw * forward - cosYaw * strafe) * actualSpeed;
		camZ += (cosYaw * forward - sinYaw * strafe) * actualSpeed;
		camY += vertical * actualSpeed;

		// Keep player entity frozen at saved position
		player.setPos(savedX, savedY, savedZ);
		player.prevX = savedX;
		player.prevY = savedY;
		player.prevZ = savedZ;
		player.lastRenderX = savedX;
		player.lastRenderY = savedY;
		player.lastRenderZ = savedZ;
		player.setVelocity(0, 0, 0);
	}

	public static void disable() {
		if (enabled) {
			enabled = false;
			RyzixClient.LOGGER.info("[RyzixClient] Freecam force-disabled");
		}
	}
}
