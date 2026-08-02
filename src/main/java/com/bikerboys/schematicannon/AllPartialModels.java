package com.bikerboys.schematicannon;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

public final class AllPartialModels {
	public static final ExtraModelKey<BlockStateModel> SCHEMATICANNON_CONNECTOR = key("schematicannon_connector");
	public static final ExtraModelKey<BlockStateModel> SCHEMATICANNON_PIPE = key("schematicannon_pipe");

	private static ExtraModelKey<BlockStateModel> key(String name) {
		return ExtraModelKey.create(() -> Schematicannon.ID + ":" + name);
	}

	public static void register() {
		ModelLoadingPlugin.register(context -> {
			context.addModel(SCHEMATICANNON_CONNECTOR, SimpleUnbakedExtraModel.blockStateModel(
				Schematicannon.asResource("block/schematicannon/connector")));
			context.addModel(SCHEMATICANNON_PIPE, SimpleUnbakedExtraModel.blockStateModel(
				Schematicannon.asResource("block/schematicannon/pipe")));
		});
	}

	private AllPartialModels() {}
}
