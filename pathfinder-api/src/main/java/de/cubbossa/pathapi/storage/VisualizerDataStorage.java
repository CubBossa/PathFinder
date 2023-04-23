package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import java.util.Map;
import java.util.Optional;

public interface VisualizerDataStorage<T extends PathVisualizer<T, ?, ?>> {

  T createAndLoadVisualizer(NamespacedKey key);

  Map<NamespacedKey, T> loadVisualizers();

  Optional<T> loadVisualizer(NamespacedKey key);

  void saveVisualizer(T visualizer);

  void deleteVisualizer(T visualizer);
}
