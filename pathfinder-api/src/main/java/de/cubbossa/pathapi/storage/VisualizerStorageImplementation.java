package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import java.util.Map;
import java.util.Optional;

public interface VisualizerStorageImplementation<VisualizerT extends PathVisualizer<?, ?>> {

  VisualizerT createAndLoadVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key);

  Map<NamespacedKey, VisualizerT> loadVisualizers(VisualizerType<VisualizerT> type);

  Optional<VisualizerT> loadVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key);

  void saveVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer);

  void deleteVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer);
}
