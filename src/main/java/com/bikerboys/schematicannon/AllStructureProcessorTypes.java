package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.schematics.SchematicProcessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllStructureProcessorTypes {
    private static final DeferredRegister<MapCodec<? extends StructureProcessor>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Schematicannon.ID);

    public static final DeferredHolder<MapCodec<? extends StructureProcessor>, MapCodec<SchematicProcessor>> SCHEMATIC =
            PROCESSORS.register("schematic", () -> SchematicProcessor.CODEC);

    public static void register(IEventBus eventBus) {
        PROCESSORS.register(eventBus);
    }

    private AllStructureProcessorTypes() {
    }
}
