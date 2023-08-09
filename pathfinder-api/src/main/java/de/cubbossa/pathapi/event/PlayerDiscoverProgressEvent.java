package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;

public interface PlayerDiscoverProgressEvent<PlayerT> extends PathFinderEvent {

  PathPlayer<PlayerT> getPlayer();

  NodeGroup getFoundGroup();

  NodeGroup getProgressObserverGroup();
}
