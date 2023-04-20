package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.visualizer.PathVisualizer;

public record VisualizerModifier(PathVisualizer<?, ?, ?> visualizer) implements Modifier {
}
