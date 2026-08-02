package com.bikerboys.schematicannon.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.List;

import com.bikerboys.schematicannon.AllDataComponents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ClipboardEntry {

	public boolean checked;
	public MutableComponent text;
	public ItemStack icon;
	public int itemAmount;

	public ClipboardEntry(boolean checked, MutableComponent text) {
		this.checked = checked;
		this.text = text;
		this.icon = ItemStack.EMPTY;
	}

	public ClipboardEntry displayItem(ItemStack icon, int amount) {
		this.icon = icon;
		this.itemAmount = amount;
		return this;
	}

	public static List<List<ClipboardEntry>> readAll(ItemStack clipboardItem) {
		CompoundTag data = clipboardItem.get(AllDataComponents.CLIPBOARD_DATA);
		if (data == null)
			return new ArrayList<>();

		List<List<ClipboardEntry>> pages = new ArrayList<>();
		for (var pageElement : data.getListOrEmpty("Pages")) {
			if (!(pageElement instanceof CompoundTag pageTag))
				continue;
			List<ClipboardEntry> page = new ArrayList<>();
			for (var entryElement : pageTag.getListOrEmpty("Entries"))
				if (entryElement instanceof CompoundTag entryTag)
					page.add(readNBT(entryTag));
			pages.add(page);
		}
		return pages;
	}

	public static List<ClipboardEntry> getLastViewedEntries(ItemStack heldItem) {
		List<List<ClipboardEntry>> pages = readAll(heldItem);
		if (pages.isEmpty())
			return new ArrayList<>();
		CompoundTag data = heldItem.get(AllDataComponents.CLIPBOARD_DATA);
		int page = data == null ? 0 : Math.min(data.getIntOr("PreviouslyOpenedPage", 0), pages.size() - 1);
		return pages.get(page);
	}

	public static void saveAll(List<List<ClipboardEntry>> entries, ItemStack clipboardItem) {
		CompoundTag data = clipboardItem.getOrDefault(AllDataComponents.CLIPBOARD_DATA, new CompoundTag()).copy();
		ListTag pageTags = new ListTag();
		for (List<ClipboardEntry> page : entries) {
			CompoundTag pageTag = new CompoundTag();
			ListTag entryTags = new ListTag();
			for (ClipboardEntry entry : page)
				entryTags.add(entry.writeNBT());
			pageTag.put("Entries", entryTags);
			pageTags.add(pageTag);
		}
		data.put("Pages", pageTags);
		clipboardItem.set(AllDataComponents.CLIPBOARD_DATA, data);
	}

	public CompoundTag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("Checked", checked);
		nbt.store("Text", ComponentSerialization.CODEC, text);
		if (icon.isEmpty())
			return nbt;
		nbt.putString("Icon", BuiltInRegistries.ITEM.getKey(icon.getItem()).toString());
		nbt.putInt("ItemAmount", itemAmount);
		return nbt;
	}

	public static ClipboardEntry readNBT(CompoundTag tag) {
		MutableComponent text = tag.read("Text", ComponentSerialization.CODEC)
			.map(Component::copy)
			.orElseGet(Component::empty);
		ClipboardEntry entry = new ClipboardEntry(tag.getBooleanOr("Checked", false), text);
		String itemId = tag.getStringOr("Icon", "");
		Identifier id = Identifier.tryParse(itemId);
		if (id != null) {
			Item item = BuiltInRegistries.ITEM.getValue(id);
			if (item != null)
				entry.displayItem(new ItemStack(item), tag.getIntOr("ItemAmount", 0));
		}
		return entry;
	}
}
