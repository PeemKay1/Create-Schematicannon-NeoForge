package com.bikerboys.schematicannon.content.equipment.clipboard;

import javax.annotation.Nonnull;

import com.bikerboys.schematicannon.AllDataComponents;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class ClipboardBlockItem extends BlockItem  {

	public ClipboardBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return super.useOn(context);
		return use(context.getLevel(), player, context.getHand());
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, Player pPlayer, ItemStack pStack,
		BlockState pState) {
		if (pLevel.isClientSide())
			return false;
		if (!(pLevel.getBlockEntity(pPos) instanceof ClipboardBlockEntity cbe))
			return false;
		cbe.dataContainer = pStack.copyWithCount(1);
		cbe.notifyUpdate();
		return true;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (hand == InteractionHand.OFF_HAND)
			return InteractionResult.PASS;

		player.getCooldowns()
			.addCooldown(heldItem, 10);
		if (world.isClientSide())
			openScreen(player, heldItem);
		CompoundTag tag = heldItem.getOrDefault(AllDataComponents.CLIPBOARD_DATA, new CompoundTag()).copy();
		// A material checklist is read-only and must stay a written clipboard.
		// Changing its type to EDITING here broke its model/state after it was opened.
		if (!tag.getBooleanOr("Readonly", false))
			tag.putInt("Type", ClipboardOverrides.ClipboardType.EDITING.ordinal());
		heldItem.set(AllDataComponents.CLIPBOARD_DATA, tag);

		return InteractionResult.SUCCESS;
	}

	private void openScreen(Player player, ItemStack stack) {
		if (Minecraft.getInstance().player == player)
			Minecraft.getInstance().setScreenAndShow(new ClipboardScreen(player.getInventory().getSelectedSlot(), stack, null));
	}

	public void registerModelOverrides() {
		// Registered by the client bootstrap once the 26.2 item-model port is enabled.
	}

}
