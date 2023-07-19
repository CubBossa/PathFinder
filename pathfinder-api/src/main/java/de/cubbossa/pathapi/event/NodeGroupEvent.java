package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.group.NodeGroup;

public interface NodeGroupEvent extends PathFinderEvent {
  NodeGroup getGroup();
}
