package com.bikerboys.schematicannon.foundation.utility;

public record Couple<T>(T first, T second) {

	public static <T> Couple<T> create(T first, T second) {
		return new Couple<>(first, second);
	}

	public T getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public T get(boolean firstValue) {
		return firstValue ? first : second;
	}

}
