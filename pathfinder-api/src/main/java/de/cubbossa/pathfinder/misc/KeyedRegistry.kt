package de.cubbossa.pathfinder.misc;

import java.util.Map;

public interface KeyedRegistry<T extends Keyed> extends Map<NamespacedKey, T>, Iterable<T> {

  T put(T value);
}
