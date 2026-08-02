package com.bikerboys.schematicannon.content.schematics.client;

import java.util.List;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.bikerboys.schematicannon.AllBlocks;
import com.bikerboys.schematicannon.AllItems;
import com.bikerboys.schematicannon.AllDataComponents;
import com.bikerboys.schematicannon.AllKeys;
import com.bikerboys.schematicannon.AllPackets;
import com.bikerboys.schematicannon.Schematicannon;
import com.bikerboys.schematicannon.StructureTransform;
import com.bikerboys.schematicannon.content.schematics.SchematicInstances;
import com.bikerboys.schematicannon.content.schematics.SchematicItem;
import com.bikerboys.schematicannon.content.schematics.client.tools.ToolType;
import com.bikerboys.schematicannon.content.schematics.client.tools.SchematicToolBase;
import com.bikerboys.schematicannon.content.schematics.packet.SchematicPlacePacket;
import com.bikerboys.schematicannon.content.schematics.packet.SchematicSyncPacket;
import com.bikerboys.schematicannon.foundation.blockEntity.IMultiBlockEntityContainer;
import com.bikerboys.schematicannon.foundation.blockEntity.SmartBlockEntity;
import com.bikerboys.schematicannon.foundation.utility.CreateLang;

import com.bikerboys.schematicannon.foundation.utility.AnimationTickHolder;
import com.bikerboys.schematicannon.foundation.virtualWorld.SchematicLevel;
import com.bikerboys.schematicannon.foundation.render.AABBOutline;
import com.bikerboys.schematicannon.foundation.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import net.minecraft.client.DeltaTracker;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class SchematicHandler implements GuiLayer {

	private String displayedSchematic;
	private SchematicTransformation transformation;
	private AABB bounds;
	private boolean deployed;
	private boolean active;
	private ToolType currentTool;

	private static final int SYNC_DELAY = 10;
	private int syncCooldown;
	private int activeHotbarSlot;
	private ItemStack activeSchematicItem;
	private AABBOutline outline;

	private final Vector<SchematicRenderer> renderers;
	private final SchematicHotbarSlotOverlay overlay;
	private ToolSelectionScreen selectionScreen;

	public SchematicHandler() {
		renderers = new Vector<>(3);
		for (int i = 0; i < renderers.capacity(); i++)
			renderers.add(new SchematicRenderer());

		overlay = new SchematicHotbarSlotOverlay();
		currentTool = ToolType.DEPLOY;
		transformation = new SchematicTransformation();
	}

	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			if (active) {
				active = false;
				syncCooldown = 0;
				activeHotbarSlot = 0;
				activeSchematicItem = null;
				renderers.forEach(r -> r.setActive(false));
			}
			return;
		}

		if (activeSchematicItem != null && transformation != null)
			transformation.tick();

		LocalPlayer player = mc.player;
		ItemStack stack = findBlueprintInHand(player);
		if (stack == null) {
			active = false;
			syncCooldown = 0;
			activeHotbarSlot = 0;
			activeSchematicItem = null;
			renderers.forEach(r -> r.setActive(false));
			return;
		}

		if (!active || !stack.getOrDefault(AllDataComponents.SCHEMATIC_FILE, "")
			.equals(displayedSchematic)) {
			renderers.forEach(r -> r.setActive(false));
			init(player, stack);
		}
		if (!active)
			return;

		if (syncCooldown > 0)
			syncCooldown--;
		if (syncCooldown == 1)
			sync();

		selectionScreen.update();
		currentTool.getTool()
			.updateSelection();
	}

	private void init(LocalPlayer player, ItemStack stack) {
		loadSettings(stack);
		displayedSchematic = stack.getOrDefault(AllDataComponents.SCHEMATIC_FILE, "");
		active = true;
		if (deployed) {
			setupRenderer();
			ToolType toolBefore = currentTool;
			selectionScreen = new ToolSelectionScreen(ToolType.getTools(player.isCreative()), this::equip);
			if (toolBefore != null) {
				selectionScreen.setSelectedElement(toolBefore);
				equip(toolBefore);
			}
		} else
			selectionScreen = new ToolSelectionScreen(ImmutableList.of(ToolType.DEPLOY), this::equip);
	}

	private void setupRenderer() {
		Level clientWorld = Minecraft.getInstance().level;
		StructureTemplate schematic =
			SchematicItem.loadSchematic(clientWorld, activeSchematicItem);
		Vec3i size = schematic.getSize();
		if (size.equals(Vec3i.ZERO))
			return;

		SchematicLevel w = new SchematicLevel(clientWorld);
		SchematicLevel wMirroredFB = new SchematicLevel(clientWorld);
		SchematicLevel wMirroredLR = new SchematicLevel(clientWorld);
		StructurePlaceSettings placementSettings = new StructurePlaceSettings();
		StructureTransform transform;
		BlockPos pos;

		pos = BlockPos.ZERO;

		try {
			schematic.placeInWorld(w, pos, pos, placementSettings, w.getRandom(), Block.UPDATE_CLIENTS);
			for (BlockEntity blockEntity : w.getBlockEntities())
				blockEntity.setLevel(w);
			fixControllerBlockEntities(w);
		} catch (Exception e) {
			Minecraft.getInstance().player.sendSystemMessage(CreateLang.translate("schematic.error")
				.component());
			Schematicannon.LOGGER.error("Failed to load Schematic for Previewing", e);
			return;
		}

		placementSettings.setMirror(Mirror.FRONT_BACK);
		pos = BlockPos.ZERO.east(size.getX() - 1);
		schematic.placeInWorld(wMirroredFB, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.UPDATE_CLIENTS);
		transform = new StructureTransform(placementSettings.getRotationPivot(), Axis.Y, Rotation.NONE,
			placementSettings.getMirror());
		for (BlockEntity be : wMirroredFB.getRenderedBlockEntities())
			transform.apply(be);
		fixControllerBlockEntities(wMirroredFB);

		placementSettings.setMirror(Mirror.LEFT_RIGHT);
		pos = BlockPos.ZERO.south(size.getZ() - 1);
		schematic.placeInWorld(wMirroredLR, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.UPDATE_CLIENTS);
		transform = new StructureTransform(placementSettings.getRotationPivot(), Axis.Y, Rotation.NONE,
			placementSettings.getMirror());
		for (BlockEntity be : wMirroredLR.getRenderedBlockEntities())
			transform.apply(be);
		fixControllerBlockEntities(wMirroredLR);

		renderers.get(0)
			.display(w);
		renderers.get(1)
			.display(wMirroredFB);
		renderers.get(2)
			.display(wMirroredLR);
	}

	private void fixControllerBlockEntities(SchematicLevel level) {
		for (BlockEntity blockEntity : level.getBlockEntities()) {
			if (!(blockEntity instanceof IMultiBlockEntityContainer multiBlockEntity))
				continue;
			BlockPos lastKnown = multiBlockEntity.getLastKnownPos();
			BlockPos current = blockEntity.getBlockPos();
			if (lastKnown == null || current == null)
				continue;
			if (multiBlockEntity.isController())
				continue;
			if (!lastKnown.equals(current)) {
				BlockPos newControllerPos = multiBlockEntity.getController()
					.offset(current.subtract(lastKnown));
				if (multiBlockEntity instanceof SmartBlockEntity sbe)
					sbe.markVirtual();
				multiBlockEntity.setController(newControllerPos);
			}
		}
	}

	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
		boolean present = activeSchematicItem != null;
		if (!active && !present)
			return;

		if (active) {
			ms.pushPose();
			currentTool.getTool()
				.renderTool(ms, buffer, camera);
			ms.popPose();
		}

		ms.pushPose();
		transformation.applyTransformations(ms, camera);

		if (!renderers.isEmpty()) {
			float pt = AnimationTickHolder.getPartialTicks();
			boolean lr = transformation.getScaleLR()
				.getValue(pt) < 0;
			boolean fb = transformation.getScaleFB()
				.getValue(pt) < 0;
			if (lr && !fb)
				renderers.get(2)
					.render(ms, buffer);
			else if (fb && !lr)
				renderers.get(1)
					.render(ms, buffer);
			else
				renderers.get(0)
					.render(ms, buffer);
		}

		if (active)
			currentTool.getTool()
				.renderOnSchematic(ms, buffer);

		ms.popPose();

	}

	public void submitGeometry(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		if (!active || activeSchematicItem == null || transformation == null)
			return;
		Vec3 camera = cameraState.pos;

		if (active && !deployed && currentTool.getTool() instanceof SchematicToolBase tool) {
			BlockPos selectedPos = tool.getSelectedPos();
			if (selectedPos != null) {
				Vec3 center = bounds.getCenter();
				BlockPos target = selectedPos.offset(-((int) center.x), 0, -((int) center.z));
				poseStack.pushPose();
				poseStack.translate(
					target.getX() - camera.x,
					target.getY() - camera.y,
					target.getZ() - camera.z);
				collector.submitShapeOutline(
					poseStack, Shapes.create(bounds), RenderTypes.lines(),
					0xFF6886C5, 2, false);
				poseStack.popPose();
			}
			return;
		}

		poseStack.pushPose();
		transformation.applyTransformations(poseStack, camera);

		if (!renderers.isEmpty()) {
			float partialTicks = AnimationTickHolder.getPartialTicks();
			boolean leftRight = transformation.getScaleLR().getValue(partialTicks) < 0;
			boolean frontBack = transformation.getScaleFB().getValue(partialTicks) < 0;
			if (leftRight && !frontBack)
				renderers.get(2).submit(poseStack, collector, cameraState);
			else if (frontBack && !leftRight)
				renderers.get(1).submit(poseStack, collector, cameraState);
			else
				renderers.get(0).submit(poseStack, collector, cameraState);
		}
		if (deployed)
			collector.submitShapeOutline(
				poseStack, Shapes.create(bounds), RenderTypes.lines(),
				0xFF6886C5, 2, false);

		poseStack.popPose();
	}

	public void updateRenderers() {
		for (SchematicRenderer renderer : renderers) {
			renderer.update();
		}
	}

	public void clearPreview() {
		active = false;
		deployed = false;
		activeSchematicItem = null;
		displayedSchematic = null;
		syncCooldown = 0;
		renderers.forEach(renderer -> renderer.setActive(false));
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		if (!active)
			return;
		if (activeSchematicItem != null)
			this.overlay.renderOn(graphics, activeHotbarSlot);
		currentTool.getTool()
			.renderOverlay(graphics, partialTicks, width, height);
		selectionScreen.renderPassive(graphics, partialTicks);
	}

	public boolean onMouseInput(int button, boolean pressed) {
		if (!active)
			return false;
		if (!pressed || button != 1)
			return false;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.isShiftKeyDown())
			return false;
		if (mc.hitResult instanceof BlockHitResult blockRayTraceResult) {
			BlockState clickedBlock = mc.level.getBlockState(blockRayTraceResult.getBlockPos());
			if (clickedBlock.is(AllBlocks.SCHEMATICANNON.get()))
				return false;
		}
		return currentTool.getTool()
			.handleRightClick();
	}

	public void onKeyInput(int key, boolean pressed) {
		if (!active)
			return;
		if (!AllKeys.TOOL_MENU.doesModifierAndCodeMatch(key))
			return;

		if (pressed && !selectionScreen.focused)
			selectionScreen.focused = true;
		if (!pressed && selectionScreen.focused) {
			selectionScreen.focused = false;
			selectionScreen.onClose();
		}
	}

	public boolean mouseScrolled(double delta) {
		if (!active)
			return false;

		if (selectionScreen.focused) {
			selectionScreen.cycle((int) Math.signum(delta));
			return true;
		}
		if (AllKeys.ctrlDown())
			return currentTool.getTool()
				.handleMouseWheel(delta);
		return false;
	}

	private ItemStack findBlueprintInHand(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (!stack.is(AllItems.SCHEMATIC.get()))
			return null;
		if (!stack.has(AllDataComponents.SCHEMATIC_FILE))
			return null;

		activeSchematicItem = stack;
		activeHotbarSlot = player.getInventory().getSelectedSlot();
		return stack;
	}

	private boolean itemLost(Player player) {
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack candidate = player.getInventory()
				.getItem(i);
			if (!candidate.is(activeSchematicItem.getItem()))
				continue;
			if (ItemStack.matches(candidate, activeSchematicItem))
				return false;
		}
		return true;
	}

	public void markDirty() {
		syncCooldown = SYNC_DELAY;
	}

	public void sync() {
		if (activeSchematicItem == null)
			return;
		AllPackets.getChannel().sendToServer(new SchematicSyncPacket(activeHotbarSlot, transformation.toSettings(),
			transformation.getAnchor(), deployed));
	}

	public void equip(ToolType tool) {
		this.currentTool = tool;
		currentTool.getTool()
			.init();
	}

	public void loadSettings(ItemStack blueprint) {
		BlockPos anchor = BlockPos.ZERO;
		StructurePlaceSettings settings = SchematicItem.getSettings(blueprint);
		transformation = new SchematicTransformation();

		deployed = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false)
			&& blueprint.getOrDefault(AllDataComponents.SCHEMATIC_PLACEMENT_VERSION, 0)
				>= SchematicItem.PLACEMENT_FORMAT_VERSION;
		if (deployed)
			anchor = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
		Vec3i size = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_BOUNDS, Vec3i.ZERO);

		bounds = new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ());
		outline = new AABBOutline(bounds);
		outline.getParams()
			.colored(0x6886c5)
			.lineWidth(1 / 16f);
		transformation.init(anchor, settings, bounds);
	}

	public void deploy() {
		if (!deployed) {
			List<ToolType> tools = ToolType.getTools(Minecraft.getInstance().player.isCreative());
			selectionScreen = new ToolSelectionScreen(tools, this::equip);
		}
		deployed = true;
		setupRenderer();
	}

	public String getCurrentSchematicName() {
		return displayedSchematic != null ? displayedSchematic : "-";
	}

	public void printInstantly() {
		AllPackets.getChannel().sendToServer(new SchematicPlacePacket(activeSchematicItem.copy()));
		activeSchematicItem.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
		SchematicInstances.clearHash(activeSchematicItem);
		renderers.forEach(r -> r.setActive(false));
		active = false;
		markDirty();
	}

	public boolean isActive() {
		return active;
	}

	public AABB getBounds() {
		return bounds;
	}

	public SchematicTransformation getTransformation() {
		return transformation;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public ItemStack getActiveSchematicItem() {
		return activeSchematicItem;
	}

	public AABBOutline getOutline() {
		return outline;
	}

}
