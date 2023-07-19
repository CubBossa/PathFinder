package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;

import java.util.concurrent.CompletableFuture;

public interface VisualizerModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:visualizer");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  NamespacedKey getVisualizerKey();

  CompletableFuture<PathVisualizer<?, ?>> getVisualizer();
}
