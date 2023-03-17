package de.cubbossa.pathfinder.core.nodegroup.modifier;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;

public record VisualizerModifier(PathVisualizer<?, ?> visualizer) implements Modifier {
}
