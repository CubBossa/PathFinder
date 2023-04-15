package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;

public abstract class InternalVisualizerType<T extends PathVisualizer<T, ?, ?>> extends
    AbstractVisualizerType<T> {

  public InternalVisualizerType(NamespacedKey key) {
    super(key);
    setStorage(new InternalVisualizerDataStorage<>(this, PathPlugin.getInstance().getStorage()));
  }
}
