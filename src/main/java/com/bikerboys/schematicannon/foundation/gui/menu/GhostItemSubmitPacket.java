package com.bikerboys.schematicannon.foundation.gui.menu;

import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;
import com.bikerboys.schematicannon.foundation.networking.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GhostItemSubmitPacket extends SimplePacketBase {

	private final ItemStack item;
	private final int slot;

	public GhostItemSubmitPacket(ItemStack item, int slot) {
		this.item = item;
		this.slot = slot;
	}

	public GhostItemSubmitPacket(FriendlyByteBuf buffer) {
		item = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
		slot = buffer.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, item);
		buffer.writeInt(slot);
	}

	@Override
	public boolean handle(PacketContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;

			if (player.containerMenu instanceof GhostItemMenu<?> menu) {
				menu.ghostInventory.setStackInSlot(slot, item);
				menu.getSlot(36 + slot)
					.setChanged();
			}
		});
		return true;
	}

}
