package com.bikerboys.schematicannon.foundation.render;

import com.bikerboys.schematicannon.AllSpecialTextures;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AABBOutline {
	private AABB bounds;
	private final Params params = new Params();

	public AABBOutline(AABB bounds) {
		this.bounds = bounds;
	}

	public AABBOutline setBounds(AABB bounds) {
		this.bounds = bounds;
		return this;
	}

	public AABB getBounds() {
		return bounds;
	}

	public Params getParams() {
		return params;
	}

	public AABBOutline colored(int color) { params.colored(color); return this; }
	public AABBOutline lineWidth(float width) { params.lineWidth(width); return this; }
	public AABBOutline highlightFace(Direction face) { params.highlightFace(face); return this; }
	public AABBOutline withFaceTextures(AllSpecialTextures texture, AllSpecialTextures highlighted) {
		params.withFaceTextures(texture, highlighted);
		return this;
	}

	public void render(PoseStack poseStack, SuperRenderTypeBuffer buffer, Vec3 camera, float partialTicks) {}

	public static class Params {
		public Params colored(int color) { return this; }
		public Params lineWidth(float width) { return this; }
		public Params disableCull() { return this; }
		public Params disableLineNormals() { return this; }
		public Params highlightFace(Direction face) { return this; }
		public Params withFaceTexture(AllSpecialTextures texture) { return this; }
		public Params withFaceTextures(AllSpecialTextures texture, AllSpecialTextures highlighted) { return this; }
		public Params clearTextures() { return this; }
	}
}
