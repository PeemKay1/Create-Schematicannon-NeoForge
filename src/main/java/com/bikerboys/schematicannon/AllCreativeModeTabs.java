package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.foundation.registry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * The standalone mod's creative inventory tab.
 */
public final class AllCreativeModeTabs {
	public static final RegistryEntry<CreativeModeTab> MAIN = register("main",
		CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
			.title(Component.translatable("itemGroup.schematicannon"))
			.icon(() -> new ItemStack(AllBlocks.SCHEMATICANNON_ITEM.get()))
			.displayItems((parameters, output) -> {
				output.accept(AllBlocks.SCHEMATICANNON_ITEM.get());
				output.accept(AllBlocks.SCHEMATIC_TABLE_ITEM.get());
				output.accept(AllBlocks.CLIPBOARD_ITEM.get());
				output.accept(AllItems.EMPTY_SCHEMATIC.get());
				output.accept(AllItems.SCHEMATIC_AND_QUILL.get());
				output.accept(AllItems.SCHEMATIC.get());
			})
			.build());

	private static RegistryEntry<CreativeModeTab> register(String name, CreativeModeTab tab) {
		var id = Schematicannon.asResource(name);
		return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab));
	}

	public static void register() {
	}

	private AllCreativeModeTabs() {}
}
