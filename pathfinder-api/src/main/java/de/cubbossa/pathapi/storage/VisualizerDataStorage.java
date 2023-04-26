package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import java.util.Map;
import java.util.Optional;

public interface VisualizerDataStorage<VisualizerT extends PathVisualizer<?, ?>> {

  VisualizerT createAndLoadVisualizer(NamespacedKey key);

  Map<NamespacedKey, VisualizerT> loadVisualizers();

  Optional<VisualizerT> loadVisualizer(NamespacedKey key);

  void saveVisualizer(VisualizerT visualizer);

  void deleteVisualizer(VisualizerT visualizer);
}
