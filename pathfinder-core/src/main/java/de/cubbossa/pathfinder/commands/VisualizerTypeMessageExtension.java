package de.cubbossa.pathfinder.commands;

import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.translations.Message;

public interface VisualizerTypeMessageExtension<VisualizerT extends PathVisualizer<?, ?>> {

  Message getInfoMessage(VisualizerT element);
}
