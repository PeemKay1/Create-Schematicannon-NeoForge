package com.bikerboys.schematicannon.foundation.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ItemStackHandler implements IItemHandlerModifiable, Container {
	private final List<ItemStack> stacks;

	public ItemStackHandler(int size) {
		stacks = new ArrayList<>(Collections.nCopies(size, ItemStack.EMPTY));
	}

	@Override
	public int getSlots() {
		return stacks.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return stacks.get(slot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		stacks.set(slot, stack);
		onContentsChanged(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty() || !isItemValid(slot, stack))
			return stack;
		ItemStack present = stacks.get(slot);
		if (!present.isEmpty() && !ItemStack.isSameItemSameComponents(present, stack))
			return stack;
		int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
		int accepted = Math.min(stack.getCount(), limit - present.getCount());
		if (accepted <= 0)
			return stack;
		if (!simulate) {
			ItemStack inserted = present.isEmpty() ? stack.copy() : present.copy();
			inserted.setCount(present.getCount() + accepted);
			stacks.set(slot, inserted);
			onContentsChanged(slot);
		}
		ItemStack remainder = stack.copy();
		remainder.shrink(accepted);
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack present = stacks.get(slot);
		if (present.isEmpty() || amount <= 0)
			return ItemStack.EMPTY;
		int count = Math.min(amount, present.getCount());
		ItemStack extracted = present.copyWithCount(count);
		if (!simulate) {
			ItemStack remainder = present.copy();
			remainder.shrink(count);
			stacks.set(slot, remainder);
			onContentsChanged(slot);
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

	protected void onContentsChanged(int slot) {
	}

	public void load(ValueInput input) {
		for (int i = 0; i < stacks.size(); i++)
			stacks.set(i, input.read("Slot" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
	}

	public void save(ValueOutput output) {
		for (int i = 0; i < stacks.size(); i++)
			output.store("Slot" + i, ItemStack.OPTIONAL_CODEC, stacks.get(i));
	}

	@Override
	public int getContainerSize() {
		return getSlots();
	}

	@Override
	public boolean isEmpty() {
		return stacks.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return getStackInSlot(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return extractItem(slot, amount, false);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack result = stacks.get(slot);
		stacks.set(slot, ItemStack.EMPTY);
		return result;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		setStackInSlot(slot, stack);
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < stacks.size(); i++)
			setStackInSlot(i, ItemStack.EMPTY);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return isItemValid(slot, stack);
	}
}
