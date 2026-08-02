package com.bikerboys.schematicannon.foundation.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class CachedRenderBBBlockEntity extends SyncedBlockEntity {

	public CachedRenderBBBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

}
