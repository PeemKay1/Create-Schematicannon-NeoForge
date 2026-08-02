package com.bikerboys.schematicannon.content.schematics.client;

import java.util.ArrayList;
import java.util.List;

import com.bikerboys.schematicannon.Schematicannon;
import com.bikerboys.schematicannon.foundation.render.SuperRenderTypeBuffer;
import com.bikerboys.schematicannon.foundation.virtualWorld.SchematicLevel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SchematicRenderer {
	private boolean active;
	private boolean changed;
	protected SchematicLevel schematic;
	private final List<PreviewBlock> blocks = new ArrayList<>();

	public void display(SchematicLevel world) {
		schematic = world;
		active = true;
		changed = true;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void update() {
		changed = true;
	}

	public void render(PoseStack poseStack, SuperRenderTypeBuffer buffers) {
		if (!active || schematic == null)
			return;
		// Geometry is submitted by the 26.2 level-render-state bridge.
	}

	/**
	 * Submits the schematic through Minecraft 26.2's render-state pipeline.
	 * Uses the client block-model state directly. MovingBlockRenderState is meant
	 * for pistons and only exposes one block to its lighting/model resolver; on
	 * 26.2 that path silently produced an empty preview for schematic worlds.
	 */
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		if (!active || schematic == null)
			return;
		if (changed)
			rebuildRenderStates();

		for (PreviewBlock block : blocks) {
			BlockPos pos = block.pos();
			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
			collector.submitCustomGeometry(poseStack,
				block.hasTranslucency() ? RenderTypes.translucentMovingBlock() : RenderTypes.cutoutMovingBlock(),
				(pose, consumer) -> renderBlock(block, pose, consumer));
			poseStack.popPose();
		}

		for (BlockEntity blockEntity : schematic.getRenderedBlockEntities())
			submitBlockEntity(blockEntity, poseStack, collector, cameraState);
	}

	private static void renderBlock(PreviewBlock block, PoseStack.Pose pose, VertexConsumer consumer) {
		QuadInstance quadInstance = new QuadInstance();
		quadInstance.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
		quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
		for (BlockStateModelPart part : block.parts()) {
			for (Direction direction : Direction.values())
				renderQuads(part.getQuads(direction), block.tints(), pose, consumer, quadInstance);
			renderQuads(part.getQuads(null), block.tints(), pose, consumer, quadInstance);
		}
	}

	private static void renderQuads(List<BakedQuad> quads, int[] tints, PoseStack.Pose pose,
			VertexConsumer consumer, QuadInstance quadInstance) {
		for (BakedQuad quad : quads) {
			int tintIndex = quad.materialInfo().tintIndex();
			int color = tintIndex >= 0 && tintIndex < tints.length ? tints[tintIndex] : 0xFFFFFFFF;
			quadInstance.setColor(color);
			consumer.putBakedQuad(pose, quad, quadInstance);
		}
	}

	private void rebuildRenderStates() {
		blocks.clear();
		int nonAirBlocks = 0;
		int modelBlocks = 0;
		schematic.renderMode = true;
		try {
			for (var entry : schematic.getBlockMap().entrySet()) {
				BlockPos pos = entry.getKey();
				BlockState state = entry.getValue();
				if (state.isAir())
					continue;
				nonAirBlocks++;
				if (state.getRenderShape() != RenderShape.MODEL)
					continue;
				modelBlocks++;

				BlockStateModel model = Minecraft.getInstance()
					.getModelManager()
					.getBlockStateModelSet()
					.get(state);
				List<BlockStateModelPart> parts = new ArrayList<>();
				model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state,
					RandomSource.create(state.getSeed(pos)), parts);
				if (parts.isEmpty())
					continue;

				int[] tints = Minecraft.getInstance()
					.getBlockColors()
					.getTintSources(state)
					.stream()
					.mapToInt(source -> source.color(state))
					.toArray();
				boolean hasTranslucency =
					model.hasMaterialFlag(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, 1);
				blocks.add(new PreviewBlock(pos.immutable(), List.copyOf(parts), tints, hasTranslucency));
			}
		} finally {
			schematic.renderMode = false;
			changed = false;
		}
		if (blocks.isEmpty())
			Schematicannon.LOGGER.warn(
				"Schematic preview contains no renderable block models (stored={}, nonAir={}, models={})",
				schematic.getBlockMap().size(), nonAirBlocks, modelBlocks);
		else
			Schematicannon.LOGGER.debug(
				"Prepared {} block models for schematic preview (stored={}, nonAir={})",
				blocks.size(), schematic.getBlockMap().size(), nonAirBlocks);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void submitBlockEntity(BlockEntity blockEntity, PoseStack poseStack,
			SubmitNodeCollector collector, CameraRenderState cameraState) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockEntityRenderer renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
		if (renderer == null)
			return;

		BlockEntityRenderState renderState = renderer.createRenderState();
		renderer.extractRenderState(blockEntity, renderState, 0, Vec3.ZERO, null);
		BlockPos pos = blockEntity.getBlockPos();
		poseStack.pushPose();
		poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
		renderer.submit(renderState, poseStack, collector, cameraState);
		poseStack.popPose();
	}

	private record PreviewBlock(BlockPos pos, List<BlockStateModelPart> parts, int[] tints,
			boolean hasTranslucency) {}
}
