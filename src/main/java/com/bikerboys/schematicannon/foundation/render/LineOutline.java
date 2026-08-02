package com.bikerboys.schematicannon.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

public class LineOutline {
	private final AABBOutline.Params params = new AABBOutline.Params();
	public AABBOutline.Params getParams() { return params; }
	public LineOutline set(Vec3 start, Vec3 end) { return this; }
	public void render(PoseStack poseStack, SuperRenderTypeBuffer buffer, Vec3 camera, float partialTicks) {}
}
