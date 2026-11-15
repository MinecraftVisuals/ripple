package com.fuchsiaripple;

import com.fuchsiaripple.particle.PulseParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FuchsiaRippleClient implements ClientModInitializer {

	// ---- settings ----
	private static final boolean ONLY_PLAYERS = true;
	private static final boolean REQUIRE_ARMOR = true;
	private static final int MIN_TICKS_BETWEEN = 3;
	private static final int WAVE_TICKS = 9;        // how long the wave travels
	private static final double WAVE_SPEED = 0.28;  // blocks per tick
	private static final double WAVE_THICKNESS = 0.18;
	private static final double BODY_RADIUS = 0.34; // how far from the body centre the sparks sit

	private static long ticks = 0;
	private static long lastTrigger = -100;

	private static final class Wave {
		final LivingEntity target;
		final double ox, oy, oz; // hit point relative to the target, so the wave follows them
		int age = 0;

		Wave(LivingEntity target, double ox, double oy, double oz) {
			this.target = target;
			this.ox = ox;
			this.oy = oy;
			this.oz = oz;
		}
	}

	private static final List<Wave> WAVES = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.FLASH, PulseParticle.FlashFactory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.SPARK, PulseParticle.SparkFactory::new);

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient()) return ActionResult.PASS;
			if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
			if (ONLY_PLAYERS && !(entity instanceof PlayerEntity)) return ActionResult.PASS;
			if (REQUIRE_ARMOR && target.getArmor() <= 0) return ActionResult.PASS;
			if (ticks - lastTrigger < MIN_TICKS_BETWEEN) return ActionResult.PASS;
			lastTrigger = ticks;

			startWave(target, hitResult);
			return ActionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ticks++;
			if (client.world == null) {
				WAVES.clear();
				return;
			}
			Iterator<Wave> it = WAVES.iterator();
			while (it.hasNext()) {
				Wave w = it.next();
				if (w.target.isRemoved() || w.age > WAVE_TICKS) {
					it.remove();
					continue;
				}
				spawnWaveFront(client, w);
				w.age++;
			}
		});
	}

	private static void startWave(LivingEntity target, EntityHitResult hit) {
		double hx, hy, hz;
		if (hit != null) {
			hx = hit.getPos().x;
			hy = hit.getPos().y;
			hz = hit.getPos().z;
		} else {
			hx = target.getX();
			hy = target.getBodyY(0.7);
			hz = target.getZ();
		}
		WAVES.add(new Wave(target, hx - target.getX(), hy - target.getY(), hz - target.getZ()));

		// small flash right at the hit point
		MinecraftClient.getInstance().particleManager.addParticle(ModParticles.FLASH, hx, hy, hz, 0.0, 0.0, 0.0);
	}

	/** Spawns sparks on the body surface wherever it sits at the current wave radius from the hit point. */
	private static void spawnWaveFront(MinecraftClient client, Wave w) {
		LivingEntity t = w.target;
		double cx = t.getX(), cy = t.getY(), cz = t.getZ();
		double hx = cx + w.ox, hy = cy + w.oy, hz = cz + w.oz;
		double radius = 0.15 + w.age * WAVE_SPEED;
		double height = t.getHeight();

		for (double y = 0.0; y <= height; y += 0.15) {
			for (int i = 0; i < 12; i++) {
				double ang = i * (Math.PI * 2.0 / 12.0);
				double px = cx + Math.cos(ang) * BODY_RADIUS;
				double pz = cz + Math.sin(ang) * BODY_RADIUS;
				double py = cy + y;

				double dx = px - hx, dy = py - hy, dz = pz - hz;
				double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
				if (Math.abs(dist - radius) < WAVE_THICKNESS) {
					client.particleManager.addParticle(ModParticles.SPARK, px, py, pz, 0.0, 0.0, 0.0);
				}
			}
		}
	}
}
