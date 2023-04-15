package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.translations.Message;

public interface VisualizerTypeMessageExtension<Visualizer extends PathVisualizer<Visualizer, ?, ?>> {

  Message getInfoMessage(Visualizer element);
}
