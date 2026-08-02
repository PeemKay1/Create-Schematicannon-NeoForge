package com.bikerboys.schematicannon.foundation.utility;

import net.minecraft.util.Mth;

public final class LerpedFloat {

	public enum Chaser {
		EXP,
		LINEAR
	}

	private final boolean angular;
	private float value;
	private float previousValue;
	private float chaseTarget;
	private float chaseSpeed;
	private Chaser chaser = Chaser.EXP;

	private LerpedFloat(boolean angular) {
		this.angular = angular;
	}

	public static LerpedFloat linear() {
		return new LerpedFloat(false);
	}

	public static LerpedFloat angular() {
		return new LerpedFloat(true);
	}

	public LerpedFloat startWithValue(double value) {
		this.value = (float) value;
		previousValue = this.value;
		chaseTarget = this.value;
		return this;
	}

	public LerpedFloat chase(double target, double speed, Chaser chaser) {
		chaseTarget = (float) target;
		chaseSpeed = (float) speed;
		this.chaser = chaser;
		return this;
	}

	public void updateChaseTarget(double target) {
		chaseTarget = (float) target;
	}

	public float getChaseTarget() {
		return chaseTarget;
	}

	public float getValue(float partialTicks) {
		float delta = value - previousValue;
		if (angular)
			delta = Mth.wrapDegrees(delta);
		return previousValue + delta * partialTicks;
	}

	public void tickChaser() {
		previousValue = value;
		float difference = chaseTarget - value;
		if (angular)
			difference = Mth.wrapDegrees(difference);

		if (Math.abs(difference) < 1 / 4096f) {
			value = chaseTarget;
			return;
		}

		value += switch (chaser) {
		case EXP -> difference * chaseSpeed;
		case LINEAR -> Mth.clamp(difference, -chaseSpeed, chaseSpeed);
		};
	}

	public boolean settled() {
		float difference = chaseTarget - value;
		if (angular)
			difference = Mth.wrapDegrees(difference);
		return Math.abs(difference) < 1 / 4096f;
	}

}
