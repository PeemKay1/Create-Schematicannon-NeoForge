package com.bikerboys.schematicannon.foundation;

import com.bikerboys.schematicannon.AllBlockEntityTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CreateNBTProcessors {
	public static void register() {
		BlockEntityType<?> lectern =
			BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("lectern"));
		if (lectern != null)
			NBTProcessors.addProcessor(lectern,
				data -> NBTProcessors.containsClickEvent(data) ? null : data);

		NBTProcessors.addProcessor(AllBlockEntityTypes.CLIPBOARD.get(), CreateNBTProcessors::clipboardProcessor);

	}

	public static CompoundTag clipboardProcessor(CompoundTag data) {
		return NBTProcessors.containsClickEvent(data) ? null : data;
	}
}
