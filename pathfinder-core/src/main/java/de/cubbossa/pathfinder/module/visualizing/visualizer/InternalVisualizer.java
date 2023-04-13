package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;

public abstract class InternalVisualizer<T extends PathVisualizer<T, D>, D>
		extends AbstractVisualizer<T, D>
		implements PathVisualizer<T, D> {
}
