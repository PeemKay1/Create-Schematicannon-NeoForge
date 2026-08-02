package com.bikerboys.schematicannon;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The standalone mod's creative inventory tab.
 */
public final class AllCreativeModeTabs {
	public static final DeferredRegister<CreativeModeTab> TABS =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Schematicannon.ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
		() -> CreativeModeTab.builder()
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

	public static void register(IEventBus eventBus) {
		TABS.register(eventBus);
	}

	private AllCreativeModeTabs() {}
}
