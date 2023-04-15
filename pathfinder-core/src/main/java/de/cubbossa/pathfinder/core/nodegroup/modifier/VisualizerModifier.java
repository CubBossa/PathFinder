package de.cubbossa.pathfinder.core.nodegroup.modifier;

import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;

public record VisualizerModifier(PathVisualizer<?, ?, ?> visualizer) implements Modifier {
}
