package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public interface VisualizerTypeRegistry {

  @Nullable <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> getVisualizerType(
      NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> void registerVisualizerType(
      VisualizerType<VisualizerT> type);

  void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?>> type);

  KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> getVisualizerTypes();
}
