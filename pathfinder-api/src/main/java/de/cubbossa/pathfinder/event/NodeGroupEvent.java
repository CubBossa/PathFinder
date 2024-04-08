package de.cubbossa.pathfinder.event;

import de.cubbossa.pathfinder.group.NodeGroup;

public interface NodeGroupEvent extends PathFinderEvent {
  NodeGroup getGroup();
}
