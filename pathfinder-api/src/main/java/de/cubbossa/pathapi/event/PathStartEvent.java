package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.misc.PathPlayer;

public interface PathStartEvent<PlayerT> extends PathEvent<PlayerT> {

  PathPlayer<PlayerT> getPlayer();
}
