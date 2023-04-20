package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.pathfinder.storage.Storage;
import de.cubbossa.pathapi.storage.VisualizerDataStorage;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InternalVisualizerDataStorage<T extends PathVisualizer<T, ?, ?>>
    implements VisualizerDataStorage<T> {

  private final AbstractVisualizerType<T> type;
  private final Storage storage;

  @Override
  public T createAndLoadVisualizer(NamespacedKey key) {
    return storage.getImplementation().createAndLoadVisualizer(type, key);
  }

  @Override
  public Map<NamespacedKey, T> loadVisualizers() {
    return storage.getImplementation().loadVisualizers(type);
  }

  @Override
  public Optional<T> loadVisualizer(NamespacedKey key) {
    return storage.getImplementation().loadVisualizer(type, key);
  }

  @Override
  public void saveVisualizer(T visualizer) {
    storage.getImplementation().saveVisualizer(visualizer);
  }

  @Override
  public void deleteVisualizer(T visualizer) {
    storage.getImplementation().deleteVisualizer(visualizer);
  }
}
