package com.bikerboys.schematicannon.content.schematics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.DataFixer;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;

/**
 * Converts Litematica's GZIP-compressed NBT format into a vanilla structure
 * template tag. Keeping the conversion in memory lets the schematic table,
 * preview and schematicannon use the original .litematic file directly.
 */
public final class LitematicImporter {

	public static final String EXTENSION = ".litematic";
	private static final int MIN_VERSION = 1;
	private static final int MAX_VERSION = 7;
	private static final int MINECRAFT_1_12_2_DATA_VERSION = 1343;
	private static final long MAX_VOLUME = 64L * 1024L * 1024L;

	private LitematicImporter() {
	}

	public static CompoundTag convert(CompoundTag source, DataFixer dataFixer) throws IOException {
		return convert(source, dataFixer,
			SharedConstants.getCurrentVersion().dataVersion().version());
	}

	static CompoundTag convert(CompoundTag source, DataFixer dataFixer, int currentDataVersion) throws IOException {
		int version = source.getIntOr("Version", 0);
		if (version < MIN_VERSION || version > MAX_VERSION)
			throw new IOException("Unsupported Litematica schematic version: " + version);

		CompoundTag regionsTag = source.getCompound("Regions")
			.orElseThrow(() -> new IOException("Litematica schematic has no Regions tag"));
		List<Region> regions = readRegions(regionsTag);
		if (regions.isEmpty())
			throw new IOException("Litematica schematic contains no regions");

		Bounds bounds = findBounds(regions);
		long volume = checkedVolume(bounds.sizeX(), bounds.sizeY(), bounds.sizeZ());
		if (volume > MAX_VOLUME)
			throw new IOException("Litematica schematic is too large: " + volume + " blocks");

		ListTag outputPalette = new ListTag();
		Map<CompoundTag, Integer> paletteIds = new HashMap<>();
		Map<Position, ConvertedBlock> blocks = new LinkedHashMap<>();
		ListTag entities = new ListTag();

		for (Region region : regions) {
			ListTag localPalette = region.tag().getListOrEmpty("BlockStatePalette");
			if (localPalette.isEmpty())
				throw new IOException("Region '" + region.name() + "' has an empty block palette");

			int[] paletteRemap = new int[localPalette.size()];
			boolean[] airPalette = new boolean[localPalette.size()];
			for (int i = 0; i < localPalette.size(); i++) {
				CompoundTag state = localPalette.getCompound(i)
					.orElseThrow(() -> new IOException("Invalid palette entry in region '" + region.name() + "'"));
				CompoundTag stateCopy = state.copy();
				Integer outputId = paletteIds.get(stateCopy);
				if (outputId == null) {
					outputId = outputPalette.size();
					outputPalette.add(stateCopy);
					paletteIds.put(stateCopy, outputId);
				}
				paletteRemap[i] = outputId;
				airPalette[i] = isAir(state);
			}

			long[] packedStates = region.tag().getLongArray("BlockStates")
				.orElseThrow(() -> new IOException("Region '" + region.name() + "' has no BlockStates array"));
			int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(localPalette.size() - 1));
			long localVolume = checkedVolume(region.sizeX(), region.sizeY(), region.sizeZ());
			long requiredLongs = (localVolume * bits + 63L) >>> 6;
			if (packedStates.length < requiredLongs)
				throw new IOException("Truncated BlockStates array in region '" + region.name() + "'");

			Map<Position, CompoundTag> blockEntities = readBlockEntities(region.tag(), version);
			long index = 0;
			for (int y = 0; y < region.sizeY(); y++) {
				for (int z = 0; z < region.sizeZ(); z++) {
					for (int x = 0; x < region.sizeX(); x++, index++) {
						int localPaletteId = getPacked(packedStates, index, bits);
						if (localPaletteId < 0 || localPaletteId >= localPalette.size())
							throw new IOException("Invalid palette index " + localPaletteId
								+ " in region '" + region.name() + "'");

						Position outputPos = new Position(
							region.minX() + x - bounds.minX(),
							region.minY() + y - bounds.minY(),
							region.minZ() + z - bounds.minZ());
						if (airPalette[localPaletteId]) {
							// Deterministic behavior for overlapping regions: later regions
							// in name order can also clear an earlier region's block.
							blocks.remove(outputPos);
							continue;
						}

						CompoundTag blockEntity = blockEntities.get(new Position(x, y, z));
						blocks.put(outputPos, new ConvertedBlock(
							paletteRemap[localPaletteId],
							sanitizeBlockEntity(blockEntity)));
					}
				}
			}

			appendEntities(region, bounds, version, entities);
		}

		CompoundTag structure = new CompoundTag();
		structure.put("size", intList(bounds.sizeX(), bounds.sizeY(), bounds.sizeZ()));
		structure.put("palette", outputPalette);
		structure.put("blocks", writeBlocks(blocks));
		structure.put("entities", entities);

		int sourceDataVersion = source.getIntOr("MinecraftDataVersion",
			version < 5 ? MINECRAFT_1_12_2_DATA_VERSION : currentDataVersion);
		structure.putInt("DataVersion", sourceDataVersion);

		if (dataFixer != null && sourceDataVersion > 0 && sourceDataVersion < currentDataVersion)
			structure = DataFixTypes.STRUCTURE.updateToCurrentVersion(dataFixer, structure, sourceDataVersion);

		return structure;
	}

	private static List<Region> readRegions(CompoundTag regionsTag) throws IOException {
		List<Region> regions = new ArrayList<>();
		List<String> names = new ArrayList<>(regionsTag.keySet());
		names.sort(Comparator.naturalOrder());

		for (String name : names) {
			CompoundTag tag = regionsTag.getCompound(name).orElse(null);
			if (tag == null)
				continue;
			Position position = readPosition(tag.getCompoundOrEmpty("Position"));
			Position signedSize = readPosition(tag.getCompoundOrEmpty("Size"));
			if (signedSize.x() == 0 || signedSize.y() == 0 || signedSize.z() == 0
				|| signedSize.x() == Integer.MIN_VALUE
				|| signedSize.y() == Integer.MIN_VALUE
				|| signedSize.z() == Integer.MIN_VALUE)
				throw new IOException("Invalid size in Litematica region '" + name + "'");

			int endX = Math.addExact(position.x(), signedSize.x() > 0 ? signedSize.x() - 1 : signedSize.x() + 1);
			int endY = Math.addExact(position.y(), signedSize.y() > 0 ? signedSize.y() - 1 : signedSize.y() + 1);
			int endZ = Math.addExact(position.z(), signedSize.z() > 0 ? signedSize.z() - 1 : signedSize.z() + 1);
			regions.add(new Region(name, tag,
				Math.min(position.x(), endX), Math.min(position.y(), endY), Math.min(position.z(), endZ),
				Math.max(position.x(), endX), Math.max(position.y(), endY), Math.max(position.z(), endZ),
				Math.abs(signedSize.x()), Math.abs(signedSize.y()), Math.abs(signedSize.z()),
				position));
		}
		return regions;
	}

	private static Bounds findBounds(List<Region> regions) throws IOException {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (Region region : regions) {
			minX = Math.min(minX, region.minX());
			minY = Math.min(minY, region.minY());
			minZ = Math.min(minZ, region.minZ());
			maxX = Math.max(maxX, region.maxX());
			maxY = Math.max(maxY, region.maxY());
			maxZ = Math.max(maxZ, region.maxZ());
		}
		try {
			return new Bounds(minX, minY, minZ,
				Math.addExact(Math.subtractExact(maxX, minX), 1),
				Math.addExact(Math.subtractExact(maxY, minY), 1),
				Math.addExact(Math.subtractExact(maxZ, minZ), 1));
		} catch (ArithmeticException e) {
			throw new IOException("Litematica schematic bounds overflow", e);
		}
	}

	private static long checkedVolume(int x, int y, int z) throws IOException {
		try {
			return Math.multiplyExact(Math.multiplyExact((long) x, (long) y), (long) z);
		} catch (ArithmeticException e) {
			throw new IOException("Litematica schematic volume overflow", e);
		}
	}

	private static int getPacked(long[] data, long index, int bits) {
		long mask = (1L << bits) - 1L;
		long startOffset = index * bits;
		int startArrayIndex = (int) (startOffset >>> 6);
		int endArrayIndex = (int) (((index + 1L) * bits - 1L) >>> 6);
		int startBitOffset = (int) (startOffset & 63L);
		if (startArrayIndex == endArrayIndex)
			return (int) (data[startArrayIndex] >>> startBitOffset & mask);
		int firstPartBits = 64 - startBitOffset;
		return (int) ((data[startArrayIndex] >>> startBitOffset
			| data[endArrayIndex] << firstPartBits) & mask);
	}

	private static boolean isAir(CompoundTag state) {
		String name = state.getStringOr("Name", "");
		return name.equals("minecraft:air")
			|| name.equals("minecraft:cave_air")
			|| name.equals("minecraft:void_air");
	}

	private static Map<Position, CompoundTag> readBlockEntities(CompoundTag region, int version) {
		Map<Position, CompoundTag> result = new HashMap<>();
		for (Tag element : region.getListOrEmpty("TileEntities")) {
			if (!(element instanceof CompoundTag entry))
				continue;
			Position pos = readPosition(entry);
			CompoundTag data = version == 1
				? entry.getCompound("TileNBT").map(CompoundTag::copy).orElse(null)
				: entry.copy();
			if (data != null && !data.isEmpty())
				result.put(pos, data);
		}
		return result;
	}

	private static CompoundTag sanitizeBlockEntity(CompoundTag source) {
		if (source == null)
			return null;
		CompoundTag copy = source.copy();
		copy.remove("x");
		copy.remove("y");
		copy.remove("z");
		return copy;
	}

	private static ListTag writeBlocks(Map<Position, ConvertedBlock> blocks) {
		ListTag result = new ListTag();
		blocks.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				CompoundTag block = new CompoundTag();
				block.put("pos", intList(entry.getKey().x(), entry.getKey().y(), entry.getKey().z()));
				block.putInt("state", entry.getValue().paletteId());
				if (entry.getValue().blockEntity() != null)
					block.put("nbt", entry.getValue().blockEntity());
				result.add(block);
			});
		return result;
	}

	private static void appendEntities(Region region, Bounds bounds, int version, ListTag output) {
		for (Tag element : region.tag().getListOrEmpty("Entities")) {
			if (!(element instanceof CompoundTag entry))
				continue;

			CompoundTag entityNbt = version == 1
				? entry.getCompound("EntityData").map(CompoundTag::copy).orElse(null)
				: entry.copy();
			if (entityNbt == null || entityNbt.isEmpty())
				continue;

			double[] localPos = readEntityPosition(version == 1 ? entry : entityNbt);
			if (localPos == null)
				continue;
			double x = region.origin().x() + localPos[0] - bounds.minX();
			double y = region.origin().y() + localPos[1] - bounds.minY();
			double z = region.origin().z() + localPos[2] - bounds.minZ();

			entityNbt.put("Pos", doubleList(x, y, z));
			shiftHangingEntityPosition(entityNbt, region.origin(), bounds);

			CompoundTag wrapper = new CompoundTag();
			wrapper.put("pos", doubleList(x, y, z));
			wrapper.put("blockPos", intList(floor(x), floor(y), floor(z)));
			wrapper.put("nbt", entityNbt);
			output.add(wrapper);
		}
	}

	private static double[] readEntityPosition(CompoundTag tag) {
		ListTag pos = tag.getListOrEmpty("Pos");
		if (pos.size() >= 3)
			return new double[] {
				pos.getDoubleOr(0, 0),
				pos.getDoubleOr(1, 0),
				pos.getDoubleOr(2, 0)
			};
		if (tag.contains("x") && tag.contains("y") && tag.contains("z"))
			return new double[] {
				tag.getDoubleOr("x", 0),
				tag.getDoubleOr("y", 0),
				tag.getDoubleOr("z", 0)
			};
		return null;
	}

	private static void shiftHangingEntityPosition(CompoundTag entity, Position origin, Bounds bounds) {
		if (!entity.contains("TileX") || !entity.contains("TileY") || !entity.contains("TileZ"))
			return;
		entity.putInt("TileX", origin.x() + entity.getIntOr("TileX", 0) - bounds.minX());
		entity.putInt("TileY", origin.y() + entity.getIntOr("TileY", 0) - bounds.minY());
		entity.putInt("TileZ", origin.z() + entity.getIntOr("TileZ", 0) - bounds.minZ());
	}

	private static Position readPosition(CompoundTag tag) {
		return new Position(
			tag.getIntOr("x", 0),
			tag.getIntOr("y", 0),
			tag.getIntOr("z", 0));
	}

	private static int floor(double value) {
		int integer = (int) value;
		return value < integer ? integer - 1 : integer;
	}

	private static ListTag intList(int x, int y, int z) {
		ListTag list = new ListTag();
		list.add(IntTag.valueOf(x));
		list.add(IntTag.valueOf(y));
		list.add(IntTag.valueOf(z));
		return list;
	}

	private static ListTag doubleList(double x, double y, double z) {
		ListTag list = new ListTag();
		list.add(DoubleTag.valueOf(x));
		list.add(DoubleTag.valueOf(y));
		list.add(DoubleTag.valueOf(z));
		return list;
	}

	private record Region(String name, CompoundTag tag,
						  int minX, int minY, int minZ,
						  int maxX, int maxY, int maxZ,
						  int sizeX, int sizeY, int sizeZ,
						  Position origin) {
	}

	private record Bounds(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
	}

	private record ConvertedBlock(int paletteId, CompoundTag blockEntity) {
	}

	private record Position(int x, int y, int z) implements Comparable<Position> {
		@Override
		public int compareTo(Position other) {
			int result = Integer.compare(y, other.y);
			if (result == 0)
				result = Integer.compare(z, other.z);
			if (result == 0)
				result = Integer.compare(x, other.x);
			return result;
		}
	}
}
