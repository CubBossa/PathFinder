package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import java.util.Optional;

public interface VisualizerTypeRegistry {

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(
      NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(
      VisualizerT visualizer);

  <VisualizerT extends PathVisualizer<?, ?>> void registerVisualizerType(
      VisualizerType<VisualizerT> type);

  void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?>> type);

  KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> getTypes();
}
