package com.bikerboys.schematicannon.content.schematics.client;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.Window;
import com.bikerboys.schematicannon.AllKeys;
import com.bikerboys.schematicannon.content.schematics.client.tools.ToolType;
import com.bikerboys.schematicannon.foundation.gui.AllGuiTextures;
import com.bikerboys.schematicannon.foundation.utility.CreateLang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ToolSelectionScreen extends Screen {

	public final String scrollToCycle = CreateLang.translateDirect("gui.toolmenu.cycle")
		.getString();
	public final String holdToFocus = "gui.toolmenu.focusKey";

	protected List<ToolType> tools;
	protected Consumer<ToolType> callback;
	public boolean focused;
	private float yOffset;
	protected int selection;
	private boolean initialized;

	protected int w;
	protected int h;

	public ToolSelectionScreen(List<ToolType> tools, Consumer<ToolType> callback) {
		super(Component.literal("Tool Selection"));
		this.tools = tools;
		this.callback = callback;
		focused = false;
		yOffset = 0;
		selection = 0;
		initialized = false;

		callback.accept(tools.get(selection));

		w = Math.max(tools.size() * 50 + 30, 220);
		h = 42;
	}

	public void setSelectedElement(ToolType tool) {
		if (!tools.contains(tool))
			return;
		selection = tools.indexOf(tool);
	}

	public void cycle(int direction) {
		selection += (direction < 0) ? 1 : -1;
		selection = (selection + tools.size()) % tools.size();
		// Apply the highlighted tool while scrolling. Waiting for the modifier key
		// to be released made the 26.2 menu feel delayed and left the old tool active.
		callback.accept(tools.get(selection));
	}

	private void draw(GuiGraphicsExtractor graphics, float partialTicks) {
		Window mainWindow = minecraft.getWindow();
		int x = (mainWindow.getGuiScaledWidth() - w) / 2 + 15;
		int y = mainWindow.getGuiScaledHeight() - h - 75 - (int) yOffset;

		AllGuiTextures gray = AllGuiTextures.HUD_BACKGROUND;
		graphics.fill(x - 15, y, x - 15 + w, y + h, focused ? 0xdd202020 : 0x99202020);

		float toolTipAlpha = yOffset / 10;
		List<Component> toolTip = tools.get(selection)
			.getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

		if (toolTipAlpha > 0.25f) {
			int descriptionY = y + h + 3;
			graphics.fill(x - 15, descriptionY, x - 15 + w, descriptionY + 64, 0xdd202020);

			if (toolTip.size() > 0)
				graphics.text(font, toolTip.get(0), x - 10, descriptionY + 5, 0xEEEEEE + stringAlphaComponent, false);
			if (toolTip.size() > 1)
				graphics.text(font, toolTip.get(1), x - 10, descriptionY + 17, 0xCCDDFF + stringAlphaComponent, false);
			if (toolTip.size() > 2)
				graphics.text(font, toolTip.get(2), x - 10, descriptionY + 29, 0xCCDDFF + stringAlphaComponent, false);
			if (toolTip.size() > 3)
				graphics.text(font, toolTip.get(3), x - 10, descriptionY + 41, 0xCCCCDD + stringAlphaComponent, false);
		}

		if (tools.size() > 1) {
			String keyName = AllKeys.TOOL_MENU.getBoundKey();
			int width = minecraft.getWindow()
				.getGuiScaledWidth();
			if (!focused)
				graphics.centeredText(minecraft.font, CreateLang.translateDirect(holdToFocus, keyName), width / 2,
					y - 10, 0xFFCCDDFF);
			else
				graphics.centeredText(minecraft.font, scrollToCycle, width / 2, y - 10, 0xFFCCDDFF);
		} else {
			x += 65;
		}


		for (int i = 0; i < tools.size(); i++) {
			if (i == selection)
				graphics.fill(x + i * 50 + 14, y + 2, x + i * 50 + 34, y + 22, 0x556886C5);
			tools.get(i)
				.getIcon()
				.render(graphics, x + i * 50 + 16, y + 4);
		}
		graphics.centeredText(minecraft.font, tools.get(selection)
			.getDisplayName()
			.getString(), x - 15 + w / 2, y + 28, 0xFFCCDDFF);
	}

	public void update() {
		if (focused)
			yOffset += (10 - yOffset) * .1f;
		else
			yOffset *= .9f;
	}

	public void renderPassive(GuiGraphicsExtractor graphics, float partialTicks) {
		draw(graphics, partialTicks);
	}

	@Override
	public void onClose() {
		callback.accept(tools.get(selection));
	}

	@Override
	protected void init() {
		super.init();
		initialized = true;
	}
}
