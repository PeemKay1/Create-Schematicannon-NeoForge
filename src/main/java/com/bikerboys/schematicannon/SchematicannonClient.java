package com.bikerboys.schematicannon;


import com.bikerboys.schematicannon.content.schematics.client.ClientSchematicLoader;
import com.bikerboys.schematicannon.content.schematics.client.SchematicAndQuillHandler;
import com.bikerboys.schematicannon.content.schematics.client.SchematicHandler;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonRenderer;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonScreen;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableScreen;
import com.bikerboys.schematicannon.foundation.ClientResourceReloadListener;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@Mod(value = Schematicannon.ID, dist = Dist.CLIENT)
public class SchematicannonClient {

	public static final ClientSchematicLoader SCHEMATIC_SENDER = new ClientSchematicLoader();
	public static final SchematicHandler SCHEMATIC_HANDLER = new SchematicHandler();
	public static final SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();



	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

	public SchematicannonClient(IEventBus modEventBus, ModContainer container) {
		onCtorClient(modEventBus, NeoForge.EVENT_BUS);
	}

	public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(SchematicannonClient::clientInit);
		modEventBus.addListener(AllParticleTypes::registerFactories);
		modEventBus.addListener(AllPackets::registerClient);
		modEventBus.addListener(AllPartialModels::register);
		modEventBus.addListener(SchematicannonClient::registerRenderers);
		modEventBus.addListener(SchematicannonClient::registerReloadListeners);
		modEventBus.addListener(SchematicannonClient::registerGuiLayers);
		modEventBus.addListener(SchematicannonClient::registerMenuScreens);

	}

	public static void clientInit(final FMLClientSetupEvent event) {


	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(AllBlockEntityTypes.SCHEMATICANNON.get(), SchematicannonRenderer::new);
	}

	private static void registerReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(Schematicannon.asResource("client_resources"), RESOURCE_RELOAD_LISTENER);
	}

	private static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, Schematicannon.asResource("schematic"), SCHEMATIC_HANDLER);
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(AllMenuTypes.SCHEMATIC_TABLE.get(), SchematicTableScreen::new);
		event.register(AllMenuTypes.SCHEMATICANNON.get(), SchematicannonScreen::new);
	}



	public static void invalidateRenderers() {
		SCHEMATIC_HANDLER.updateRenderers();
	}



}
