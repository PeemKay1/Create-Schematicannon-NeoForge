package com.bikerboys.schematicannon.content.schematics;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

/**
 * Build-only smoke test for real .litematic files. This class is excluded from
 * the distributable JAR.
 */
public final class LitematicImporterSmokeTest {

	private LitematicImporterSmokeTest() {
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1)
			throw new IllegalArgumentException("Expected a schematics directory");
		Path directory = Path.of(args[0]);
		int tested = 0;

		try (var files = Files.walk(directory)) {
			for (Path file : files.filter(Files::isRegularFile)
				.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".litematic"))
				.sorted()
				.toList()) {
				CompoundTag source;
				try (DataInputStream input = new DataInputStream(new BufferedInputStream(
					new GZIPInputStream(Files.newInputStream(file))))) {
					source = NbtIo.read(input, NbtAccounter.create(0x20000000L));
				}
				int sourceDataVersion = source.getIntOr("MinecraftDataVersion", 1343);
				CompoundTag converted = LitematicImporter.convert(source, null, sourceDataVersion);
				var sizeTag = converted.getListOrEmpty("size");
				int[] size = {
					sizeTag.getIntOr(0, 0),
					sizeTag.getIntOr(1, 0),
					sizeTag.getIntOr(2, 0)
				};
				if (size[0] <= 0 || size[1] <= 0 || size[2] <= 0)
					throw new AssertionError("Invalid converted size for " + file);
				if (converted.getListOrEmpty("palette").isEmpty())
					throw new AssertionError("Empty converted palette for " + file);
				if (converted.getListOrEmpty("blocks").isEmpty())
					throw new AssertionError("No converted blocks for " + file);
				System.out.printf("OK %s -> %dx%dx%d, %d blocks, %d states%n",
					file.getFileName(), size[0], size[1], size[2],
					converted.getListOrEmpty("blocks").size(),
					converted.getListOrEmpty("palette").size());
				tested++;
			}
		}
		if (tested == 0)
			throw new AssertionError("No .litematic files found in " + directory);
		System.out.println("Successfully converted " + tested + " Litematica schematic(s)");
	}
}
