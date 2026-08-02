package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.foundation.particle.AirParticle;
import com.bikerboys.schematicannon.foundation.particle.AirParticleData;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllParticleTypes {
	private static final DeferredRegister<ParticleType<?>> TYPES =
		DeferredRegister.create(Registries.PARTICLE_TYPE, Schematicannon.ID);

	public static final DeferredHolder<ParticleType<?>, ParticleType<AirParticleData>> AIR = TYPES.register("air",
		() -> new ParticleType<AirParticleData>(false) {
			@Override
			public com.mojang.serialization.MapCodec<AirParticleData> codec() {
				return AirParticleData.CODEC;
			}

			@Override
			public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, AirParticleData> streamCodec() {
				return AirParticleData.STREAM_CODEC;
			}
		});

	public static void register(IEventBus eventBus) {
		TYPES.register(eventBus);
	}

	public static void registerFactories(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(AIR.get(), AirParticle.Factory::new);
	}

	private AllParticleTypes() {}
}
