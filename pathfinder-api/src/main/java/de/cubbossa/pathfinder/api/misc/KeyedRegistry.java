package de.cubbossa.pathfinder.api.misc;

import java.util.Map;

public interface KeyedRegistry<T extends Keyed> extends Map<NamespacedKey, T>, Iterable<T> {

	T put(T value);
}
