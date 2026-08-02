package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonMenu;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Schematicannon.ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SchematicTableMenu>> SCHEMATIC_TABLE = MENUS.register(
            "schematic_table", () -> IMenuTypeExtension.create(SchematicTableMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SchematicannonMenu>> SCHEMATICANNON = MENUS.register(
            "schematicannon", () -> IMenuTypeExtension.create(SchematicannonMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    private AllMenuTypes() {
    }
}
