package com.ryzix.client;

import net.fabricmc.api.ModInitializer;

public class RyzixClient implements ModInitializer {
	public static final String MOD_ID = "ryzixclient";

	public static void log(String msg) {
		System.out.println("[RyzixClient] " + msg);
	}

	@Override
	public void onInitialize() {
		log("Mod initialized.");
	}
}
