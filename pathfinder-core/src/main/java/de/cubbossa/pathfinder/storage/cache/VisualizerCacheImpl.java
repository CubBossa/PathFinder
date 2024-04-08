package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;

import java.util.*;

public class VisualizerCacheImpl
    implements de.cubbossa.pathfinder.storage.cache.VisualizerCache {

  private final Cache<NamespacedKey, PathVisualizer<?, ?>> cache;
  private boolean cachedAll = false;
  private Map<NamespacedKey, Collection<PathVisualizer<?, ?>>> cachedTypes = new HashMap<>();

  public VisualizerCacheImpl() {
    cache = Caffeine.newBuilder()
        .maximumSize(100)
        .build();
  }

  @Override
  public <T extends PathVisualizer<?, ?>> Optional<T> getVisualizer(NamespacedKey key) {
    return Optional.ofNullable((T) cache.asMap().get(key));
  }

  @Override
  public Optional<Collection<PathVisualizer<?, ?>>> getVisualizers() {
    return Optional.ofNullable(cachedAll ? new HashSet<>(cache.asMap().values()) : null);
  }

  @Override
  public <T extends PathVisualizer<?, ?>> Optional<Collection<T>> getVisualizers(VisualizerType<T> type) {
    return Optional.ofNullable((Collection<T>) cachedTypes.get(type));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void writeAll(VisualizerType<VisualizerT> type, Collection<VisualizerT> v) {
    v.forEach(vis -> cache.put(vis.getKey(), vis));
    cachedTypes.put(type.getKey(), (Collection<PathVisualizer<?, ?>>) v);
  }

  @Override
  public void writeAll(Collection<PathVisualizer<?, ?>> visualizers) {
    visualizers.forEach(v -> cache.put(v.getKey(), v));
    cachedAll = true;
  }

  @Override
  public void write(PathVisualizer<?, ?> visualizer) {
    cache.put(visualizer.getKey(), visualizer);
  }

  @Override
  public void invalidate(PathVisualizer<?, ?> visualizer) {
    cache.invalidate(visualizer.getKey());
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
    cachedTypes.clear();
    cachedAll = false;
  }
}
