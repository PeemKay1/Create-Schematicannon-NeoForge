package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonMenu;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableMenu;
import com.bikerboys.schematicannon.foundation.registry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class AllMenuTypes {
    public static final RegistryEntry<MenuType<SchematicTableMenu>> SCHEMATIC_TABLE =
        register("schematic_table", new MenuType<>(SchematicTableMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryEntry<MenuType<SchematicannonMenu>> SCHEMATICANNON =
        register("schematicannon", new MenuType<>(SchematicannonMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu>
    RegistryEntry<MenuType<T>> register(String name, MenuType<T> type) {
        var id = Schematicannon.asResource(name);
        return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.MENU, id, type));
    }

    public static void register() {
    }

    private AllMenuTypes() {
    }
}
