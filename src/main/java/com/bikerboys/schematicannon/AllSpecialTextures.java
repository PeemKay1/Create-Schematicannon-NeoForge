package com.bikerboys.schematicannon;

import net.minecraft.resources.Identifier;

public enum AllSpecialTextures {

	CHECKERED("checkerboard.png"),
	THIN_CHECKERED("thin_checkerboard.png"),
	CUTOUT_CHECKERED("cutout_checkerboard.png"),
	HIGHLIGHT_CHECKERED("highlighted_checkerboard.png"),
	SELECTION("selection.png"),

	;

	public static final String ASSET_PATH = "textures/special/";
	private final Identifier location;

	AllSpecialTextures(String filename) {
		location = Schematicannon.asResource(ASSET_PATH + filename);
	}

	public Identifier getLocation() {
		return location;
	}

}
