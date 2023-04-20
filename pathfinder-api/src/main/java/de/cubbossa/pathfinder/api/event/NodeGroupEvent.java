package de.cubbossa.pathfinder.api.event;

import de.cubbossa.pathfinder.api.group.NodeGroup;

public interface NodeGroupEvent extends PathFinderEvent {
	NodeGroup getGroup();
}
