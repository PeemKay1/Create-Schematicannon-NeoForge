package com.bikerboys.schematicannon.foundation.mixin.client;

import com.bikerboys.schematicannon.SchematicannonClient;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Inject(method = "submitFeatures", at = @At("RETURN"))
	private void schematicannon$submitPreview(LevelRenderState state, SubmitNodeCollector collector,
			boolean renderBlockOutline, CallbackInfo ci) {
		if (state.cameraRenderState != null && state.cameraRenderState.pos != null)
			SchematicannonClient.SCHEMATIC_HANDLER.submitGeometry(
				new PoseStack(), collector, state.cameraRenderState);
	}
}
