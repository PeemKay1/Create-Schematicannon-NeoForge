package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.schematics.SchematicAndQuillItem;
import com.bikerboys.schematicannon.content.schematics.SchematicItem;
import com.bikerboys.schematicannon.foundation.registry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class AllItems {
    public static final RegistryEntry<Item> EMPTY_SCHEMATIC = item("empty_schematic", Item::new);
    public static final RegistryEntry<SchematicAndQuillItem> SCHEMATIC_AND_QUILL =
        item("schematic_and_quill", SchematicAndQuillItem::new);
    public static final RegistryEntry<SchematicItem> SCHEMATIC = item("schematic", SchematicItem::new);

    private static <T extends Item> RegistryEntry<T> item(String name,
        java.util.function.Function<Item.Properties, T> factory) {
        var id = Schematicannon.asResource(name);
        var key = ResourceKey.create(Registries.ITEM, id);
        T item = factory.apply(new Item.Properties().setId(key).stacksTo(1));
        return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.ITEM, id, item));
    }

    public static void register() {
    }

    private AllItems() {
    }
}
