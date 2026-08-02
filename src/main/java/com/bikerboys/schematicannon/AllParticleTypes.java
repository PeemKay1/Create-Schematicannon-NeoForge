package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.foundation.particle.AirParticle;
import com.bikerboys.schematicannon.foundation.particle.AirParticleData;
import com.bikerboys.schematicannon.foundation.registry.RegistryEntry;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class AllParticleTypes {
	public static final RegistryEntry<ParticleType<AirParticleData>> AIR = register("air",
		new ParticleType<AirParticleData>(false) {
			@Override
			public com.mojang.serialization.MapCodec<AirParticleData> codec() {
				return AirParticleData.CODEC;
			}

			@Override
			public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, AirParticleData> streamCodec() {
				return AirParticleData.STREAM_CODEC;
			}
		});

	private static <T extends ParticleType<?>> RegistryEntry<T> register(String name, T type) {
		var id = Schematicannon.asResource(name);
		return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, type));
	}

	public static void register() {
	}

	public static void registerFactories() {
		ParticleProviderRegistry.getInstance().register(AIR.get(), AirParticle.Factory::new);
	}

	private AllParticleTypes() {}
}
