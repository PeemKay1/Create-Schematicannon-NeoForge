package com.bikerboys.schematicannon.foundation.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface ScreenElement {
	void render(GuiGraphicsExtractor graphics, int x, int y);
}
