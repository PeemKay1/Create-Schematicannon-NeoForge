package com.bikerboys.schematicannon.foundation;

import com.bikerboys.schematicannon.SchematicannonClient;
import com.bikerboys.schematicannon.Schematicannon;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements IdentifiableResourceReloadListener, ResourceManagerReloadListener {
	@Override
	public Identifier getFabricId() {
		return Schematicannon.asResource("client_resources");
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		SchematicannonClient.invalidateRenderers();
	}

}
