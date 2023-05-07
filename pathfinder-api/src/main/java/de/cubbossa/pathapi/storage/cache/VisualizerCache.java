package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface VisualizerCache extends StorageCache<PathVisualizer<?, ?>> {
  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> getVisualizer(NamespacedKey key,
                                                                                 Function<NamespacedKey, Optional<VisualizerT>> loader);

  Collection<PathVisualizer<?, ?>> getVisualizers(
      Supplier<Collection<PathVisualizer<?, ?>>> loader);

  <VisualizerT extends PathVisualizer<?, ?>> Collection<VisualizerT> getVisualizers(
      VisualizerType<VisualizerT> type,
      Function<VisualizerType<VisualizerT>, Collection<VisualizerT>> loader);
}
