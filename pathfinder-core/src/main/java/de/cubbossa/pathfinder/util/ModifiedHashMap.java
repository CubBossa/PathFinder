package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.Changes;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModifiedHashMap<K, V> extends HashMap<K, V> {

  @Getter
  private final Changes<V> changes;

  public ModifiedHashMap() {
    changes = new Changes<>();
  }

  @Override
  public V put(K key, V value) {
    changes.getAddList().add(value);
    return super.put(key, value);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    super.putAll(m);
    changes.getAddList().addAll(m.values());
  }

  @Override
  public V remove(Object key) {
    V val = super.remove(key);
    if (val != null) {
      changes.getRemoveList().add(val);
    }
    return val;
  }

  @Override
  public boolean remove(Object key, Object value) {
    if (super.remove(key, value)) {
      changes.getRemoveList().add((V) value);
      return true;
    }
    return false;
  }

  @Override
  public V putIfAbsent(K key, V value) {
    V val = super.putIfAbsent(key, value);
    if (val == null) {
      changes.getAddList().add(value);
    }
    return val;
  }

  @Override
  public Collection<V> values() {
    return new ModifiedHashSet<>(changes, super.values());
  }
}
