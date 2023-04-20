package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.node.Node;

public interface NodeDeleteEvent extends PathFinderEvent {
	Node<?> getNode();
}
