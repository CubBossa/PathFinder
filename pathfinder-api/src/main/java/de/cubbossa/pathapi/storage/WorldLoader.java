package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.misc.World;

import java.util.UUID;

@FunctionalInterface
public interface WorldLoader {

  World loadWorld(UUID uuid);
}
