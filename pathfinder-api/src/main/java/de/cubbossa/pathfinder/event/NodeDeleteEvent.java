package de.cubbossa.pathfinder.event;

import de.cubbossa.pathfinder.node.Node;

public interface NodeDeleteEvent extends PathFinderEvent {
  Node getNode();
}
