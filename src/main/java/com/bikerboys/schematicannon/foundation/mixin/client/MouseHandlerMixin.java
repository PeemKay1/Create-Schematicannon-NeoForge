package com.bikerboys.schematicannon.foundation.mixin.client;

import com.bikerboys.schematicannon.SchematicannonClient;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void schematicannon$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (SchematicannonClient.SCHEMATIC_HANDLER.mouseScrolled(vertical)
			|| SchematicannonClient.SCHEMATIC_AND_QUILL_HANDLER.mouseScrolled(vertical))
			ci.cancel();
	}

	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	private void schematicannon$onButton(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
		boolean pressed = action != 0;
		if (SchematicannonClient.SCHEMATIC_HANDLER.onMouseInput(button.button(), pressed)
			|| SchematicannonClient.SCHEMATIC_AND_QUILL_HANDLER.onMouseInput(button.button(), pressed))
			ci.cancel();
	}
}
