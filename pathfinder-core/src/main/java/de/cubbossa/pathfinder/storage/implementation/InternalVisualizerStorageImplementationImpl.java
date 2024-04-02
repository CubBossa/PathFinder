package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerStorageImplementation;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InternalVisualizerStorageImplementationImpl<T extends PathVisualizer<?, ?>>
    implements VisualizerStorageImplementation<T> {

  private final InternalVisualizerStorageImplementation storage;

  @Override
  public T createAndLoadVisualizer(VisualizerType<T> type, NamespacedKey key) {
    return storage.createAndLoadInternalVisualizer(type, key);
  }

  @Override
  public Map<NamespacedKey, T> loadVisualizers(VisualizerType<T> type) {
    return storage.loadInternalVisualizers(type);
  }

  @Override
  public Optional<T> loadVisualizer(VisualizerType<T> type, NamespacedKey key) {
    return storage.loadInternalVisualizer(type, key);
  }

  @Override
  public void saveVisualizer(VisualizerType<T> type, T visualizer) {
    storage.saveInternalVisualizer(type, visualizer);
  }

  @Override
  public void deleteVisualizer(VisualizerType<T> type, T visualizer) {
    storage.deleteInternalVisualizer(visualizer);
  }
}
