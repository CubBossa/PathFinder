package de.cubbossa.pathfinder.event;

import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.PathPlayer;

public interface PlayerDiscoverProgressEvent<PlayerT> extends PathFinderEvent {

  PathPlayer<PlayerT> getPlayer();

  NodeGroup getFoundGroup();

  NodeGroup getProgressObserverGroup();
}
