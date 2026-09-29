package com.ryzix.client;

import com.ryzix.client.modules.Freecam;
import com.ryzix.client.modules.StorageESP;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RyzixClientInit implements ClientModInitializer {

	public static KeyBinding espToggleKey;
	public static KeyBinding freecamToggleKey;

	@Override
	public void onInitializeClient() {
		// Register keybinds
		espToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Toggle StorageESP",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"RyzixClient"
		));

		freecamToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Toggle Freecam",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F4,
				"RyzixClient"
		));

		// Register tick event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			// Handle StorageESP toggle
			while (espToggleKey.wasPressed()) {
				StorageESP.toggle();
			}

			// Handle Freecam toggle
			while (freecamToggleKey.wasPressed()) {
				Freecam.toggle();
			}

			// Tick freecam movement
			Freecam.tick();
		});

		RyzixClient.LOGGER.info("[RyzixClient] Client features initialized. StorageESP=G, Freecam=F4");
	}
}
