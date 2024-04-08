package de.cubbossa.pathfinder.event;

import de.cubbossa.pathfinder.misc.PathPlayer;

public interface PathStoppedEvent<PlayerT> extends PathEvent<PlayerT> {

  PathPlayer<PlayerT> getPlayer();
}
