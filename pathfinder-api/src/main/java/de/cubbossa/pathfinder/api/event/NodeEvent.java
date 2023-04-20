package de.cubbossa.pathfinder.api.event;

import de.cubbossa.pathfinder.api.node.Node;

public interface NodeEvent extends PathFinderEvent {
	Node<?> getNode();
}
