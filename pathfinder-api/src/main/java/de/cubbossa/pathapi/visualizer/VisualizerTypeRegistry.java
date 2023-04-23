package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public interface VisualizerTypeRegistry {

  @Nullable <T extends PathVisualizer<T, ?, ?>> VisualizerType<T> getVisualizerType(
      NamespacedKey key);

  <T extends PathVisualizer<T, ?, ?>> void registerVisualizerType(VisualizerType<T> type);

  void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?, ?>> type);

  KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?, ?>>> getVisualizerTypes();
}
