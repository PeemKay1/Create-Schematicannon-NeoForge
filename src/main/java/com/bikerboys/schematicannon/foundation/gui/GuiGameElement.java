package com.bikerboys.schematicannon.foundation.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public final class GuiGameElement {
	public static GuiRenderBuilder of(ItemStack stack) {
		return new GuiRenderBuilder(stack);
	}

	public static final class GuiRenderBuilder {
		private final ItemStack stack;
		private int x;
		private int y;
		private float scale = 1;

		private GuiRenderBuilder(ItemStack stack) {
			this.stack = stack;
		}

		@SuppressWarnings("unchecked")
		public <T extends GuiRenderBuilder> T at(int x, int y, int z) {
			this.x = x;
			this.y = y;
			return (T) this;
		}

		public GuiRenderBuilder scale(float scale) {
			this.scale = scale;
			return this;
		}

		public void render(GuiGraphicsExtractor graphics) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(x, y);
			graphics.pose().scale(scale, scale);
			graphics.item(stack, 0, 0);
			graphics.pose().popMatrix();
		}
	}

	private GuiGameElement() {}
}
