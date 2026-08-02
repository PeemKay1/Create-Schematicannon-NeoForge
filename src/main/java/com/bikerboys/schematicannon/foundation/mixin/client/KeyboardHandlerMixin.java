package com.bikerboys.schematicannon.foundation.mixin.client;

import com.bikerboys.schematicannon.SchematicannonClient;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Inject(method = "keyPress", at = @At("HEAD"))
	private void schematicannon$onKey(long window, int action, KeyEvent event, CallbackInfo ci) {
		SchematicannonClient.SCHEMATIC_HANDLER.onKeyInput(event.key(), action != 0);
	}
}
