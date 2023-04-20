package de.cubbossa.pathfinder.api.event;

import de.cubbossa.pathfinder.api.node.Node;

public interface NodeDeleteEvent extends PathFinderEvent {
	Node<?> getNode();
}
