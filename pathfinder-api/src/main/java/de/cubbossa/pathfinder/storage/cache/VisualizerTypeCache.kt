package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface VisualizerTypeCache extends StorageCache<Map.Entry<NamespacedKey, VisualizerType<?>>> {

  CacheMap<NamespacedKey, VisualizerType<?>> getTypes(Collection<NamespacedKey> key);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> void write(NamespacedKey key, VisualizerType<VisualizerT> type);
}
