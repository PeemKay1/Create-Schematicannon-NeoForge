package com.bikerboys.schematicannon;

import com.bikerboys.schematicannon.content.schematics.SchematicProcessor;
import com.bikerboys.schematicannon.foundation.registry.RegistryEntry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public final class AllStructureProcessorTypes {
    public static final RegistryEntry<MapCodec<SchematicProcessor>> SCHEMATIC =
        register("schematic", SchematicProcessor.CODEC);

    private static RegistryEntry<MapCodec<SchematicProcessor>> register(String name, MapCodec<SchematicProcessor> codec) {
        var id = Schematicannon.asResource(name);
        return new RegistryEntry<>(id, Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, id, codec));
    }

    public static void register() {
    }

    private AllStructureProcessorTypes() {
    }
}
