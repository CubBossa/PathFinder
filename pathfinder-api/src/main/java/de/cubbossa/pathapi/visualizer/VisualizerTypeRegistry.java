package de.cubbossa.pathapi.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import java.util.Optional;

public interface VisualizerTypeRegistry extends Disposable {

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> getDefaultType();

  <VisualizerT extends PathVisualizer<?, ?>> void setDefaultType(VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(NamespacedKey typeKey);

  <VisualizerT extends PathVisualizer<?, ?>> void registerVisualizerType(VisualizerType<VisualizerT> type);

  void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?>> type);

  KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> getTypes();
}
