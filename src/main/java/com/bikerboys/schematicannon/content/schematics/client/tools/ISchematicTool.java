package com.bikerboys.schematicannon.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;

import com.bikerboys.schematicannon.foundation.render.SuperRenderTypeBuffer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;

public interface ISchematicTool {

	void init();
	void updateSelection();

	boolean handleRightClick();
	boolean handleMouseWheel(double delta);

	void renderTool(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera);
	void renderOverlay(GuiGraphicsExtractor graphics, float partialTicks, int width, int height);
	void renderOnSchematic(PoseStack ms, SuperRenderTypeBuffer buffer);

}
