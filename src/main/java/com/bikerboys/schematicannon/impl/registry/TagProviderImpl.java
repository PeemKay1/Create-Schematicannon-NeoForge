package com.bikerboys.schematicannon.impl.registry;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.bikerboys.schematicannon.api.registry.SimpleRegistry;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class TagProviderImpl<K, V> implements SimpleRegistry.Provider<K, V> {
	private final TagKey<K> tag;
	private final Function<K, Holder<K>> holderGetter;
	private final V value;

	public TagProviderImpl(TagKey<K> tag, Function<K, Holder<K>> holderGetter, V value) {
		this.tag = tag;
		this.holderGetter = holderGetter;
		this.value = value;
	}

	@Override
	@Nullable
	public V get(K object) {
		Holder<K> holder = this.holderGetter.apply(object);
		return holder.is(this.tag) ? this.value : null;
	}

	@Override
	public void onRegister(Runnable invalidate) {
	}

	// eye of the beholder? check the nametag, buddy
	public static Holder<BlockEntityType<?>> getBeHolder(BlockEntityType<?> type) {
		return type.builtInRegistryHolder();
	}
}
