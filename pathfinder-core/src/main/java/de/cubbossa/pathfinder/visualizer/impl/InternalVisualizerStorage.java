package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerDataStorage;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import lombok.RequiredArgsConstructor;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class InternalVisualizerStorage<T extends PathVisualizer<?, ?>>
    implements VisualizerDataStorage<T> {

  private final AbstractVisualizerType<T> type;
  private final InternalVisualizerDataStorage storage;

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
