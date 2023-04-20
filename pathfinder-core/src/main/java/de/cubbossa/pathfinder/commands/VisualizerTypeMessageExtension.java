package de.cubbossa.pathfinder.commands;

import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.translations.Message;

public interface VisualizerTypeMessageExtension<Visualizer extends PathVisualizer<Visualizer, ?, ?>> {

  Message getInfoMessage(Visualizer element);
}
