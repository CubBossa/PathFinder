package de.cubbossa.pathfinder.api.storage;

import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import java.util.Map;
import java.util.Optional;

public interface VisualizerDataStorage<T extends PathVisualizer<T, ?>> {

  T createAndLoadVisualizer(NamespacedKey key);
  Map<NamespacedKey, T> loadVisualizers();
  Optional<T> loadVisualizer(NamespacedKey key);
  void saveVisualizer(T visualizer);
  void deleteVisualizer(T visualizer);
}