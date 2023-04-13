package de.cubbossa.pathfinder.api.misc;

import lombok.Getter;

@Getter
public final class NamespacedKey {

	private static final char SEPARATOR = ':';

	public static NamespacedKey fromString(String value) {
		String[] splits = value.split(":");
		return new NamespacedKey(splits[0], splits[1]);
	}

	private final String namespace;
	private final String key;

	public NamespacedKey(String namespace, String key) {
		this.namespace = namespace;
		this.key = key;
	}

	@Override
	public String toString() {
		return namespace + SEPARATOR + key;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
