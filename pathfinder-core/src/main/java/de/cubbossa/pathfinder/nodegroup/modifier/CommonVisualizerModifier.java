package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.VisualizerModifier;
import de.cubbossa.pathapi.visualizer.PathVisualizer;

public record CommonVisualizerModifier(PathVisualizer<?, ?> visualizer) implements Modifier, VisualizerModifier {
}
