package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.group.Modifier;
import de.cubbossa.pathfinder.group.VisualizerModifier;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VisualizerModifierImpl implements Modifier, VisualizerModifier {

  private final NamespacedKey visualizerKey;

  public VisualizerModifierImpl(NamespacedKey visualizerKey) {
    this.visualizerKey = visualizerKey;
  }

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }

  public NamespacedKey getVisualizerKey() {
    return visualizerKey;
  }

  @Override
  public CompletableFuture<Optional<PathVisualizer<?, ?>>> getVisualizer() {
    return PathFinderProvider.get().getStorage().loadVisualizer(visualizerKey);
  }
}
