package com.bikerboys.schematicannon.foundation.utility;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public final class FontHelper {

	private FontHelper() {
	}

	public enum Palette {
		BLUE(ChatFormatting.GRAY, ChatFormatting.AQUA),
		GRAY_AND_BLUE(ChatFormatting.GRAY, ChatFormatting.AQUA),
		GRAY_AND_WHITE(ChatFormatting.GRAY, ChatFormatting.WHITE),
		ALL_GRAY(ChatFormatting.GRAY, ChatFormatting.DARK_GRAY);

		private final Style primary;
		private final Style highlight;

		Palette(ChatFormatting primary, ChatFormatting highlight) {
			this.primary = Style.EMPTY.applyFormat(primary);
			this.highlight = Style.EMPTY.applyFormat(highlight);
		}

		public Style primary() {
			return primary;
		}

		public Style highlight() {
			return highlight;
		}
	}

}
