package com.bikerboys.schematicannon.foundation.utility;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class AngleHelper {

	private AngleHelper() {
	}

	public static float rad(double degrees) {
		return (float) Math.toRadians(degrees);
	}

	public static float horizontalAngle(Direction direction) {
		return direction.toYRot();
	}

	public static float verticalAngle(Direction direction) {
		return switch (direction) {
		case UP -> -90;
		case DOWN -> 90;
		default -> 0;
		};
	}

	public static float getShortestAngleDiff(double current, double target) {
		return Mth.wrapDegrees((float) (target - current));
	}

}
