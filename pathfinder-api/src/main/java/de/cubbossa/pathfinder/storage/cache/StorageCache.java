package de.cubbossa.pathfinder.storage.cache;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public interface StorageCache<E> {

  void write(E e);

  void invalidate(E e);

  void invalidateAll();

  record CacheCollection<K, V>(Collection<V> present, Collection<K> absent) {

    public static <K, V> CacheCollection<K, V> empty(Collection<K> absent) {
      Preconditions.checkNotNull(absent);
      return new CacheCollection<>(new HashSet<>(), absent);
    }
  }

  record CacheMap<K, V>(Map<K, V> present, Collection<K> absent) {

    public static <K, V> CacheMap<K, V> empty(Collection<K> absent) {
      Preconditions.checkNotNull(absent);
      return new CacheMap<>(new HashMap<>(), absent);
    }
  }
}
