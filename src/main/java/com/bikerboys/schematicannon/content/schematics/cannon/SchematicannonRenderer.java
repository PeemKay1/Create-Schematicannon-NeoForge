package com.bikerboys.schematicannon.content.schematics.cannon;

import java.util.ArrayList;
import java.util.List;

import com.bikerboys.schematicannon.AllPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class SchematicannonRenderer implements BlockEntityRenderer<SchematicannonBlockEntity, SchematicannonRenderer.State> {
	public SchematicannonRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(SchematicannonBlockEntity blockEntity, State state, float partialTicks,
			Vec3 cameraPosition, CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		double[] angles = getCannonAngles(blockEntity, blockEntity.getBlockPos(), partialTicks);
		state.yaw = angles[0];
		state.pitch = angles[1];
		state.recoil = getRecoil(blockEntity, partialTicks);
		state.launched.clear();
		for (LaunchedItem launched : blockEntity.flyingBlocks) {
			if (launched.ticksRemaining == 0)
				continue;
			state.launched.add(createFlyingRenderState(blockEntity, launched, partialTicks));
			if (launched.ticksRemaining >= launched.totalTicks - 1 && blockEntity.firstRenderTick) {
				spawnLaunchParticles(blockEntity, launched.target);
				blockEntity.firstRenderTick = false;
			}
		}
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(.5f, 0, .5f);
		poseStack.mulPose(Axis.YP.rotationDegrees((float) state.yaw + 90));
		poseStack.translate(-.5f, 0, -.5f);
		submitModel(AllPartialModels.SCHEMATICANNON_CONNECTOR, state, poseStack, collector);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(.5f, 15 / 16f, .5f);
		poseStack.mulPose(Axis.YP.rotationDegrees((float) state.yaw + 90));
		poseStack.mulPose(Axis.ZP.rotationDegrees((float) state.pitch));
		poseStack.translate(-.5f, -15 / 16f, -.5f);
		poseStack.translate(0, -state.recoil / 100, 0);
		submitModel(AllPartialModels.SCHEMATICANNON_PIPE, state, poseStack, collector);
		poseStack.popPose();

		for (FlyingRenderState flying : state.launched)
			submitFlyingItem(flying, poseStack, collector, state.lightCoords);
	}

	private static FlyingRenderState createFlyingRenderState(SchematicannonBlockEntity blockEntity,
			LaunchedItem launched, float partialTicks) {
		Vec3 start = Vec3.atCenterOf(blockEntity.getBlockPos().above());
		Vec3 target = Vec3.atCenterOf(launched.target);
		Vec3 distance = target.subtract(start);
		double yDifference = target.y - start.y;
		double throwHeight = Math.sqrt(distance.lengthSqr()) * .6 + yDifference;
		Vec3 cannonOffset = distance.add(0, throwHeight, 0).normalize().scale(2);
		start = start.add(cannonOffset);
		yDifference = target.y - start.y;

		float progress = ((float) launched.totalTicks
			- (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
		progress = Mth.clamp(progress, 0, 1);
		Vec3 blockLocationXZ = target.subtract(start).scale(progress).multiply(1, 0, 1);
		double yOffset = 2 * (1 - progress) * progress * throwHeight
			+ progress * progress * yDifference;
		Vec3 position = blockLocationXZ.add(.5, yOffset + 1.5, .5).add(cannonOffset);

		if (launched instanceof LaunchedItem.ForBlockState block) {
			BlockStateModel model = Minecraft.getInstance().getModelManager()
				.getBlockStateModelSet().get(block.state);
			List<BlockStateModelPart> parts = new ArrayList<>();
			model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, block.state,
				RandomSource.create(block.state.getSeed(launched.target)), parts);
			int[] tints = Minecraft.getInstance().getBlockColors().getTintSources(block.state)
				.stream().mapToInt(source -> source.color(block.state)).toArray();
			boolean translucent = model.hasMaterialFlag(
				BlockAndTintGetter.EMPTY, BlockPos.ZERO, block.state, 1);
			return new FlyingRenderState(position, progress, List.copyOf(parts), tints,
				translucent, null);
		}

		ItemStackRenderState itemState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(
			itemState, launched.stack, ItemDisplayContext.GROUND,
			blockEntity.getLevel(), null, 0);
		return new FlyingRenderState(position, progress, List.of(), new int[0], false, itemState);
	}

	private static void submitFlyingItem(FlyingRenderState flying, PoseStack poseStack,
			SubmitNodeCollector collector, int light) {
		poseStack.pushPose();
		poseStack.translate(flying.position.x, flying.position.y, flying.position.z);
		poseStack.translate(.125, .125, .125);
		poseStack.mulPose(Axis.YP.rotationDegrees(360 * flying.progress));
		poseStack.mulPose(Axis.XP.rotationDegrees(360 * flying.progress));
		poseStack.translate(-.125, -.125, -.125);

		if (flying.itemState != null) {
			poseStack.scale(1.2f, 1.2f, 1.2f);
			flying.itemState.submit(poseStack, collector, light,
				OverlayTexture.NO_OVERLAY, 0);
		} else if (!flying.parts.isEmpty()) {
			poseStack.scale(.3f, .3f, .3f);
			collector.submitCustomGeometry(poseStack,
				flying.translucent ? RenderTypes.translucentMovingBlock()
					: RenderTypes.cutoutMovingBlock(),
				(pose, consumer) -> renderFlyingBlock(flying, pose, consumer));
		}
		poseStack.popPose();
	}

	private static void renderFlyingBlock(FlyingRenderState flying, PoseStack.Pose pose,
			VertexConsumer consumer) {
		renderParts(flying.parts, flying.tints, LightCoordsUtil.FULL_BRIGHT, pose, consumer);
	}

	private static void renderParts(List<BlockStateModelPart> parts, int[] tints, int light,
			PoseStack.Pose pose, VertexConsumer consumer) {
		QuadInstance instance = new QuadInstance();
		instance.setLightCoords(light);
		instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
		for (BlockStateModelPart part : parts) {
			for (Direction direction : Direction.values())
				renderQuads(part.getQuads(direction), tints, pose, consumer, instance);
			renderQuads(part.getQuads(null), tints, pose, consumer, instance);
		}
	}

	private static void renderQuads(List<BakedQuad> quads, int[] tints, PoseStack.Pose pose,
			VertexConsumer consumer, QuadInstance instance) {
		for (BakedQuad quad : quads) {
			int tintIndex = quad.materialInfo().tintIndex();
			instance.setColor(tintIndex >= 0 && tintIndex < tints.length
				? tints[tintIndex] : 0xFFFFFFFF);
			consumer.putBakedQuad(pose, quad, instance);
		}
	}

	private static void spawnLaunchParticles(SchematicannonBlockEntity blockEntity, BlockPos targetPos) {
		Vec3 start = Vec3.atCenterOf(blockEntity.getBlockPos().above());
		Vec3 target = Vec3.atCenterOf(targetPos);
		Vec3 distance = target.subtract(start);
		double throwHeight = Math.sqrt(distance.lengthSqr()) * .6 + target.y - start.y;
		Vec3 cannonOffset = distance.add(0, throwHeight, 0).normalize().scale(2);
		Vec3 particleStart = start.add(cannonOffset).subtract(.5, .5, .5);
		RandomSource random = blockEntity.getLevel().getRandom();
		for (int i = 0; i < 10; i++) {
			double speedX = cannonOffset.x * .01;
			double speedY = (cannonOffset.y + 1) * .01;
			double speedZ = cannonOffset.z * .01;
			blockEntity.getLevel().addParticle(ParticleTypes.CLOUD,
				particleStart.x + random.nextFloat() - speedX * 40,
				particleStart.y + random.nextFloat() - speedY * 40,
				particleStart.z + random.nextFloat() - speedZ * 40,
				speedX, speedY, speedZ);
		}
	}

	private static void submitModel(net.neoforged.neoforge.client.model.standalone.StandaloneModelKey<BlockStateModel> key,
			State state, PoseStack poseStack, SubmitNodeCollector collector) {
		BlockStateModel model = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(0), parts);
		List<BlockStateModelPart> immutableParts = List.copyOf(parts);
		collector.submitCustomGeometry(poseStack, RenderTypes.cutoutMovingBlock(),
			(pose, consumer) -> renderParts(immutableParts, new int[0],
				state.lightCoords, pose, consumer));
	}

	public static double[] getCannonAngles(SchematicannonBlockEntity blockEntity, BlockPos pos, float partialTicks) {
		BlockPos target = blockEntity.printer.getCurrentTarget();
		if (target == null)
			return new double[] { blockEntity.defaultYaw, 40 };

		Vec3 diff = Vec3.atLowerCornerOf(target.subtract(pos));
		if (blockEntity.previousTarget != null)
			diff = Vec3.atLowerCornerOf(blockEntity.previousTarget)
				.add(Vec3.atLowerCornerOf(target.subtract(blockEntity.previousTarget)).scale(partialTicks))
				.subtract(Vec3.atLowerCornerOf(pos));
		double yaw = Mth.atan2(diff.x(), diff.z()) / Math.PI * 180;
		float distance = Mth.sqrt((float) (diff.x() * diff.x() + diff.z() * diff.z()));
		double pitch = Mth.atan2(distance, diff.y() * 3 + distance * 2f) / Math.PI * 180 + 10;
		return new double[] { yaw, pitch };
	}

	public static double getRecoil(SchematicannonBlockEntity blockEntity, float partialTicks) {
		double recoil = 0;
		for (LaunchedItem launched : blockEntity.flyingBlocks) {
			if (launched.ticksRemaining != 0 && launched.ticksRemaining + 1 - partialTicks > launched.totalTicks - 10)
				recoil = Math.max(recoil, launched.ticksRemaining + 1 - partialTicks - launched.totalTicks + 10);
		}
		return recoil;
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 128;
	}

	public static class State extends BlockEntityRenderState {
		double yaw;
		double pitch;
		double recoil;
		final List<FlyingRenderState> launched = new ArrayList<>();
	}

	private record FlyingRenderState(Vec3 position, float progress,
			List<BlockStateModelPart> parts, int[] tints, boolean translucent,
			ItemStackRenderState itemState) {}
}
