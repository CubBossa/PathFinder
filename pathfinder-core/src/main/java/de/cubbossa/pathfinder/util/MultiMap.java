package de.cubbossa.pathfinder.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MultiMap<K1, K2, V> implements Map<K1, Map<K2, V>> {

    private final Supplier<Map<K2, V>> factory;

    private final Map<K1, Map<K2, V>> map;

    public MultiMap() {
        this(HashMap::new, HashMap::new);
    }

    public MultiMap(Supplier<Map<K1, Map<K2, V>>> mapFactory, Supplier<Map<K2, V>> innerFactory) {
        map = mapFactory.get();
        factory = innerFactory;
    }

    @Override
    public int size() {
        return map.values().stream().mapToInt(Map::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Map<K2, V> get(Object key) {
        return null;
    }

    public V get(K1 key, K2 inner) {
        var i = map.get(key);
        return i == null ? null : i.get(inner);
    }

    @Nullable
    @Override
    public Map<K2, V> put(K1 key, Map<K2, V> value) {
        return null;
    }

    public V put(K1 key, K2 inner, V value) {
        return map.computeIfAbsent(key, k1 -> factory.get()).put(inner, value);
    }

    @Override
    public Map<K2, V> remove(Object key) {
        return map.remove(key);
    }

    public V removeInner(K1 key, K2 inner) {
        var i = map.get(key);
        return i == null ? null : i.remove(inner);
    }

    @Override
    public void putAll(@NotNull Map<? extends K1, ? extends Map<K2, V>> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<K1> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<Map<K2, V>> values() {
        return map.values();
    }

    public Collection<V> flatValues() {
        return map.values().stream().flatMap(m -> m.values().stream()).toList();
    }

    @NotNull
    @Override
    public Set<Entry<K1, Map<K2, V>>> entrySet() {
        return map.entrySet();
    }

    public boolean containsKeyPair(K1 key, K2 inner) {
        var i = map.get(key);
        return i != null && i.containsKey(inner);
    }
}
