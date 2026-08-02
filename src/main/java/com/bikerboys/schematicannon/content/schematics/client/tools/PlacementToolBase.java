package com.bikerboys.schematicannon.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;

import com.bikerboys.schematicannon.foundation.render.SuperRenderTypeBuffer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;

public abstract class PlacementToolBase extends SchematicToolBase {

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	@Override
	public void renderTool(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
		super.renderTool(ms, buffer, camera);
	}

	@Override
	public void renderOverlay(GuiGraphicsExtractor graphics, float partialTicks, int width, int height) {
		super.renderOverlay(graphics, partialTicks, width, height);
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		return false;
	}

	@Override
	public boolean handleRightClick() {
		return false;
	}

}
