package com.bikerboys.schematicannon.content.equipment.clipboard;

import java.util.List;
import java.util.UUID;

import com.bikerboys.schematicannon.AllBlocks;
import com.bikerboys.schematicannon.AllDataComponents;
import com.bikerboys.schematicannon.foundation.blockEntity.SmartBlockEntity;
import com.bikerboys.schematicannon.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ClipboardBlockEntity extends SmartBlockEntity {

	public ItemStack dataContainer;
	private UUID lastEdit;

	public ClipboardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		dataContainer = AllBlocks.CLIPBOARD_ITEM.get().getDefaultInstance();
	}

	@Override
	public void initialize() {
		super.initialize();
		updateWrittenState();
	}

	public void onEditedBy(Player player) {
		lastEdit = player.getUUID();
		notifyUpdate();
		updateWrittenState();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
	}

	public void updateWrittenState() {
		BlockState blockState = getBlockState();
		if (!blockState.is(AllBlocks.CLIPBOARD.get()))
			return;
		if (level.isClientSide())
			return;
		boolean isWritten = blockState.getValue(ClipboardBlock.WRITTEN);
		boolean shouldBeWritten = dataContainer.has(AllDataComponents.CLIPBOARD_DATA);
		if (isWritten == shouldBeWritten)
			return;
		level.setBlockAndUpdate(worldPosition, blockState.setValue(ClipboardBlock.WRITTEN, shouldBeWritten));
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		CompoundTag clipboardData = dataContainer.get(AllDataComponents.CLIPBOARD_DATA);
		if (clipboardData != null)
			tag.put("ClipboardData", clipboardData.copy());
		if (clientPacket && lastEdit != null)
			tag.store("LastEdit", UUIDUtil.CODEC, lastEdit);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		dataContainer = AllBlocks.CLIPBOARD_ITEM.get().getDefaultInstance();
		if (tag.contains("ClipboardData"))
			dataContainer.set(AllDataComponents.CLIPBOARD_DATA, tag.getCompoundOrEmpty("ClipboardData").copy());

		if (clientPacket && level != null && level.isClientSide())
			readClientSide(tag);
	}

	private void readClientSide(CompoundTag tag) {
		Minecraft mc = Minecraft.getInstance();
		Object currentScreen = mc.gui.screen();
		if (!ClipboardScreen.class.isInstance(currentScreen))
			return;
		ClipboardScreen cs = ClipboardScreen.class.cast(currentScreen);
		if (tag.read("LastEdit", UUIDUtil.CODEC).filter(mc.player.getUUID()::equals).isPresent())
			return;
		if (!worldPosition.equals(cs.targetedBlock))
			return;
		cs.reopenWith(dataContainer);
	}

	public ClipboardBlockEntity(BlockPos pos, BlockState state) {
		this(com.bikerboys.schematicannon.AllBlockEntityTypes.CLIPBOARD.get(), pos, state);
	}

	@Override
	protected void readValue(ValueInput input) {
		dataContainer = input.read("Item", ItemStack.CODEC)
			.filter(stack -> stack.is(AllBlocks.CLIPBOARD_ITEM.get()))
			.orElseGet(() -> AllBlocks.CLIPBOARD_ITEM.get().getDefaultInstance());
	}

	@Override
	protected void writeValue(ValueOutput output) {
		output.store("Item", ItemStack.CODEC, dataContainer);
	}


}
