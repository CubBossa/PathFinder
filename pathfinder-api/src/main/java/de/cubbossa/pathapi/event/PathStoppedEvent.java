package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.misc.PathPlayer;

public interface PathStoppedEvent<PlayerT> extends PathEvent<PlayerT> {

  PathPlayer<PlayerT> getPlayer();
}
