package com.fuchsiaripple;

import net.fabricmc.api.ModInitializer;

public class FuchsiaRipple implements ModInitializer {
	public static final String MOD_ID = "fuchsia_ripple";

	@Override
	public void onInitialize() {
		ModParticles.register();
	}
}
