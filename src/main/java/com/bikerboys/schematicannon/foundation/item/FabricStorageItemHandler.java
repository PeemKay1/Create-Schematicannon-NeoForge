package com.bikerboys.schematicannon.foundation.item;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

final class FabricStorageItemHandler implements IItemHandler {
	private final Storage<ItemVariant> storage;

	FabricStorageItemHandler(Storage<ItemVariant> storage) {
		this.storage = storage;
	}

	private List<StorageView<ItemVariant>> views() {
		List<StorageView<ItemVariant>> views = new ArrayList<>();
		storage.iterator().forEachRemaining(views::add);
		return views;
	}

	@Override
	public int getSlots() {
		return Math.max(1, views().size());
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		List<StorageView<ItemVariant>> views = views();
		if (slot < 0 || slot >= views.size())
			return ItemStack.EMPTY;
		StorageView<ItemVariant> view = views.get(slot);
		return view.isResourceBlank() ? ItemStack.EMPTY
			: view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot != 0 || stack.isEmpty())
			return stack;
		long inserted;
		try (Transaction transaction = Transaction.openOuter()) {
			inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
			if (!simulate)
				transaction.commit();
		}
		ItemStack remainder = stack.copy();
		remainder.shrink((int) inserted);
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		List<StorageView<ItemVariant>> views = views();
		if (slot < 0 || slot >= views.size() || amount <= 0)
			return ItemStack.EMPTY;
		StorageView<ItemVariant> view = views.get(slot);
		if (view.isResourceBlank())
			return ItemStack.EMPTY;
		long extracted;
		ItemVariant resource = view.getResource();
		try (Transaction transaction = Transaction.openOuter()) {
			extracted = view.extract(resource, amount, transaction);
			if (!simulate)
				transaction.commit();
		}
		return resource.toStack((int) extracted);
	}

	@Override
	public int getSlotLimit(int slot) {
		List<StorageView<ItemVariant>> views = views();
		return slot >= 0 && slot < views.size()
			? (int) Math.min(Integer.MAX_VALUE, views.get(slot).getCapacity()) : 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return storage.supportsInsertion();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FabricStorageItemHandler other && other.storage == storage;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(storage);
	}
}
