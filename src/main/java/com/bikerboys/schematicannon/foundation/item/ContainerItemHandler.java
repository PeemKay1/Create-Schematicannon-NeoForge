package com.bikerboys.schematicannon.foundation.item;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

final class ContainerItemHandler implements IItemHandlerModifiable {
	private final Container container;

	ContainerItemHandler(Container container) {
		this.container = container;
	}

	@Override
	public int getSlots() {
		return container.getContainerSize();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return container.getItem(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty() || !container.canPlaceItem(slot, stack))
			return stack;
		ItemStack present = container.getItem(slot);
		if (!present.isEmpty() && !ItemStack.isSameItemSameComponents(present, stack))
			return stack;
		int limit = Math.min(container.getMaxStackSize(stack), stack.getMaxStackSize());
		int accepted = Math.min(stack.getCount(), limit - present.getCount());
		if (accepted <= 0)
			return stack;
		if (!simulate) {
			ItemStack inserted = present.isEmpty() ? stack.copy() : present.copy();
			inserted.setCount(present.getCount() + accepted);
			container.setItem(slot, inserted);
			container.setChanged();
		}
		ItemStack remainder = stack.copy();
		remainder.shrink(accepted);
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack present = container.getItem(slot);
		if (present.isEmpty() || amount <= 0)
			return ItemStack.EMPTY;
		int extractedCount = Math.min(amount, present.getCount());
		ItemStack extracted = present.copyWithCount(extractedCount);
		if (!simulate) {
			container.removeItem(slot, extractedCount);
			container.setChanged();
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return container.getMaxStackSize(container.getItem(slot));
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return container.canPlaceItem(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		container.setItem(slot, stack);
		container.setChanged();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ContainerItemHandler other && other.container == container;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(container);
	}
}
