package de.cubbossa.pathfinder.event;

import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.PathPlayer;
import java.time.LocalDateTime;

public interface PlayerDiscoverLocationEvent<PlayerT> extends PathFinderEvent {

  PathPlayer<PlayerT> getPlayer();

  NodeGroup getGroup();

  DiscoverableModifier getModifier();

  LocalDateTime getTimeStamp();
}
