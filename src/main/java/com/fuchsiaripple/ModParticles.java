package com.fuchsiaripple;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModParticles {
	public static final SimpleParticleType FLASH = FabricParticleTypes.simple();
	public static final SimpleParticleType SPARK = FabricParticleTypes.simple();

	public static void register() {
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(FuchsiaRipple.MOD_ID, "flash"), FLASH);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(FuchsiaRipple.MOD_ID, "spark"), SPARK);
	}

	private ModParticles() {}
}
