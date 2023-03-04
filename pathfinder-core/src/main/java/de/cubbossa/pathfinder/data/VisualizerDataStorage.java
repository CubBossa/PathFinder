package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import java.util.Map;
import org.bukkit.NamespacedKey;

public interface VisualizerDataStorage<T extends PathVisualizer<T, ?>> {

  Map<NamespacedKey, T> loadPathVisualizer();

  void updatePathVisualizer(T visualizer);

  void deletePathVisualizer(T visualizer);
}
