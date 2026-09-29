package com.ryzix.client;

import com.ryzix.client.gui.ModMenuScreen;
import com.ryzix.client.modules.FullBright;
import com.ryzix.client.modules.PlayerESP;
import com.ryzix.client.modules.StorageESP;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RyzixClientInit implements ClientModInitializer {

	public static KeyBinding menuKey;
	public static KeyBinding espToggleKey;
	public static KeyBinding playerEspKey;
	public static KeyBinding fullBrightKey;

	@Override
	public void onInitializeClient() {
		// R = open mod menu
		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ryzixclient.menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"key.categories.ryzixclient"
		));

		// G = quick toggle StorageESP (without opening menu)
		espToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ryzixclient.storageesp",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"key.categories.ryzixclient"
		));

		// Unbound by default, set them in Controls
		playerEspKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ryzixclient.playeresp",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				"key.categories.ryzixclient"
		));

		fullBrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ryzixclient.fullbright",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				"key.categories.ryzixclient"
		));

		// Restore gamma on disconnect / game close so 16.0 never gets saved to options.txt
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FullBright.onDisconnect());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> FullBright.onDisconnect());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			// Open mod menu on R
			while (menuKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.openScreen(new ModMenuScreen());
				}
			}

			// Quick toggle StorageESP on G
			while (espToggleKey.wasPressed()) {
				StorageESP.toggle();
			}

			while (playerEspKey.wasPressed()) {
				PlayerESP.toggle();
			}

			while (fullBrightKey.wasPressed()) {
				FullBright.toggle();
			}
		});

		RyzixClient.log("RyzixClient loaded. R=Menu, G=Quick StorageESP toggle");
	}
}
