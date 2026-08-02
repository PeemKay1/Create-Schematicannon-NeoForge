package com.bikerboys.schematicannon.foundation.blockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;

@ParametersAreNonnullByDefault
public abstract class SyncedBlockEntity extends BlockEntity {

	public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag update = new CompoundTag();
		update.put("SchematicannonClientData", writeClient(new CompoundTag()));
		return update;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(ValueInput input) {
		readClient(input.read("SchematicannonClientData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
	}

	@Override
	public void onDataPacket(Connection connection, ValueInput input) {
		handleUpdateTag(input);
	}

	// Special handling for client update packets
	public void readClient(CompoundTag tag) {
	}

	// Special handling for client update packets
	public CompoundTag writeClient(CompoundTag tag) {
		return tag;
	}

	public void sendData() {
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(getBlockPos());
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

	public LevelChunk containedChunk() {
		return level.getChunkAt(worldPosition);
	}

	@SuppressWarnings("deprecation")
	public HolderGetter<Block> blockHolderGetter() {
		return level != null ? level.holderLookup(Registries.BLOCK)
			: BuiltInRegistries.BLOCK;
	}

}
