package com.bikerboys.schematicannon.content.equipment.clipboard;

import com.bikerboys.schematicannon.Schematicannon;
import com.bikerboys.schematicannon.AllDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ClipboardOverrides {

	public enum ClipboardType {
		EMPTY("empty_clipboard"), WRITTEN("clipboard"), EDITING("clipboard_and_quill");

		public String file;
		public static Identifier ID = Schematicannon.asResource("clipboard_type");

		ClipboardType(String file) {
			this.file = file;
		}
	}

	public static void switchTo(ClipboardType type, ItemStack clipboardItem) {
		CompoundTag tag = clipboardItem.getOrDefault(AllDataComponents.CLIPBOARD_DATA, new CompoundTag()).copy();
		tag.putInt("Type", type.ordinal());
		clipboardItem.set(AllDataComponents.CLIPBOARD_DATA, tag);
	}

	public static void registerModelOverridesClient(ClipboardBlockItem item) {
		// Item model properties were replaced by the 26.2 item-model system.
		// The default clipboard model remains usable until the dedicated model port lands.
	}

}
