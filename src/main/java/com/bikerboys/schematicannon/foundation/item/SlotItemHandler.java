package com.bikerboys.schematicannon.foundation.item;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemHandler extends Slot {
	private final IItemHandlerModifiable handler;

	public SlotItemHandler(IItemHandlerModifiable handler, int index, int x, int y) {
		super(handler instanceof net.minecraft.world.Container container
			? container : new HandlerContainer(handler), index, x, y);
		this.handler = handler;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return handler.isItemValid(getContainerSlot(), stack);
	}

	private static final class HandlerContainer extends ItemStackHandler {
		private final IItemHandlerModifiable delegate;

		private HandlerContainer(IItemHandlerModifiable delegate) {
			super(delegate.getSlots());
			this.delegate = delegate;
		}

		@Override public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }
		@Override public void setStackInSlot(int slot, ItemStack stack) { delegate.setStackInSlot(slot, stack); }
		@Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return delegate.insertItem(slot, stack, simulate); }
		@Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return delegate.extractItem(slot, amount, simulate); }
		@Override public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
		@Override public boolean isItemValid(int slot, ItemStack stack) { return delegate.isItemValid(slot, stack); }
	}
}
