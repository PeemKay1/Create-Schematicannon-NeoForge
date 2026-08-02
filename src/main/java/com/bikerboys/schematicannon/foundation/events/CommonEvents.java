package com.bikerboys.schematicannon.foundation.events;

import com.bikerboys.schematicannon.Schematicannon;
import com.bikerboys.schematicannon.content.schematics.SchematicInstances;
import com.bikerboys.schematicannon.foundation.utility.TickBasedCache;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;

public class CommonEvents {

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> onServerTick());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> Schematicannon.SCHEMATIC_RECEIVER.shutdown());
		ServerLevelEvents.UNLOAD.register((server, level) -> SchematicInstances.invalidate(level));
	}

	public static void onServerTick() {
		Schematicannon.SCHEMATIC_RECEIVER.tick();
		TickBasedCache.tick();
	}
}
