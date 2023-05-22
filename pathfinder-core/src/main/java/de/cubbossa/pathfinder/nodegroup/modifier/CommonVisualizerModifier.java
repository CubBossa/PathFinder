package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.VisualizerModifier;
import de.cubbossa.pathapi.visualizer.PathVisualizer;

public record CommonVisualizerModifier(PathVisualizer<?, ?> visualizer) implements Modifier, VisualizerModifier {

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
