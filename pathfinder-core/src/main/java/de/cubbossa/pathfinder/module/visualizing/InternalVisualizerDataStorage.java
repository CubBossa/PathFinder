package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.storage.Storage;
import de.cubbossa.pathfinder.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.VisualizerDataStorage;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;

@RequiredArgsConstructor
public class InternalVisualizerDataStorage<T extends PathVisualizer<T, ?>>
    implements VisualizerDataStorage<T> {

  private final VisualizerType<T> type;
  private final StorageImplementation storage;

  @Override
  public T createAndLoadVisualizer(NamespacedKey key) {
    return storage.createAndLoadVisualizer(type, key);
  }

  @Override
  public Map<NamespacedKey, T> loadVisualizers() {
    return storage.loadVisualizers(type);
  }

  @Override
  public Optional<T> loadVisualizer(NamespacedKey key) {
    return storage.loadVisualizer(type, key);
  }

  @Override
  public void saveVisualizer(T visualizer) {
    storage.saveVisualizer(visualizer);
  }

  @Override
  public void deleteVisualizer(T visualizer) {
    storage.deleteVisualizer(visualizer);
  }
}
