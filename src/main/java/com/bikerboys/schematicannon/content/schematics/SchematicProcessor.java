package com.bikerboys.schematicannon.content.schematics;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.bikerboys.schematicannon.AllStructureProcessorTypes;
import com.bikerboys.schematicannon.foundation.NBTProcessors;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;

public class SchematicProcessor implements StructureProcessor {

	public static final SchematicProcessor INSTANCE = new SchematicProcessor();
	public static final MapCodec<SchematicProcessor> CODEC = MapCodec.unit(INSTANCE);

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo processBlock(LevelReader world, BlockPos pos, BlockPos anotherPos,
			BlockPos pivot, StructureTemplate.StructureBlockInfo info, StructurePlaceSettings settings) {
		if (info.nbt() != null && info.state().hasBlockEntity()) {
			BlockEntity be = ((EntityBlock) info.state().getBlock()).newBlockEntity(info.pos(), info.state());
			if (be != null) {
				CompoundTag nbt = NBTProcessors.process(info.state(), be, info.nbt(), false);
				if (nbt != info.nbt())
					return new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), nbt);
			}
		}
		return info;
	}

	@Override
	public MapCodec<? extends StructureProcessor> codec() {
		return CODEC;
	}

}
