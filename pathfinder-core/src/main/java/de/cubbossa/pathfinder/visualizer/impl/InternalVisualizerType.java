package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;

public abstract class InternalVisualizerType<VisualizerT extends PathVisualizer<?, ?>> extends
    AbstractVisualizerType<VisualizerT> {

  public InternalVisualizerType(NamespacedKey key) {
      super(key);
      setStorage(new InternalVisualizerDataStorage<>(this, (StorageImpl) PathFinderProvider.get().getStorage()));
  }
}
