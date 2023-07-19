package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;

public interface PlayerForgetLocationEvent<PlayerT> extends PathFinderEvent {

  PathPlayer<PlayerT> getPlayer();

  NodeGroup getGroup();

  DiscoverableModifier getModifier();
}
