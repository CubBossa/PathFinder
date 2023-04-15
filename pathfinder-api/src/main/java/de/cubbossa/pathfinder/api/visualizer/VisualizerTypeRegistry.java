package de.cubbossa.pathfinder.api.visualizer;

import de.cubbossa.pathfinder.api.misc.KeyedRegistry;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import org.jetbrains.annotations.Nullable;

public interface VisualizerTypeRegistry {

  @Nullable <T extends PathVisualizer<T, ?, ?>> VisualizerType<T> getVisualizerType(NamespacedKey key);

  <T extends PathVisualizer<T, ?, ?>> void registerVisualizerType(VisualizerType<T> type);

  void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?, ?>> type);

  KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?, ?>>> getVisualizerTypes();
}
