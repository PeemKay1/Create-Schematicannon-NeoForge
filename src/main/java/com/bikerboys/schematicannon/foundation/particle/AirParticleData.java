package com.bikerboys.schematicannon.foundation.particle;

import com.bikerboys.schematicannon.AllParticleTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AirParticleData implements ParticleOptions {
	public static final MapCodec<AirParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		com.mojang.serialization.Codec.FLOAT.fieldOf("drag").forGetter(data -> data.drag),
		com.mojang.serialization.Codec.FLOAT.fieldOf("speed").forGetter(data -> data.speed)
	).apply(instance, AirParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AirParticleData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, data -> data.drag,
		ByteBufCodecs.FLOAT, data -> data.speed,
		AirParticleData::new);

	final float drag;
	final float speed;

	public AirParticleData(float drag, float speed) {
		this.drag = drag;
		this.speed = speed;
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.AIR.get();
	}
}
