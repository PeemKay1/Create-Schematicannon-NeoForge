package com.bikerboys.schematicannon.content.schematics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;

public class SchematicAndQuillItem extends Item {

	public SchematicAndQuillItem(Properties properties) {
		super(properties);
	}

	public static void replaceStructureVoidWithAir(CompoundTag nbt) {
		String air = BuiltInRegistries.BLOCK.getKey(Blocks.AIR)
			.toString();
		String structureVoid = BuiltInRegistries.BLOCK.getKey(Blocks.STRUCTURE_VOID)
			.toString();

		nbt.getListOrEmpty("palette")
			.compoundStream()
			.forEach(c -> {
			if (c.getStringOr("Name", "")
				.equals(structureVoid))
				c.putString("Name", air);
		});
	}




}
