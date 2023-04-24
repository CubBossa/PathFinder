package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface VisualizerCache extends StorageCache<PathVisualizer<?, ?, ?>> {
  <T extends PathVisualizer<T, ?, ?>> Optional<T> getVisualizer(NamespacedKey key,
                                                                Function<NamespacedKey, T> loader);

  Collection<PathVisualizer<?, ?, ?>> getVisualizers(
      Supplier<Collection<PathVisualizer<?, ?, ?>>> loader);

  <T extends PathVisualizer<T, ?, ?>> Collection<T> getVisualizers(VisualizerType<T> type,
                                                                   Function<VisualizerType<T>, Collection<T>> loader);
}
