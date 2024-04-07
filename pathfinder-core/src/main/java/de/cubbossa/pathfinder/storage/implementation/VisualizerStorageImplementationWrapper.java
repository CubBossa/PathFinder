package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VisualizerStorageImplementationWrapper<T extends AbstractVisualizer<?, ?>>
    implements VisualizerStorageImplementation<T> {

  private final AbstractVisualizerType<T> type;
  private final InternalVisualizerStorageImplementation storage;

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
    storage.saveInternalVisualizer(type, visualizer);
  }

  @Override
  public void deleteVisualizer(T visualizer) {
    storage.deleteInternalVisualizer(type, visualizer);
  }
}
