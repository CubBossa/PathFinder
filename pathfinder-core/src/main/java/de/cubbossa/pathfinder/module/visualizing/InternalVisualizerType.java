package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import org.bukkit.NamespacedKey;

public abstract class InternalVisualizerType<T extends PathVisualizer<T, ?>> extends VisualizerType<T> {

  public InternalVisualizerType(NamespacedKey key) {
    super(key);
    setStorage(new InternalVisualizerDataStorage<>(this, PathPlugin.getInstance().getStorage().getImplementation()));
  }
}
