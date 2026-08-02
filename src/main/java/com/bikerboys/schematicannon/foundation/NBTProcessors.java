package com.bikerboys.schematicannon.foundation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Standalone schematic NBT sanitizer. This replaces the small Catnip utility
 * used by the original project without adding Create as a runtime dependency.
 */
public final class NBTProcessors {
	private static final Map<BlockEntityType<?>, UnaryOperator<CompoundTag>> PROCESSORS = new HashMap<>();

	public static void addProcessor(BlockEntityType<?> type, UnaryOperator<CompoundTag> processor) {
		PROCESSORS.put(type, processor);
	}

	@Nullable
	public static CompoundTag process(BlockState state, BlockEntity blockEntity, @Nullable CompoundTag data,
									 boolean survivalMode) {
		if (data == null)
			return null;
		UnaryOperator<CompoundTag> processor = PROCESSORS.get(blockEntity.getType());
		return processor == null ? data : processor.apply(data);
	}

	public static ItemStack withUnsafeNBTDiscarded(ItemStack original) {
		ItemStack safe = original.copy();
		safe.remove(DataComponents.BLOCK_ENTITY_DATA);
		safe.remove(DataComponents.ENTITY_DATA);
		safe.remove(DataComponents.BUCKET_ENTITY_DATA);
		safe.remove(DataComponents.CONTAINER_LOOT);
		return safe;
	}

	public static boolean textComponentHasClickEvent(Component component) {
		return component.toFlatList(Style.EMPTY)
			.stream()
			.anyMatch(part -> part.getStyle().getClickEvent() != null);
	}

	/**
	 * Conservative fallback for serialized components in block-entity data.
	 * It catches both the legacy "clickEvent" and current "click_event" keys.
	 */
	public static boolean containsClickEvent(Tag tag) {
		if (tag instanceof CompoundTag compound) {
			for (Map.Entry<String, Tag> entry : compound.entrySet()) {
				String key = entry.getKey();
				if (key.equalsIgnoreCase("clickEvent") || key.equalsIgnoreCase("click_event"))
					return true;
				if (containsClickEvent(entry.getValue()))
					return true;
			}
			return false;
		}
		if (tag instanceof ListTag list) {
			for (Tag child : list)
				if (containsClickEvent(child))
					return true;
			return false;
		}
		if (tag instanceof StringTag string) {
			String value = string.value();
			return value.contains("\"clickEvent\"") || value.contains("\"click_event\"");
		}
		return false;
	}

	private NBTProcessors() {}
}
