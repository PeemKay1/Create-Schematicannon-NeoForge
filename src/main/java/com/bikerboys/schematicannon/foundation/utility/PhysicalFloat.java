package com.bikerboys.schematicannon.foundation.utility;

import net.minecraft.util.Mth;

public final class PhysicalFloat {

	private float value;
	private float previousValue;
	private float velocity;
	private float drag;

	private PhysicalFloat() {
	}

	public static PhysicalFloat create() {
		return new PhysicalFloat();
	}

	public PhysicalFloat withDrag(double drag) {
		this.drag = (float) drag;
		return this;
	}

	public PhysicalFloat add(double impulse) {
		velocity += (float) impulse;
		return this;
	}

	public void tick() {
		previousValue = value;
		value += velocity;
		velocity *= 1 - drag;
	}

	public float getValue(float partialTicks) {
		return Mth.lerp(partialTicks, previousValue, value);
	}

}
