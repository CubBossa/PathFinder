package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.translations.Message;

public interface VisualizerTypeMessageExtension<VisualizerT extends PathVisualizer<?, ?>> {

  Message getInfoMessage(VisualizerT element);
}
