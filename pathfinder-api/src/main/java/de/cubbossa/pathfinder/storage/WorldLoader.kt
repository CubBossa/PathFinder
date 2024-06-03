package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.misc.World;
import java.util.UUID;

@FunctionalInterface
public interface WorldLoader {

  World loadWorld(UUID uuid);
}
