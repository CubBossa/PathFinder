package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.node.Node;

public interface NodeEvent extends PathFinderEvent {
  Node getNode();
}
