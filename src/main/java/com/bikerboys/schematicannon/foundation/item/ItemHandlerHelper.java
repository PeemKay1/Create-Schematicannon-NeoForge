package com.bikerboys.schematicannon.foundation.item;

import net.minecraft.world.item.ItemStack;

public final class ItemHandlerHelper {
	public static ItemStack insertItem(IItemHandler handler, ItemStack stack, boolean simulate) {
		ItemStack remainder = stack;
		for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++)
			remainder = handler.insertItem(slot, remainder, simulate);
		return remainder;
	}

	private ItemHandlerHelper() {
	}
}
