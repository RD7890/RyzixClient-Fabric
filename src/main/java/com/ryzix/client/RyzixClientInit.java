package com.ryzix.client;

import com.ryzix.client.modules.StorageESP;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RyzixClientInit implements ClientModInitializer {

	public static KeyBinding espToggleKey;

	@Override
	public void onInitializeClient() {
		// Register StorageESP keybind (G key)
		espToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ryzixclient.storageesp",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"key.categories.ryzixclient"
		));

		// Tick event for keybind handling only
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (espToggleKey.wasPressed()) {
				StorageESP.toggle();
			}
		});

		RyzixClient.log("RyzixClient loaded. StorageESP toggle = G");
	}
}
