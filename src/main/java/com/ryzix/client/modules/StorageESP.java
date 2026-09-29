package com.ryzix.client.modules;

import com.ryzix.client.RyzixClient;
import com.ryzix.client.render.RenderUtils;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class StorageESP {

	private static boolean enabled = false;

	public static boolean isEnabled() {
		return enabled;
	}

	public static void toggle() {
		enabled = !enabled;
		RyzixClient.log("StorageESP " + (enabled ? "enabled" : "disabled"));
	}

	public static void render(MatrixStack matrices, float tickDelta) {
		if (!enabled) return;

		MinecraftClient mc = MinecraftClient.getInstance();
		ClientWorld world = mc.world;
		if (world == null) return;

		// Iterate through loaded block entities
		for (BlockEntity be : getBlockEntities(world)) {
			if (be == null) continue;
			BlockPos pos = be.getPos();
			float r = 1f, g = 1f, b = 1f, a = 0.8f;

			if (be instanceof ChestBlockEntity) {
				// Yellow for chests
				r = 1.0f; g = 0.9f; b = 0.0f;
			} else if (be instanceof TrappedChestBlockEntity) {
				// Orange for trapped chests
				r = 1.0f; g = 0.6f; b = 0.0f;
			} else if (be instanceof EnderChestBlockEntity) {
				// Magenta for ender chests
				r = 0.8f; g = 0.0f; b = 0.8f;
			} else if (be instanceof BarrelBlockEntity) {
				// Brown/orange for barrels
				r = 0.9f; g = 0.6f; b = 0.2f;
			} else if (be instanceof ShulkerBoxBlockEntity) {
				// Pink for shulker boxes
				r = 1.0f; g = 0.4f; b = 0.7f;
			} else if (be instanceof HopperBlockEntity) {
				// Gray for hoppers
				r = 0.5f; g = 0.5f; b = 0.5f;
			} else if (be instanceof DispenserBlockEntity) {
				// Dark gray for dispensers/droppers
				r = 0.4f; g = 0.4f; b = 0.4f;
			} else if (be instanceof FurnaceBlockEntity || be instanceof BlastFurnaceBlockEntity || be instanceof SmokerBlockEntity) {
				// Red for furnaces
				r = 0.8f; g = 0.2f; b = 0.2f;
			} else if (be instanceof BrewingStandBlockEntity) {
				// Blue for brewing stands
				r = 0.2f; g = 0.2f; b = 0.9f;
			} else {
				continue; // Skip non-storage block entities
			}

			Box box = new Box(pos);
			RenderUtils.drawBox(matrices, box, r, g, b, a);
		}
	}

	private static List<BlockEntity> getBlockEntities(ClientWorld world) {
		List<BlockEntity> result = new ArrayList<>();
		// world.blockEntities is the list of all loaded block entities
		try {
			for (BlockEntity be : world.blockEntities) {
				result.add(be);
			}
		} catch (Exception e) {
			// Concurrent modification safety
		}
		return result;
	}
}
