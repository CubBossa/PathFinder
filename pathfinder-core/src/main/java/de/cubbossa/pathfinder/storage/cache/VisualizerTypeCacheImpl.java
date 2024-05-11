package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class VisualizerTypeCacheImpl implements VisualizerTypeCache {

  private final Map<NamespacedKey, VisualizerType<?>> types;

  public VisualizerTypeCacheImpl() {
    types = new HashMap<>();
  }

  @Override
  public void write(Map.Entry<NamespacedKey, VisualizerType<?>> e) {
    types.put(e.getKey(), e.getValue());
  }

  @Override
  public void invalidate(Map.Entry<NamespacedKey, VisualizerType<?>> e) {
    types.remove(e.getKey());
  }

  @Override
  public void invalidateAll() {
    types.clear();
  }

  @Override
  public CacheMap<NamespacedKey, VisualizerType<?>> getTypes(Collection<NamespacedKey> keys) {
    HashMap<NamespacedKey, VisualizerType<?>> present = new HashMap<>();
    HashSet<NamespacedKey> absent = new HashSet<>();
    for (NamespacedKey key : keys) {
      if (types.containsKey(key)) {
        present.put(key, types.get(key));
      } else {
        absent.add(key);
      }
    }
    return new CacheMap<>(present, absent);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(NamespacedKey key) {
    return Optional.ofNullable((VisualizerType<VisualizerT>) types.get(key));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void write(NamespacedKey key, VisualizerType<VisualizerT> type) {
    types.put(key, type);
  }
}
