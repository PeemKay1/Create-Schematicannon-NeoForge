package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.equipment.clipboard.ClipboardBlockEntity;
import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonBlockEntity;
import com.bikerboys.schematicannon.content.schematics.table.SchematicTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllBlockEntityTypes {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,
            Schematicannon.ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SchematicannonBlockEntity>> SCHEMATICANNON =
            TYPES.register("schematicannon", () -> new BlockEntityType<SchematicannonBlockEntity>(SchematicannonBlockEntity::new,
                    AllBlocks.SCHEMATICANNON.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SchematicTableBlockEntity>> SCHEMATIC_TABLE =
            TYPES.register("schematic_table", () -> new BlockEntityType<SchematicTableBlockEntity>(SchematicTableBlockEntity::new,
                    AllBlocks.SCHEMATIC_TABLE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClipboardBlockEntity>> CLIPBOARD =
            TYPES.register("clipboard", () -> new BlockEntityType<ClipboardBlockEntity>(ClipboardBlockEntity::new, AllBlocks.CLIPBOARD.get()));

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }

    private AllBlockEntityTypes() {
    }
}
