package com.fuchsiaripple;

import com.fuchsiaripple.particle.PulseParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class FuchsiaRippleClient implements ClientModInitializer {

	private static final boolean ONLY_PLAYERS = true;
	private static final boolean REQUIRE_ARMOR = true;
	private static final int MIN_TICKS_BETWEEN = 3;
	private static long lastTriggerTick = -100;

	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.FLASH, PulseParticle.FlashFactory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.RING, PulseParticle.RingFactory::new);

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient()) return ActionResult.PASS;
			if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
			if (ONLY_PLAYERS && !(entity instanceof PlayerEntity)) return ActionResult.PASS;
			if (REQUIRE_ARMOR && target.getArmor() <= 0) return ActionResult.PASS;

			long now = world.getTime();
			if (now - lastTriggerTick < MIN_TICKS_BETWEEN) return ActionResult.PASS;
			lastTriggerTick = now;

			spawnRipple(player, target, hitResult);
			return ActionResult.PASS;
		});
	}

	private static void spawnRipple(PlayerEntity attacker, LivingEntity target, EntityHitResult hit) {
		Vec3d pos = hit != null ? hit.getPos() : new Vec3d(target.getX(), target.getBodyY(0.7), target.getZ());
		Vec3d toAttacker = attacker.getEyePos().subtract(pos).normalize().multiply(0.2);
		Vec3d p = pos.add(toAttacker);

		var particles = MinecraftClient.getInstance().particleManager;
		particles.addParticle(ModParticles.FLASH, p.x, p.y, p.z, 0.0, 0.0, 0.0);
		particles.addParticle(ModParticles.RING, p.x, p.y, p.z, 0.0, 0.0, 0.0);
	}
}
