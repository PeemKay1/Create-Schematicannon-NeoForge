package com.bikerboys.schematicannon.foundation.utility;

import java.util.Locale;

public final class LangUtil {
	public static String asId(String name) {
		return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", "_")
			.replaceAll("^_+|_+$", "");
	}

	private LangUtil() {}
}
