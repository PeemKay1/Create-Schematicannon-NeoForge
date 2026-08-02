package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.equipment.clipboard.ClipboardBlockEntity;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonBlockEntity;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableBlockEntity;
import com.bikerboys.schematicannon.foundation.registry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Set;

public final class AllBlockEntityTypes {
    public static final RegistryEntry<BlockEntityType<SchematicannonBlockEntity>> SCHEMATICANNON =
        register("schematicannon", new BlockEntityType<>(SchematicannonBlockEntity::new, Set.of(AllBlocks.SCHEMATICANNON.get())));
    public static final RegistryEntry<BlockEntityType<SchematicTableBlockEntity>> SCHEMATIC_TABLE =
        register("schematic_table", new BlockEntityType<>(SchematicTableBlockEntity::new, Set.of(AllBlocks.SCHEMATIC_TABLE.get())));
    public static final RegistryEntry<BlockEntityType<ClipboardBlockEntity>> CLIPBOARD =
        register("clipboard", new BlockEntityType<>(ClipboardBlockEntity::new, Set.of(AllBlocks.CLIPBOARD.get())));

    private static <T extends BlockEntityType<?>> RegistryEntry<T> register(String name, T type) {
        var id = Schematicannon.asResource(name);
        return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type));
    }

    public static void register() {
    }

    private AllBlockEntityTypes() {
    }
}
