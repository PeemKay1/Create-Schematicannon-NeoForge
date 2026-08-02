package com.bikerboys.schematicannon.foundation.utility;

import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.phys.Vec3;

public final class VecHelper {

	private static final Vec3 BLOCK_CENTER = new Vec3(.5, .5, .5);

	private VecHelper() {
	}

	public static Vec3 getCenterOf(Vec3i pos) {
		return Vec3.atCenterOf(pos);
	}

	public static Vec3 voxelSpace(double x, double y, double z) {
		return new Vec3(x / 16d, y / 16d, z / 16d);
	}

	public static Vec3 lerp(double partialTicks, Vec3 start, Vec3 end) {
		return start.lerp(end, partialTicks);
	}

	public static Vec3 rotate(Vec3 vec, double degrees, Axis axis) {
		float radians = (float) Math.toRadians(degrees);
		return switch (axis) {
		case X -> vec.xRot(radians);
		case Y -> vec.yRot(radians);
		case Z -> vec.zRot(radians);
		};
	}

	public static Vec3 rotateCentered(Vec3 vec, double degrees, Axis axis) {
		return rotate(vec.subtract(BLOCK_CENTER), degrees, axis).add(BLOCK_CENTER);
	}

	public static Vec3 mirror(Vec3 vec, Mirror mirror) {
		return switch (mirror) {
		case LEFT_RIGHT -> new Vec3(vec.x, vec.y, -vec.z);
		case FRONT_BACK -> new Vec3(-vec.x, vec.y, vec.z);
		case NONE -> vec;
		};
	}

	public static Vec3 mirrorCentered(Vec3 vec, Mirror mirror) {
		return mirror(vec.subtract(BLOCK_CENTER), mirror).add(BLOCK_CENTER);
	}

}
