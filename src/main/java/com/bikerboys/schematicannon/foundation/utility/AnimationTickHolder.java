package com.bikerboys.schematicannon.foundation.utility;

import net.minecraft.client.Minecraft;

public final class AnimationTickHolder {

	private AnimationTickHolder() {
	}

	public static float getPartialTicks() {
		return Minecraft.getInstance()
			.getDeltaTracker()
			.getGameTimeDeltaPartialTick(false);
	}

	public static int getTicks(boolean includePaused) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null)
			return 0;
		return (int) minecraft.level.getGameTime();
	}

	public static void reset() {
	}

}
