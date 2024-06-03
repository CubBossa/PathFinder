package de.cubbossa.pathfinder.misc

interface KeyedRegistry<T : Keyed> : MutableMap<NamespacedKey, T>, Iterable<T> {

    fun put(value: T): T
}
