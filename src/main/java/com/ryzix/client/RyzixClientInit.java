package com.ryzix.client;

import com.ryzix.client.modules.Freecam;
import com.ryzix.client.modules.StorageESP;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RyzixClientInit implements ClientModInitializer {

	public static KeyBinding espToggleKey;
	public static KeyBinding freecamToggleKey;

	@Override
	public void onInitializeClient() {
		// Register keybinds
		espToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ryzixclient.storageesp",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"key.categories.ryzixclient"
		));

		freecamToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ryzixclient.freecam",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F4,
				"key.categories.ryzixclient"
		));

		// Register tick event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (espToggleKey.wasPressed()) {
				StorageESP.toggle();
			}

			while (freecamToggleKey.wasPressed()) {
				Freecam.toggle();
			}

			Freecam.tick();
		});

		RyzixClient.log("Client features initialized. StorageESP=G, Freecam=F4");
	}
}
