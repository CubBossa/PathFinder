package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.visualizer.VisualizerPath;

public interface PathEvent<PlayerT> extends PathFinderEvent {

  VisualizerPath<PlayerT> getPath();
}
