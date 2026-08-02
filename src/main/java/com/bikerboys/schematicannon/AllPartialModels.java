package com.bikerboys.schematicannon;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelDebugName;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public final class AllPartialModels {
	public static final StandaloneModelKey<BlockStateModel> SCHEMATICANNON_CONNECTOR = key("schematicannon_connector");
	public static final StandaloneModelKey<BlockStateModel> SCHEMATICANNON_PIPE = key("schematicannon_pipe");

	private static StandaloneModelKey<BlockStateModel> key(String name) {
		return new StandaloneModelKey<>((ModelDebugName) () -> Schematicannon.ID + ":" + name);
	}

	public static void register(ModelEvent.RegisterStandalone event) {
		event.register(SCHEMATICANNON_CONNECTOR, SimpleUnbakedStandaloneModel.blockStateModel(
			Schematicannon.asResource("block/schematicannon/connector")));
		event.register(SCHEMATICANNON_PIPE, SimpleUnbakedStandaloneModel.blockStateModel(
			Schematicannon.asResource("block/schematicannon/pipe")));
	}

	private AllPartialModels() {}
}
