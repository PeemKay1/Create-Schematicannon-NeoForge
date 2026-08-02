package com.bikerboys.schematicannon;

import java.util.Random;

import org.slf4j.Logger;

import com.bikerboys.schematicannon.content.schematics.ServerSchematicLoader;
import com.bikerboys.schematicannon.foundation.CreateNBTProcessors;
import com.bikerboys.schematicannon.foundation.events.CommonEvents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;

import net.fabricmc.api.ModInitializer;

public class Schematicannon implements ModInitializer {
	public static final String ID = "schematicannon";
	public static final String NAME = "Schematicannon";

	public static final Logger LOGGER = LogUtils.getLogger();

	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/**
	 * Use the {@link Random} of a local {@link Level} or {@link Entity} or create one
	 */
	@Deprecated
	public static final Random RANDOM = new Random();


	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();


	@Override
	public void onInitialize() {
		LOGGER.info("{} {} initializing! Commit hash: {}", NAME, SchematicannonBuildInfo.VERSION, SchematicannonBuildInfo.GIT_COMMIT);

		AllSoundEvents.prepare();
		AllBlocks.register();
		AllItems.register();
		AllCreativeModeTabs.register();
		AllDataComponents.register();
		AllMenuTypes.register();
		AllBlockEntityTypes.register();
		AllParticleTypes.register();
		AllStructureProcessorTypes.register();
		AllSoundEvents.register();
		AllPackets.register();

		AllSchematicStateFilters.registerDefaults();
		CreateNBTProcessors.register();
		CommonEvents.register();
	}


	public static Identifier asResource(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
