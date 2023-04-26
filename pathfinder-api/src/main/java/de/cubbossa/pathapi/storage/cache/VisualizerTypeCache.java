package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface VisualizerTypeCache
    extends StorageCache<Map.Entry<NamespacedKey, VisualizerType<?>>> {

  Map<NamespacedKey, VisualizerType<?>> getTypes(
      Collection<NamespacedKey> key,
      Function<Collection<NamespacedKey>, Map<NamespacedKey, VisualizerType<?>>> loader);

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> getType(NamespacedKey key,
                                                                                 Function<NamespacedKey, Optional<VisualizerType<VisualizerT>>> loader);

  <VisualizerT extends PathVisualizer<?, ?>> void write(NamespacedKey key,
                                                        VisualizerType<VisualizerT> type);
}
