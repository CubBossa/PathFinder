package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;

public interface VisualizerModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:visualizer");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  PathVisualizer<?, ?> visualizer();
}
