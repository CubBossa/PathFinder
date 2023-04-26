package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.PathPlugin;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class VisualizerCacheImpl
    implements StorageCache<PathVisualizer<?, ?>>,
    de.cubbossa.pathapi.storage.cache.VisualizerCache {

  private final Cache<NamespacedKey, PathVisualizer<?, ?>> cache;
  private boolean cachedAll = false;
  private Collection<NamespacedKey> cachedTypes = new HashSet<>();

  public VisualizerCacheImpl() {
    cache = Caffeine.newBuilder()
        .maximumSize(100)
        .build();
  }

  @Override
  public <T extends PathVisualizer<?, ?>> Optional<T> getVisualizer(NamespacedKey key,
                                                                    Function<NamespacedKey, T> loader) {
    return Optional.ofNullable((T) cache.get(key, loader));
  }

  @Override
  public Collection<PathVisualizer<?, ?>> getVisualizers(
      Supplier<Collection<PathVisualizer<?, ?>>> loader) {
    if (cachedAll) {
      return cache.asMap().values();
    }
    loader.get().forEach(e -> cache.put(e.getKey(), e));
    cachedAll = true;
    return cache.asMap().values();
  }

  @Override
  public <T extends PathVisualizer<?, ?>> Collection<T> getVisualizers(VisualizerType<T> type,
                                                                       Function<VisualizerType<T>, Collection<T>> loader) {
    if (cachedAll || cachedTypes.contains(type.getKey())) {
      return cache.asMap().values().stream()
          .filter(
              visualizer -> PathPlugin.getInstance().getVisualizerTypeResolver().resolve(visualizer)
                  .equals(type))
          .map(visualizer -> (T) visualizer)
          .toList();
    }
    Collection<T> loaded = loader.apply(type);
    loaded.forEach(v -> cache.put(v.getKey(), v));
    cachedTypes.add(type.getKey());
    return loaded;
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
