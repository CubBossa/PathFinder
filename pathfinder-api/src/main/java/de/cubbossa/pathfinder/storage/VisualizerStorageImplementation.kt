package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import java.util.Map;
import java.util.Optional;

public interface VisualizerStorageImplementation<VisualizerT extends PathVisualizer<?, ?>> {

  Map<NamespacedKey, VisualizerT> loadVisualizers();

  Optional<VisualizerT> loadVisualizer(NamespacedKey key);

  void saveVisualizer(VisualizerT visualizer);

  void deleteVisualizer(VisualizerT visualizer);
}
