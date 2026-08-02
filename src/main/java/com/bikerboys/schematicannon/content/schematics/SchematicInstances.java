package com.bikerboys.schematicannon.content.schematics;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.bikerboys.schematicannon.StructureTransform;
import com.bikerboys.schematicannon.AllDataComponents;

import com.bikerboys.schematicannon.foundation.virtualWorld.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SchematicInstances {

	private static final Map<Level, Cache<Integer, SchematicLevel>> LOADED_SCHEMATICS = new IdentityHashMap<>();

	@Nullable
	public static SchematicLevel get(Level world, ItemStack schematic) {
		Cache<Integer, SchematicLevel> map;
		synchronized (LOADED_SCHEMATICS) {
			map = LOADED_SCHEMATICS.computeIfAbsent(world, $ -> CacheBuilder.newBuilder()
				.expireAfterAccess(5, TimeUnit.MINUTES)
				.build());
		}
		int hash = getHash(schematic);
		SchematicLevel ifPresent = map.getIfPresent(hash);
		if (ifPresent != null)
			return ifPresent;
		SchematicLevel loadWorld = loadWorld(world, schematic);
		if (loadWorld == null)
			return null;
		map.put(hash, loadWorld);
		return loadWorld;
	}

	public static void invalidate(Level world) {
		synchronized (LOADED_SCHEMATICS) {
			Cache<Integer, SchematicLevel> removed = LOADED_SCHEMATICS.remove(world);
			if (removed != null)
				removed.invalidateAll();
		}
	}

	private static SchematicLevel loadWorld(Level wrapped, ItemStack schematic) {
		if (schematic == null || !schematic.has(AllDataComponents.SCHEMATIC_FILE))
			return null;
		if (!schematic.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false))
			return null;

		StructureTemplate activeTemplate =
			SchematicItem.loadSchematic(wrapped, schematic);

		if (activeTemplate.getSize()
			.equals(Vec3i.ZERO))
			return null;

		BlockPos anchor = schematic.getOrDefault(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
		SchematicLevel world = new SchematicLevel(anchor, wrapped);
		StructurePlaceSettings settings = SchematicItem.getSettings(schematic);
		activeTemplate.placeInWorld(world, anchor, anchor, settings, wrapped.getRandom(), Block.UPDATE_CLIENTS);

		StructureTransform transform = new StructureTransform(
			settings.getRotationPivot(),
			Direction.Axis.Y,
			settings.getRotation(),
			settings.getMirror());



		for (BlockEntity be : world.getBlockEntities())
			transform.apply(be);

		return world;
	}

	public static void clearHash(ItemStack schematic) {
		if (schematic == null)
			return;
		schematic.remove(AllDataComponents.SCHEMATIC_HASH);
	}

	public static int getHash(ItemStack schematic) {
		if (schematic == null || !schematic.has(AllDataComponents.SCHEMATIC_FILE))
			return -1;
		Integer hash = schematic.get(AllDataComponents.SCHEMATIC_HASH);
		if (hash == null) {
			hash = ItemStack.hashItemAndComponents(schematic);
			schematic.set(AllDataComponents.SCHEMATIC_HASH, hash);
		}
		return hash;
	}

}
