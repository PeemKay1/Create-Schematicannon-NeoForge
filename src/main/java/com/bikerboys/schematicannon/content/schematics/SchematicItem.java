package com.bikerboys.schematicannon.content.schematics;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;

import com.mojang.datafixers.DataFixer;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.bikerboys.schematicannon.AllItems;
import com.bikerboys.schematicannon.AllDataComponents;
import com.bikerboys.schematicannon.content.schematics.client.SchematicEditScreen;
import com.bikerboys.schematicannon.foundation.utility.CreateLang;
import com.bikerboys.schematicannon.foundation.utility.CreatePaths;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;


public class SchematicItem extends Item {

	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int PLACEMENT_FORMAT_VERSION = 1;

	public SchematicItem(Properties properties) {
		super(properties);
	}

	public static ItemStack create(Level level, String schematic, String owner) {
		ItemStack blueprint = AllItems.SCHEMATIC.get().getDefaultInstance();

		blueprint.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
		blueprint.set(AllDataComponents.SCHEMATIC_OWNER, owner);
		blueprint.set(AllDataComponents.SCHEMATIC_FILE, schematic);
		blueprint.set(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
		blueprint.set(AllDataComponents.SCHEMATIC_ROTATION, Rotation.NONE);
		blueprint.set(AllDataComponents.SCHEMATIC_MIRROR, Mirror.NONE);
		blueprint.set(AllDataComponents.SCHEMATIC_PLACEMENT_VERSION, PLACEMENT_FORMAT_VERSION);

		writeSize(level, blueprint);
		return blueprint;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flagIn) {
		String file = stack.get(AllDataComponents.SCHEMATIC_FILE);
		if (file != null) {
			tooltip.accept(Component.literal(ChatFormatting.GOLD + file));
		} else {
			tooltip.accept(CreateLang.translateDirect("schematic.invalid").withStyle(ChatFormatting.RED));
		}
		super.appendHoverText(stack, context, display, tooltip, flagIn);
	}

	public static void writeSize(Level level, ItemStack blueprint) {
		StructureTemplate t = loadSchematic(level, blueprint);
		blueprint.set(AllDataComponents.SCHEMATIC_BOUNDS, t.getSize());
		SchematicInstances.clearHash(blueprint);
	}

	public static StructurePlaceSettings getSettings(ItemStack blueprint) {
		return getSettings(blueprint, true);
	}

	public static StructurePlaceSettings getSettings(ItemStack blueprint, boolean processNBT) {
		StructurePlaceSettings settings = new StructurePlaceSettings();
		settings.setRotation(blueprint.getOrDefault(AllDataComponents.SCHEMATIC_ROTATION, Rotation.NONE));
		settings.setMirror(blueprint.getOrDefault(AllDataComponents.SCHEMATIC_MIRROR, Mirror.NONE));
		if (processNBT)
			settings.addProcessor(SchematicProcessor.INSTANCE);
		return settings;
	}

	public static StructureTemplate loadSchematic(Level level, ItemStack blueprint) {
		StructureTemplate t = new StructureTemplate();
		String owner = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_OWNER, "");
		String schematic = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_FILE, "");

		String lowerSchematicName = schematic.toLowerCase(java.util.Locale.ROOT);
		if (!lowerSchematicName.endsWith(".nbt")
			&& !lowerSchematicName.endsWith(LitematicImporter.EXTENSION))
			return t;

		Path dir;
		Path file;

		if (!level.isClientSide()) {
			dir = CreatePaths.UPLOADED_SCHEMATICS_DIR;
			file = Paths.get(owner, schematic);
		} else {
			dir = CreatePaths.SCHEMATICS_DIR;
			file = Paths.get(schematic);
		}

		Path path = dir.resolve(file).normalize();
		if (!path.startsWith(dir))
			return t;

		try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
			new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
			CompoundTag nbt = NbtIo.read(stream, NbtAccounter.create(0x20000000L));
			if (lowerSchematicName.endsWith(LitematicImporter.EXTENSION)) {
				DataFixer dataFixer = level.getServer() != null
					? level.getServer().getFixerUpper()
					: Minecraft.getInstance().getFixerUpper();
				nbt = LitematicImporter.convert(nbt, dataFixer);
			}
			t.load(level.holderLookup(Registries.BLOCK), nbt);
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("Failed to read schematic", e);
		}

		return t;
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() != null && !onItemUse(context.getPlayer(), context.getHand()))
			return super.useOn(context);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (!onItemUse(playerIn, handIn))
			return super.use(worldIn, playerIn, handIn);
		return InteractionResult.SUCCESS;
	}

	private boolean onItemUse(Player player, InteractionHand hand) {
		if (!player.isShiftKeyDown() || hand != InteractionHand.MAIN_HAND)
			return false;
		if (!player.getItemInHand(hand).has(AllDataComponents.SCHEMATIC_FILE))
			return false;
		if (!player.level().isClientSide())
			return true;
		displayBlueprintScreen();
		return true;
	}

	protected void displayBlueprintScreen() {
		Minecraft.getInstance().setScreenAndShow(new SchematicEditScreen());
	}

}
