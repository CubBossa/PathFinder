package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;

public class VisualizerCache {

  private final Cache<NamespacedKey, PathVisualizer<?, ?>> cache;
  private boolean cachedAll = false;
  private Collection<NamespacedKey> cachedTypes = new HashSet<>();

  public VisualizerCache() {
    cache = Caffeine.newBuilder()
        .maximumSize(100)
        .weakValues()
        .build();
  }

  public <T extends PathVisualizer<T, ?>> Optional<T> getVisualizer(NamespacedKey key, Function<NamespacedKey, T> loader) {
    return Optional.ofNullable((T) cache.get(key, loader));
  }

  public Collection<PathVisualizer<?, ?>> getVisualizers(Supplier<Collection<PathVisualizer<?, ?>>> loader) {
    if (cachedAll) {
      return cache.asMap().values();
    }
    loader.get().forEach(e -> cache.put(e.getKey(), e));
    cachedAll = true;
    return cache.asMap().values();
  }

  public <T extends PathVisualizer<T, ?>> Collection<T> getVisualizers(VisualizerType<T> type, Function<VisualizerType<T>, Collection<T>> loader) {
    if (cachedAll || cachedTypes.contains(type.getKey())) {
      return cache.asMap().values().stream()
          .filter(visualizer -> visualizer.getType().equals(type))
          .map(visualizer -> (T) visualizer)
          .toList();
    }
    Collection<T> loaded = loader.apply(type);
    loaded.forEach(v -> cache.put(v.getKey(), v));
    cachedTypes.add(type.getKey());
    return loaded;
  }

  public void write(PathVisualizer<?, ?> visualizer) {
    cache.put(visualizer.getKey(), visualizer);
  }

  public void invalidate(PathVisualizer<?, ?> visualizer) {
    cache.invalidate(visualizer.getKey());
  }
}
