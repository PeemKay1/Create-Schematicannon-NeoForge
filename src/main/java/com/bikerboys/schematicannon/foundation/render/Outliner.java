package com.bikerboys.schematicannon.foundation.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.phys.AABB;

public final class Outliner {
	private static final Outliner INSTANCE = new Outliner();
	private final Map<Object, AABBOutline> outlines = new HashMap<>();

	public static Outliner getInstance() { return INSTANCE; }
	public AABBOutline chaseAABB(Object key, AABB bounds) {
		return outlines.computeIfAbsent(key, ignored -> new AABBOutline(bounds)).setBounds(bounds);
	}
	public void remove(Object key) { outlines.remove(key); }
	private Outliner() {}
}
