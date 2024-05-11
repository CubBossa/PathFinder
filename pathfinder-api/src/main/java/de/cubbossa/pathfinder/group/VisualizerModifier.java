package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface VisualizerModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:visualizer");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  NamespacedKey getVisualizerKey();

  CompletableFuture<Optional<PathVisualizer<?, ?>>> getVisualizer();
}
