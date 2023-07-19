package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface VisualizerTypeCache extends StorageCache<Map.Entry<NamespacedKey, VisualizerType<?>>> {

  CacheMap<NamespacedKey, VisualizerType<?>> getTypes(Collection<NamespacedKey> key);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> void write(NamespacedKey key, VisualizerType<VisualizerT> type);
}
