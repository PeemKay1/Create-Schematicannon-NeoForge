package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.schematics.SchematicAndQuillItem;
import com.bikerboys.schematicannon.content.schematics.SchematicItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Schematicannon.ID);

    public static final DeferredItem<Item> EMPTY_SCHEMATIC = ITEMS.registerItem("empty_schematic", Item::new,
            properties -> properties.stacksTo(1));
    public static final DeferredItem<SchematicAndQuillItem> SCHEMATIC_AND_QUILL = ITEMS.registerItem(
            "schematic_and_quill", SchematicAndQuillItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<SchematicItem> SCHEMATIC = ITEMS.registerItem("schematic", SchematicItem::new,
            properties -> properties.stacksTo(1));

    public static void register(IEventBus eventBus) {
        ITEMS.addAlias(Identifier.fromNamespaceAndPath(Schematicannon.ID, "empty_blueprint"),
                EMPTY_SCHEMATIC.getId());
        ITEMS.addAlias(Identifier.fromNamespaceAndPath(Schematicannon.ID, "blueprint_and_quill"),
                SCHEMATIC_AND_QUILL.getId());
        ITEMS.addAlias(Identifier.fromNamespaceAndPath(Schematicannon.ID, "blueprint"),
                SCHEMATIC.getId());
        ITEMS.register(eventBus);
    }

    private AllItems() {
    }
}
