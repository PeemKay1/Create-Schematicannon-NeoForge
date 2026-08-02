package com.bikerboys.schematicannon.foundation.utility;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class LangBuilder {
	private final String namespace;
	private MutableComponent component = Component.empty();

	public LangBuilder(String namespace) {
		this.namespace = namespace;
	}

	public LangBuilder add(Component other) {
		component.append(other);
		return this;
	}

	public LangBuilder text(String text) {
		return add(Component.literal(text));
	}

	public LangBuilder translate(String key, Object... args) {
		return add(Component.translatable(namespace + "." + key, resolveBuilders(args)));
	}

	public LangBuilder style(ChatFormatting... styles) {
		component.withStyle(styles);
		return this;
	}

	public MutableComponent component() {
		return component;
	}

	public void forGoggles(List<Component> tooltip) {
		tooltip.add(component);
	}

	public void sendStatus(Player player) {
		player.sendOverlayMessage(component);
	}

	public static Object[] resolveBuilders(Object... args) {
		Object[] resolved = new Object[args.length];
		for (int i = 0; i < args.length; i++)
			resolved[i] = args[i] instanceof LangBuilder builder ? builder.component() : args[i];
		return resolved;
	}
}
