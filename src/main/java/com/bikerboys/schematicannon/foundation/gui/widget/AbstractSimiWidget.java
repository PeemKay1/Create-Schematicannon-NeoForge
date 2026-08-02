package com.bikerboys.schematicannon.foundation.gui.widget;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class AbstractSimiWidget extends AbstractWidget {
	protected static final Color HEADER_RGB = new Color(0x5391E1);
	protected static final Color HINT_RGB = new Color(0x96A6B7);

	protected List<Component> toolTip = new ArrayList<>();
	public int lockedTooltipX = -1;
	public int lockedTooltipY = -1;
	private Runnable callback = () -> {};

	protected AbstractSimiWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.empty());
	}

	@SuppressWarnings("unchecked")
	public <W extends AbstractSimiWidget> W withCallback(Runnable callback) {
		this.callback = callback;
		return (W) this;
	}

	public List<Component> getToolTip() {
		return toolTip;
	}

	public void tick() {}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY))
			return false;
		callback.run();
		return true;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return false;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		callback.run();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		return visible && active && isMouseOver(mouseX, mouseY) && mouseScrolled(mouseX, mouseY, vertical);
	}

	@Override
	protected final void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
			float partialTicks) {
		doRender(graphics, mouseX, mouseY, partialTicks);
	}

	protected void doRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
