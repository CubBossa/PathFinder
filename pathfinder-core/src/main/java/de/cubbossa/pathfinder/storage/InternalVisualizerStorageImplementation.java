package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import java.util.Map;
import java.util.Optional;

public interface InternalVisualizerStorageImplementation {

  <VisualizerT extends AbstractVisualizer<?, ?>> Optional<VisualizerT> loadInternalVisualizer(AbstractVisualizerType<VisualizerT> type, NamespacedKey key);

  <VisualizerT extends AbstractVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadInternalVisualizers(AbstractVisualizerType<VisualizerT> type);

  <VisualizerT extends AbstractVisualizer<?, ?>> void saveInternalVisualizer(AbstractVisualizerType<VisualizerT> type, VisualizerT visualizer);

  <VisualizerT extends AbstractVisualizer<?, ?>> void deleteInternalVisualizer(AbstractVisualizerType<VisualizerT> type, VisualizerT visualizer);
}
