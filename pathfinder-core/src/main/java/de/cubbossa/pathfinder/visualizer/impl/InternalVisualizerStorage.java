package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerDataStorage;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class InternalVisualizerStorage<T extends PathVisualizer<?, ?>>
    implements VisualizerDataStorage<T> {

  private final AbstractVisualizerType<T> type;
  private final InternalVisualizerDataStorage storage;

  @Override
  public T createAndLoadVisualizer(NamespacedKey key) {
    return storage.createAndLoadInternalVisualizer(type, key);
  }

  @Override
  public Map<NamespacedKey, T> loadVisualizers() {
    return storage.loadInternalVisualizers(type);
  }

  @Override
  public Optional<T> loadVisualizer(NamespacedKey key) {
    return storage.loadInternalVisualizer(type, key);
  }

  @Override
  public void saveVisualizer(T visualizer) {
    storage.saveInternalVisualizer(visualizer);
  }

  @Override
  public void deleteVisualizer(T visualizer) {
    storage.deleteInternalVisualizer(visualizer);
  }
}
