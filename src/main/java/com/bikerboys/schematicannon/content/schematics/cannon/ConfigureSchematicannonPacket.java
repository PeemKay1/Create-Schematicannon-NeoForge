package com.bikerboys.schematicannon.content.schematics.cannon;

import com.bikerboys.schematicannon.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;
import com.bikerboys.schematicannon.foundation.networking.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ConfigureSchematicannonPacket extends SimplePacketBase {

	public enum Option {
		DONT_REPLACE, REPLACE_SOLID, REPLACE_ANY, REPLACE_EMPTY, SKIP_MISSING, SKIP_BLOCK_ENTITIES, PLAY, PAUSE, STOP
	}

	private final Option option;
	private final boolean set;

	public ConfigureSchematicannonPacket(Option option, boolean set) {
		this.option = option;
		this.set = set;
	}

	public ConfigureSchematicannonPacket(FriendlyByteBuf buffer) {
		this(buffer.readEnum(Option.class), buffer.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeEnum(option);
		buffer.writeBoolean(set);
	}

	@Override
	public boolean handle(PacketContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null || !(player.containerMenu instanceof SchematicannonMenu))
				return;

			SchematicannonBlockEntity be = ((SchematicannonMenu) player.containerMenu).contentHolder;
			apply(be, option, set);
		});
		return true;
	}

	public static void apply(SchematicannonBlockEntity be, Option option, boolean set) {
		switch (option) {
			case DONT_REPLACE:
			case REPLACE_ANY:
			case REPLACE_EMPTY:
			case REPLACE_SOLID:
				be.replaceMode = option.ordinal();
				break;
			case SKIP_MISSING:
				be.skipMissing = set;
				break;
			case SKIP_BLOCK_ENTITIES:
				be.replaceBlockEntities = set;
				break;

			case PLAY:
				be.state = State.RUNNING;
				be.statusMsg = "running";
				break;
			case PAUSE:
				be.state = State.PAUSED;
				be.statusMsg = "paused";
				break;
			case STOP:
				be.state = State.STOPPED;
				be.statusMsg = "stopped";
				break;
			default:
				break;
		}

		be.sendUpdate = true;
	}

}
