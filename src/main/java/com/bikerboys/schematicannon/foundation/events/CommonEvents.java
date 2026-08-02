package com.bikerboys.schematicannon.foundation.events;

import com.bikerboys.schematicannon.Schematicannon;
import com.bikerboys.schematicannon.content.schematics.SchematicInstances;
import com.bikerboys.schematicannon.foundation.utility.ServerSpeedProvider;
import com.bikerboys.schematicannon.foundation.utility.TickBasedCache;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class CommonEvents {

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		Schematicannon.SCHEMATIC_RECEIVER.tick();
		ServerSpeedProvider.serverTick();

		TickBasedCache.tick();
	}

	@SubscribeEvent
	public static void onChunkUnloaded(ChunkEvent.Unload event) {
	}


	@SubscribeEvent
	public static void playerLoggedOut(PlayerLoggedOutEvent event) {
		Player player = event.getEntity();
	}




	@SubscribeEvent
	public static void serverStopping(ServerStoppingEvent event) {
		Schematicannon.SCHEMATIC_RECEIVER.shutdown();
	}


	@SubscribeEvent
	public static void onUnloadWorld(LevelEvent.Unload event) {
		LevelAccessor world = event.getLevel();
		if (world instanceof net.minecraft.world.level.Level level)
			SchematicInstances.invalidate(level);
	}





}
