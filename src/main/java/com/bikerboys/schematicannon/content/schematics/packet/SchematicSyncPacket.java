package com.bikerboys.schematicannon.content.schematics.packet;

import com.bikerboys.schematicannon.AllItems;
import com.bikerboys.schematicannon.AllDataComponents;
import com.bikerboys.schematicannon.content.schematics.SchematicInstances;
import com.bikerboys.schematicannon.content.schematics.SchematicItem;
import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;
import com.bikerboys.schematicannon.foundation.networking.PacketContext;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class SchematicSyncPacket extends SimplePacketBase {

	public int slot;
	public boolean deployed;
	public BlockPos anchor;
	public Rotation rotation;
	public Mirror mirror;

	public SchematicSyncPacket(int slot, StructurePlaceSettings settings,
			BlockPos anchor, boolean deployed) {
		this.slot = slot;
		this.deployed = deployed;
		this.anchor = anchor;
		this.rotation = settings.getRotation();
		this.mirror = settings.getMirror();
	}

	public SchematicSyncPacket(FriendlyByteBuf buffer) {
		slot = buffer.readVarInt();
		deployed = buffer.readBoolean();
		anchor = buffer.readBlockPos();
		rotation = buffer.readEnum(Rotation.class);
		mirror = buffer.readEnum(Mirror.class);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(slot);
		buffer.writeBoolean(deployed);
		buffer.writeBlockPos(anchor);
		buffer.writeEnum(rotation);
		buffer.writeEnum(mirror);
	}

	@Override
	public boolean handle(PacketContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			ItemStack stack = ItemStack.EMPTY;
			if (slot == -1) {
				stack = player.getMainHandItem();
			} else {
				stack = player.getInventory().getItem(slot);
			}
			if (!stack.is(AllItems.SCHEMATIC.get())) {
				return;
			}
			stack.set(AllDataComponents.SCHEMATIC_DEPLOYED, deployed);
			stack.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);
			stack.set(AllDataComponents.SCHEMATIC_ROTATION, rotation);
			stack.set(AllDataComponents.SCHEMATIC_MIRROR, mirror);
			stack.set(AllDataComponents.SCHEMATIC_PLACEMENT_VERSION,
				SchematicItem.PLACEMENT_FORMAT_VERSION);
			SchematicInstances.clearHash(stack);
		});
		return true;
	}

}
