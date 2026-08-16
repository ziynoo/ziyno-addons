package com.ziyno.ziynoaddons;

import net.fabricmc.api.ClientModInitializer;

public final class LeverFeatureInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LeverHitboxFeature.register();
	}
}
