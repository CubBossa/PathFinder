package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import java.util.Collection;
import java.util.Optional;

public interface VisualizerCache extends StorageCache<PathVisualizer<?, ?>> {

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> getVisualizer(NamespacedKey key);

  Optional<Collection<PathVisualizer<?, ?>>> getVisualizers();

  <VisualizerT extends PathVisualizer<?, ?>> Optional<Collection<VisualizerT>> getVisualizers(VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> void writeAll(VisualizerType<VisualizerT> type, Collection<VisualizerT> v);

  void writeAll(Collection<PathVisualizer<?, ?>> visualizers);
}
