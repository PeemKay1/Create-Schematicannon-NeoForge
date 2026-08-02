package com.bikerboys.schematicannon.foundation.events;

import com.bikerboys.schematicannon.SchematicannonClient;
import com.bikerboys.schematicannon.foundation.utility.AnimationTickHolder;
import com.bikerboys.schematicannon.foundation.utility.CameraAngleAnimationService;
import com.bikerboys.schematicannon.foundation.utility.TickBasedCache;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public final class ClientEvents {
	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetWorld());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetWorld());
	}

	public static void onTick() {
		if (!isGameActive())
			return;
		SchematicannonClient.SCHEMATIC_SENDER.tick();
		SchematicannonClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		SchematicannonClient.SCHEMATIC_HANDLER.tick();
		CameraAngleAnimationService.tick();
		TickBasedCache.clientTick();
	}

	private static void resetWorld() {
		SchematicannonClient.SCHEMATIC_HANDLER.clearPreview();
		SchematicannonClient.invalidateRenderers();
		AnimationTickHolder.reset();
	}

	private static boolean isGameActive() {
		return Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
	}

	private ClientEvents() {}
}
