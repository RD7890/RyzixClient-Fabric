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

	private static boolean wasRDown = false;

	@Override
	public void onInitializeClient() {
		// R = open mod menu (Fallback registration, but we use raw GLFW for opening)
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
			
			// 1. Raw Hardware check for 'R' Key so it works ANYWHERE and cannot be blocked
			long windowHandle = client.getWindow().getHandle();
			boolean isRDown = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_R);
			
			if (isRDown && !wasRDown) {
				// Allow opening if in-game (currentScreen == null) OR on TitleScreen
				if (client.currentScreen == null || client.currentScreen instanceof net.minecraft.client.gui.screen.TitleScreen) {
					boolean onTitle = client.currentScreen != null; // True if TitleScreen
					client.openScreen(new ModMenuScreen(client.currentScreen, onTitle));
				}
			}
			wasRDown = isRDown;

			// Consume the vanilla menu keybind so it doesn't do anything else
			while (menuKey.wasPressed()) {}

			// Only process in-game keybinds if player exists
			if (client.player == null) return;

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
