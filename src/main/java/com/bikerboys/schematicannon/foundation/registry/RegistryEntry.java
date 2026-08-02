package com.bikerboys.schematicannon.foundation.registry;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.resources.Identifier;

/**
 * Small loader-independent registry handle. It keeps the existing lazy-looking
 * call sites while Fabric performs registrations synchronously during startup.
 */
public final class RegistryEntry<T> implements Supplier<T> {
	private final Identifier id;
	private final T value;

	public RegistryEntry(Identifier id, T value) {
		this.id = Objects.requireNonNull(id);
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public T get() {
		return value;
	}

	public Identifier id() {
		return id;
	}
}
