package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;

import java.util.Map;
import java.util.Optional;

public interface InternalVisualizerDataStorage {

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerT createAndLoadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadInternalVisualizers(VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> void saveInternalVisualizer(VisualizerT visualizer);

  <VisualizerT extends PathVisualizer<?, ?>> void deleteInternalVisualizer(VisualizerT visualizer);
}
