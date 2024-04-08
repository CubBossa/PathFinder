package de.cubbossa.pathfinder.event;

import de.cubbossa.pathfinder.visualizer.VisualizerPath;

public interface PathEvent<PlayerT> extends PathFinderEvent {

  VisualizerPath<PlayerT> getPath();
}
