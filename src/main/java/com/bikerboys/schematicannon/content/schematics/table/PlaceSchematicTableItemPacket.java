package com.bikerboys.schematicannon.content.schematics.table;

import com.bikerboys.schematicannon.foundation.networking.PacketContext;
import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PlaceSchematicTableItemPacket extends SimplePacketBase {
	public PlaceSchematicTableItemPacket() {
	}

	public PlaceSchematicTableItemPacket(FriendlyByteBuf buffer) {
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
	}

	@Override
	public boolean handle(PacketContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null || !(player.containerMenu instanceof SchematicTableMenu menu)
				|| !menu.stillValid(player))
				return;
			menu.placeCarriedEmptySchematic();
		});
		return true;
	}
}
