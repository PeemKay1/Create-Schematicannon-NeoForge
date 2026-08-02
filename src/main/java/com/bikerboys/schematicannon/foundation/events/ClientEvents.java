package com.bikerboys.schematicannon.foundation.events;

import com.bikerboys.schematicannon.SchematicannonClient;
import com.bikerboys.schematicannon.foundation.item.TooltipModifier;
import com.bikerboys.schematicannon.foundation.utility.AnimationTickHolder;
import com.bikerboys.schematicannon.foundation.utility.CameraAngleAnimationService;
import com.bikerboys.schematicannon.foundation.utility.ServerSpeedProvider;
import com.bikerboys.schematicannon.foundation.utility.TickBasedCache;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientEvents {
	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event) {
		if (!isGameActive())
			return;
		SchematicannonClient.SCHEMATIC_SENDER.tick();
		SchematicannonClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		SchematicannonClient.SCHEMATIC_HANDLER.tick();
		ServerSpeedProvider.clientTick();
		CameraAngleAnimationService.tick();
		TickBasedCache.clientTick();
	}

	@SubscribeEvent
	public static void onLoadWorld(LevelEvent.Load event) {
		LevelAccessor world = event.getLevel();
		if (world.isClientSide() && world instanceof ClientLevel) {
			SchematicannonClient.SCHEMATIC_HANDLER.clearPreview();
			SchematicannonClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	@SubscribeEvent
	public static void onUnloadWorld(LevelEvent.Unload event) {
		if (!event.getLevel().isClientSide())
			return;
		SchematicannonClient.SCHEMATIC_HANDLER.clearPreview();
		SchematicannonClient.invalidateRenderers();
		AnimationTickHolder.reset();
	}

	@SubscribeEvent
	public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
		float partialTicks = AnimationTickHolder.getPartialTicks();
		if (CameraAngleAnimationService.isYawAnimating())
			event.setYaw(CameraAngleAnimationService.getYaw(partialTicks));
		if (CameraAngleAnimationService.isPitchAnimating())
			event.setPitch(CameraAngleAnimationService.getPitch(partialTicks));
	}

	@SubscribeEvent
	public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
		var cameraState = event.getLevelRenderState().cameraRenderState;
		if (cameraState == null || cameraState.pos == null)
			return;
		SchematicannonClient.SCHEMATIC_HANDLER.submitGeometry(
			event.getPoseStack(), event.getSubmitNodeCollector(), cameraState);
	}

	@SubscribeEvent
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (event.getEntity() == null)
			return;
		Item item = event.getItemStack().getItem();
		TooltipModifier modifier = TooltipModifier.REGISTRY.get(item);
		if (modifier != null && modifier != TooltipModifier.EMPTY)
			modifier.modify(event);
	}

	private static boolean isGameActive() {
		return Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
	}

	private ClientEvents() {}
}
