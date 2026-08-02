package com.bikerboys.schematicannon;

import com.mojang.serialization.Codec;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/** Persistent item state owned by Schematicannon, independent from Create internals. */
public final class AllDataComponents {
    public static final DataComponentType<Boolean> SCHEMATIC_DEPLOYED = register("schematic_deployed",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DataComponentType<String> SCHEMATIC_OWNER = register("schematic_owner",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DataComponentType<String> SCHEMATIC_FILE = register("schematic_file",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DataComponentType<BlockPos> SCHEMATIC_ANCHOR = register("schematic_anchor",
            builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));
    public static final DataComponentType<Rotation> SCHEMATIC_ROTATION = register("schematic_rotation",
            builder -> builder.persistent(Rotation.CODEC).networkSynchronized(Rotation.STREAM_CODEC));
    public static final DataComponentType<Mirror> SCHEMATIC_MIRROR = register("schematic_mirror",
            builder -> builder.persistent(Mirror.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(Mirror.CODEC)));
    public static final DataComponentType<Vec3i> SCHEMATIC_BOUNDS = register("schematic_bounds",
            builder -> builder.persistent(Vec3i.CODEC).networkSynchronized(Vec3i.STREAM_CODEC));
    public static final DataComponentType<Integer> SCHEMATIC_HASH = register("schematic_hash",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));
    public static final DataComponentType<Integer> SCHEMATIC_PLACEMENT_VERSION = register("schematic_placement_version",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    // Clipboard pages are kept in one namespaced component while its GUI is migrated.
    public static final DataComponentType<CompoundTag> CLIPBOARD_DATA = register("clipboard_data",
            builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> factory) {
        DataComponentType<T> type = factory.apply(DataComponentType.builder()).build();
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Schematicannon.asResource(name), type);
    }

    public static void register() {
    }

    private AllDataComponents() {
    }
}
