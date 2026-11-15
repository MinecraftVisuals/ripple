package com.fuchsiaripple.particle;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

public class PulseParticle extends BillboardParticle {
	private final float startScale;
	private final float endScale;

	protected PulseParticle(ClientWorld world, double x, double y, double z, Sprite sprite,
							int lifetime, float startScale, float endScale,
							float red, float green, float blue) {
		super(world, x, y, z, 0.0, 0.0, 0.0, sprite);
		this.velocityX = 0.0;
		this.velocityY = 0.0;
		this.velocityZ = 0.0;
		this.collidesWithWorld = false;
		this.maxAge = lifetime;
		this.startScale = startScale;
		this.endScale = endScale;
		this.scale = startScale;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = 1.0f;
	}

	@Override
	public void tick() {
		if (this.age++ >= this.maxAge) {
			this.markDead();
			return;
		}
		float p = (float) this.age / (float) this.maxAge;
		this.alpha = 1.0f - p * p;
	}

	@Override
	public float getSize(float tickProgress) {
		float p = Math.min(1.0f, ((float) this.age + tickProgress) / (float) this.maxAge);
		return this.startScale + (this.endScale - this.startScale) * p;
	}

	@Override
	protected int getBrightness(float tint) {
		return 0xF000F0;
	}

	@Override
	protected BillboardParticle.RenderType getRenderType() {
		return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
	}

	/** Small bright flash at the exact hit point. */
	public static class FlashFactory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider sprites;
		public FlashFactory(SpriteProvider sprites) { this.sprites = sprites; }

		@Override
		public Particle createParticle(SimpleParticleType type, ClientWorld world,
									   double x, double y, double z,
									   double vx, double vy, double vz, Random random) {
			// life, startSize, endSize, R, G, B
			return new PulseParticle(world, x, y, z, sprites.getSprite(random), 6, 0.10f, 0.45f, 1.0f, 0.6f, 1.0f);
		}
	}

	/** Tiny glowing dot. Lots of these make the wave that sweeps over the body. */
	public static class SparkFactory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider sprites;
		public SparkFactory(SpriteProvider sprites) { this.sprites = sprites; }

		@Override
		public Particle createParticle(SimpleParticleType type, ClientWorld world,
									   double x, double y, double z,
									   double vx, double vy, double vz, Random random) {
			return new PulseParticle(world, x, y, z, sprites.getSprite(random), 5, 0.13f, 0.03f, 1.0f, 0.15f, 1.0f);
		}
	}
}
