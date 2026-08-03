package com.bikerboys.schematicannon.content.schematics.cannon;

import com.bikerboys.schematicannon.AllMenuTypes;
import com.bikerboys.schematicannon.foundation.gui.menu.MenuBase;
import com.bikerboys.schematicannon.foundation.item.SlotItemHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class SchematicannonMenu extends MenuBase<SchematicannonBlockEntity> {

	public SchematicannonMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf buffer) {
		super(type, id, inv, buffer);
	}

	public SchematicannonMenu(int id, Inventory inv, FriendlyByteBuf buffer) {
		this(AllMenuTypes.SCHEMATICANNON.get(), id, inv, buffer);
	}

	public SchematicannonMenu(int id, Inventory inv) {
		this(AllMenuTypes.SCHEMATICANNON.get(), id, inv, findClientBlockEntity());
	}

	private static SchematicannonBlockEntity findClientBlockEntity() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null && minecraft.hitResult instanceof BlockHitResult hit
			&& minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof SchematicannonBlockEntity cannon)
			return cannon;
		throw new IllegalStateException("Schematicannon menu opened without a targeted cannon");
	}

	public SchematicannonMenu(MenuType<?> type, int id, Inventory inv, SchematicannonBlockEntity be) {
		super(type, id, inv, be);
	}

	public static SchematicannonMenu create(int id, Inventory inv, SchematicannonBlockEntity be) {
		return new SchematicannonMenu(AllMenuTypes.SCHEMATICANNON.get(), id, inv, be);
	}

	@Override
	protected SchematicannonBlockEntity createOnClient(FriendlyByteBuf extraData) {
		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity blockEntity = world.getBlockEntity(extraData.readBlockPos());
		if (blockEntity instanceof SchematicannonBlockEntity schematicannon) {
			schematicannon.readClient(extraData.readNbt());
			return schematicannon;
		}
		return null;
	}

	@Override
	protected void initAndReadInventory(SchematicannonBlockEntity contentHolder) {
	}

	@Override
	protected void addSlots() {
		int x = 0;
		int y = 0;

		addSlot(new SlotItemHandler(contentHolder.inventory, 0, x + 15, y + 65));
		addSlot(new SlotItemHandler(contentHolder.inventory, 1, x + 171, y + 65));
		addSlot(new SlotItemHandler(contentHolder.inventory, 2, x + 134, y + 19));
		addSlot(new SlotItemHandler(contentHolder.inventory, 3, x + 174, y + 19));
		addSlot(new SlotItemHandler(contentHolder.inventory, 4, x + 15, y + 19));

		addPlayerSlots(37, 161);
	}

	@Override
	protected void saveData(SchematicannonBlockEntity contentHolder) {
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		ItemStack original = stack.copy();

		if (index < 5) {
			if (!moveItemStackTo(stack, 5, slots.size(), false))
				return ItemStack.EMPTY;
		} else {
			boolean moved;
			if (contentHolder.inventory.isItemValid(0, stack))
				moved = moveItemStackTo(stack, 0, 1, false);
			else if (contentHolder.inventory.isItemValid(2, stack))
				moved = moveItemStackTo(stack, 2, 3, false);
			else if (contentHolder.inventory.isItemValid(4, stack))
				moved = moveItemStackTo(stack, 4, 5, false);
			else
				moved = false;

			if (!moved)
				return ItemStack.EMPTY;
		}

		if (stack.isEmpty())
			clickedSlot.set(ItemStack.EMPTY);
		else
			clickedSlot.setChanged();

		if (stack.getCount() == original.getCount())
			return ItemStack.EMPTY;

		clickedSlot.onTake(player, stack);
		return original;
	}

}
