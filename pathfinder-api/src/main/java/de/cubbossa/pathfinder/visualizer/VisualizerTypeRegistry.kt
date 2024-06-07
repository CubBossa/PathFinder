package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.KeyedRegistry;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import java.util.Optional;

public interface VisualizerTypeRegistry extends Disposable {

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> getDefaultType();

  <VisualizerT extends PathVisualizer<?, ?>> void setDefaultType(VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(NamespacedKey typeKey);

  <VisualizerT extends PathVisualizer<?, ?>> void registerVisualizerType(VisualizerType<VisualizerT> type);

  void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?>> type);

  KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> getTypes();
}
