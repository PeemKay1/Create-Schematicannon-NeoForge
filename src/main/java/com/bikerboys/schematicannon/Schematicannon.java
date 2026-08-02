package com.bikerboys.schematicannon;

import java.util.Random;

import org.slf4j.Logger;

import com.bikerboys.schematicannon.content.schematics.ServerSchematicLoader;
import com.bikerboys.schematicannon.foundation.CreateNBTProcessors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Schematicannon.ID)
public class Schematicannon {
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


	public Schematicannon(IEventBus modEventBus, ModContainer modContainer) {
		onCtor(modEventBus);
	}

	public static void onCtor(IEventBus modEventBus) {
		LOGGER.info("{} {} initializing! Commit hash: {}", NAME, SchematicannonBuildInfo.VERSION, SchematicannonBuildInfo.GIT_COMMIT);



		AllSoundEvents.prepare();
		AllBlocks.register(modEventBus);
		AllItems.register(modEventBus);
		AllCreativeModeTabs.register(modEventBus);
		AllDataComponents.register(modEventBus);
		AllMenuTypes.register(modEventBus);
		AllBlockEntityTypes.register(modEventBus);
		AllParticleTypes.register(modEventBus);
		AllStructureProcessorTypes.register(modEventBus);
		modEventBus.addListener(AllPackets::register);


		AllSchematicStateFilters.registerDefaults();

		modEventBus.addListener(Schematicannon::init);
		modEventBus.addListener(AllSoundEvents::register);

	}

	public static void init(final FMLCommonSetupEvent event) {
		CreateNBTProcessors.register();


	}


	public static Identifier asResource(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
