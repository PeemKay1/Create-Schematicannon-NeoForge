package com.bikerboys.schematicannon;


import com.bikerboys.schematicannon.content.schematics.client.ClientSchematicLoader;
import com.bikerboys.schematicannon.content.schematics.client.SchematicAndQuillHandler;
import com.bikerboys.schematicannon.content.schematics.client.SchematicHandler;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonRenderer;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonScreen;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableScreen;
import com.bikerboys.schematicannon.foundation.ClientResourceReloadListener;
import com.bikerboys.schematicannon.foundation.events.ClientEvents;
import com.bikerboys.schematicannon.foundation.events.InputEvents;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.server.packs.PackType;

public class SchematicannonClient implements ClientModInitializer {

	public static final ClientSchematicLoader SCHEMATIC_SENDER = new ClientSchematicLoader();
	public static final SchematicHandler SCHEMATIC_HANDLER = new SchematicHandler();
	public static final SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();



	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

	@Override
	public void onInitializeClient() {
		AllKeys.register();
		ClientEvents.register();
		InputEvents.register();
		AllParticleTypes.registerFactories();
		AllPartialModels.register();
		AllPackets.registerClient();
		BlockEntityRenderers.register(AllBlockEntityTypes.SCHEMATICANNON.get(), SchematicannonRenderer::new);
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(RESOURCE_RELOAD_LISTENER);
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
			Schematicannon.asResource("schematic"), SCHEMATIC_HANDLER);
		MenuScreens.register(AllMenuTypes.SCHEMATIC_TABLE.get(), SchematicTableScreen::new);
		MenuScreens.register(AllMenuTypes.SCHEMATICANNON.get(), SchematicannonScreen::new);
	}



	public static void invalidateRenderers() {
		SCHEMATIC_HANDLER.updateRenderers();
	}



}
