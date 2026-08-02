package com.bikerboys.schematicannon.foundation.item;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public interface IItemHandler {
	int getSlots();

	ItemStack getStackInSlot(int slot);

	ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

	ItemStack extractItem(int slot, int amount, boolean simulate);

	int getSlotLimit(int slot);

	boolean isItemValid(int slot, ItemStack stack);

	static IItemHandler of(Container container) {
		return container instanceof IItemHandler handler ? handler : new ContainerItemHandler(container);
	}

	static IItemHandler of(Storage<ItemVariant> storage) {
		return new FabricStorageItemHandler(storage);
	}
}
