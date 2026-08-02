package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.equipment.clipboard.ClipboardBlock;
import com.bikerboys.schematicannon.content.equipment.clipboard.ClipboardBlockItem;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonBlock;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableBlock;
import com.bikerboys.schematicannon.foundation.registry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class AllBlocks {
    public static final RegistryEntry<ClipboardBlock> CLIPBOARD = block("clipboard",
        new ClipboardBlock(blockProperties("clipboard").mapColor(MapColor.WOOD).forceSolidOn()));
    public static final RegistryEntry<ClipboardBlockItem> CLIPBOARD_ITEM = item("clipboard",
        new ClipboardBlockItem(CLIPBOARD.get(), itemProperties("clipboard")));

    public static final RegistryEntry<SchematicannonBlock> SCHEMATICANNON = block("schematicannon",
        new SchematicannonBlock(blockProperties("schematicannon").mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryEntry<BlockItem> SCHEMATICANNON_ITEM = item("schematicannon",
        new BlockItem(SCHEMATICANNON.get(), itemProperties("schematicannon")));

    public static final RegistryEntry<SchematicTableBlock> SCHEMATIC_TABLE = block("schematic_table",
        new SchematicTableBlock(blockProperties("schematic_table").mapColor(MapColor.PODZOL).forceSolidOn()));
    public static final RegistryEntry<BlockItem> SCHEMATIC_TABLE_ITEM = item("schematic_table",
        new BlockItem(SCHEMATIC_TABLE.get(), itemProperties("schematic_table")));

    private static BlockBehaviour.Properties blockProperties(String name) {
        return BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, Schematicannon.asResource(name)));
    }

    private static Item.Properties itemProperties(String name) {
        return new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, Schematicannon.asResource(name)));
    }

    private static <T extends Block> RegistryEntry<T> block(String name, T block) {
        var id = Schematicannon.asResource(name);
        return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.BLOCK, id, block));
    }

    private static <T extends Item> RegistryEntry<T> item(String name, T item) {
        var id = Schematicannon.asResource(name);
        return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.ITEM, id, item));
    }

    public static void register() {
    }

    private AllBlocks() {
    }
}
