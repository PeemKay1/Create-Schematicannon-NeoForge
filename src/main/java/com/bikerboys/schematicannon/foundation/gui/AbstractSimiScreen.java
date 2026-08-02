package com.bikerboys.schematicannon.foundation.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

/**
 * Standalone base for Schematicannon's centered screens.
 */
public abstract class AbstractSimiScreen extends Screen {

	protected int windowWidth;
	protected int windowHeight;
	protected int guiLeft;
	protected int guiTop;
	protected int windowXOffset;
	protected int windowYOffset;

	protected AbstractSimiScreen() {
		super(Component.empty());
	}

	protected AbstractSimiScreen(Component title) {
		super(title);
	}

	protected void setWindowSize(int width, int height) {
		windowWidth = width;
		windowHeight = height;
	}

	protected void setWindowOffset(int x, int y) {
		windowXOffset = x;
		windowYOffset = y;
	}

	@SafeVarargs
	protected final <T extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(T... widgets) {
		for (T widget : widgets)
			addRenderableWidget(widget);
	}

	@Override
	protected void init() {
		guiLeft = (width - windowWidth) / 2 + windowXOffset;
		guiTop = (height - windowHeight) / 2 + windowYOffset;
	}

	@Override
	protected void repositionElements() {
		guiLeft = (width - windowWidth) / 2 + windowXOffset;
		guiTop = (height - windowHeight) / 2 + windowYOffset;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		return keyPressed(event.key(), event.scancode(), event.modifiers());
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
	}

	protected boolean isPaste(int keyCode) {
		return keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_V && com.bikerboys.schematicannon.AllKeys.ctrlDown();
	}

	protected boolean isSelectAll(int keyCode) { return keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_A && hasControlDown(); }
	protected boolean isCopy(int keyCode) { return keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_C && hasControlDown(); }
	protected boolean isCut(int keyCode) { return keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_X && hasControlDown(); }
	protected boolean hasControlDown() { return com.bikerboys.schematicannon.AllKeys.ctrlDown(); }
	protected boolean hasShiftDown() { return com.bikerboys.schematicannon.AllKeys.shiftDown(); }

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return mouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		return mouseScrolled(mouseX, mouseY, vertical) || super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return charTyped((char) event.codepoint(), 0);
	}

	public boolean charTyped(char codePoint, int modifiers) {
		return super.charTyped(new CharacterEvent(codePoint));
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		return mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)
			|| super.mouseDragged(event, dragX, dragY);
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		renderWindow(graphics, mouseX, mouseY, partialTicks);
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
	}

	protected abstract void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
		float partialTicks);

}
