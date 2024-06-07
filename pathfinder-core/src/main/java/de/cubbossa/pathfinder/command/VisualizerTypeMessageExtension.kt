package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.translations.Message;

public interface VisualizerTypeMessageExtension<VisualizerT extends PathVisualizer<?, ?>> {

  Message getInfoMessage(VisualizerT element);
}
