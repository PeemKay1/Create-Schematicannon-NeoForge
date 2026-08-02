package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.equipment.clipboard.ClipboardBlock;
import com.bikerboys.schematicannon.content.equipment.clipboard.ClipboardBlockItem;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonBlock;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Schematicannon.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Schematicannon.ID);

    public static final DeferredBlock<ClipboardBlock> CLIPBOARD = BLOCKS.registerBlock("clipboard", ClipboardBlock::new,
            properties -> properties.mapColor(MapColor.WOOD).forceSolidOn());
    public static final DeferredItem<ClipboardBlockItem> CLIPBOARD_ITEM = ITEMS.registerItem("clipboard",
            properties -> new ClipboardBlockItem(CLIPBOARD.get(), properties));

    public static final DeferredBlock<SchematicannonBlock> SCHEMATICANNON = BLOCKS.registerBlock("schematicannon",
            SchematicannonBlock::new, properties -> properties.mapColor(MapColor.COLOR_GRAY));
    public static final DeferredItem<BlockItem> SCHEMATICANNON_ITEM = ITEMS.registerItem("schematicannon",
            properties -> new BlockItem(SCHEMATICANNON.get(), properties));

    public static final DeferredBlock<SchematicTableBlock> SCHEMATIC_TABLE = BLOCKS.registerBlock("schematic_table",
            SchematicTableBlock::new, properties -> properties.mapColor(MapColor.PODZOL).forceSolidOn());
    public static final DeferredItem<BlockItem> SCHEMATIC_TABLE_ITEM = ITEMS.registerItem("schematic_table",
            properties -> new BlockItem(SCHEMATIC_TABLE.get(), properties));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }

    private AllBlocks() {
    }
}
