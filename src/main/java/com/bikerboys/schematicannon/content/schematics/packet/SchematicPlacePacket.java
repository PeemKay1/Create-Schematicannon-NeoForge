package com.bikerboys.schematicannon.content.schematics.packet;

import com.bikerboys.schematicannon.content.schematics.SchematicPrinter;
import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;
import com.bikerboys.schematicannon.foundation.networking.PacketContext;
import com.bikerboys.schematicannon.foundation.utility.BlockHelper;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SchematicPlacePacket extends SimplePacketBase {

	public ItemStack stack;

	public SchematicPlacePacket(ItemStack stack) {
		this.stack = stack;
	}

	public SchematicPlacePacket(FriendlyByteBuf buffer) {
		stack = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, stack);
	}

	@Override
	public boolean handle(PacketContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			if (!player.isCreative())
				return;

			Level world = player.level();
			SchematicPrinter printer = new SchematicPrinter();
			printer.loadSchematic(stack, world, !player.canUseGameMasterBlocks());
			if (!printer.isLoaded() || printer.isErrored())
				return;

			boolean includeAir = false;

			while (printer.advanceCurrentPos()) {
				if (!printer.shouldPlaceCurrent(world))
					continue;

				printer.handleCurrentTarget((pos, state, blockEntity) -> {
					boolean placingAir = state.isAir();
					if (placingAir && !includeAir)
						return;

					CompoundTag data = BlockHelper.prepareBlockEntityData(state, blockEntity);
					BlockHelper.placeSchematicBlock(world, state, pos, null, data);
				}, (pos, entity) -> {
					world.addFreshEntity(entity);
				});
			}
		});
		return true;
	}

}
