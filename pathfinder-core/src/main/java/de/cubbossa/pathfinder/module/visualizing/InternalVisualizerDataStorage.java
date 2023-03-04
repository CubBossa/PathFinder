package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.data.VisualizerDataStorage;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;

@RequiredArgsConstructor
public class InternalVisualizerDataStorage<T extends PathVisualizer<T, ?>>
    implements VisualizerDataStorage<T> {

  private final VisualizerType<T> type;
  private final DataStorage storage;

  @Override
  public Map<NamespacedKey, T> loadPathVisualizer() {
    return storage.loadPathVisualizer(type);
  }

  @Override
  public void updatePathVisualizer(T visualizer) {
    storage.updatePathVisualizer(visualizer);
  }

  @Override
  public void deletePathVisualizer(T visualizer) {
    storage.deletePathVisualizer(visualizer);
  }
}
