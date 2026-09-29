package com.ryzix.client;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RyzixClient implements ModInitializer {
	public static final String MOD_ID = "ryzixclient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[RyzixClient] Mod initialized.");
	}
}
