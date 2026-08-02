package com.bikerboys.schematicannon.foundation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.bikerboys.schematicannon.AllPackets;

public abstract class SimplePacketBase implements CustomPacketPayload {

	public abstract void write(FriendlyByteBuf buffer);

	public abstract boolean handle(PacketContext context);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return AllPackets.typeOf(getClass());
	}

}
