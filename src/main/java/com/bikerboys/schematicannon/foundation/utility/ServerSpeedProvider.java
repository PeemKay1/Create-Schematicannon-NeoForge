package com.bikerboys.schematicannon.foundation.utility;

import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;
import com.bikerboys.schematicannon.foundation.networking.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerSpeedProvider {

	static int clientTimer = 0;
	static int serverTimer = 0;
	static boolean initialized = false;
	static float modifier = 1;
	static float targetModifier = 1;

	public static void serverTick() {
		serverTimer++;
		if (serverTimer > getSyncInterval()) {
			PacketDistributor.sendToAllPlayers(new Packet());
			serverTimer = 0;
		}
	}

	public static void clientTick() {
		if (Minecraft.getInstance()
			.hasSingleplayerServer()
			&& Minecraft.getInstance()
				.isPaused())
			return;
		modifier += (targetModifier - modifier) * .25f;
		if (Math.abs(targetModifier - modifier) < .001f)
			modifier = targetModifier;
		clientTimer++;
	}

	public static Integer getSyncInterval() {
		return 20;
	}

	public static float get() {
		return modifier;
	}

	public static class Packet extends SimplePacketBase {

		public Packet() {}

		public Packet(FriendlyByteBuf buffer) {}

		@Override
		public void write(FriendlyByteBuf buffer) {}

		@Override
		public boolean handle(PacketContext context) {
			context.enqueueWork(() -> {
				if (!initialized) {
					initialized = true;
					clientTimer = 0;
					return;
				}
				float target = ((float) getSyncInterval()) / Math.max(clientTimer, 1);
				targetModifier = Math.min(target, 1);
				// Set this to -1 because packets are processed before ticks.
				// ServerSpeedProvider#clientTick will increment it to 0 at the end of this tick.
				// Setting it to 0 causes consistent desync, as the client ends up counting too many ticks.
				clientTimer = -1;
			});
			return true;
		}

	}

}
