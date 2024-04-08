package de.cubbossa.pathfinder.event;

import de.cubbossa.pathfinder.node.Node;

public interface NodeEvent extends PathFinderEvent {
  Node getNode();
}
