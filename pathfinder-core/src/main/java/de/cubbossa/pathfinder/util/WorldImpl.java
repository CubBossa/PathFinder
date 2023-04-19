package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.api.misc.World;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;

public class WorldImpl implements World {

  private final UUID uuid;
  private org.bukkit.World world;

  public WorldImpl(UUID worldId) {
    this.uuid = worldId;
  }

  @Override
  public UUID getUniqueId() {
    return uuid;
  }

  @Override
  public String getName() {
    return resolve().getName();
  }

  private org.bukkit.World resolve() {
    if (world == null) {
      world = Bukkit.getWorld(uuid);
    }
    return world;
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorldImpl world = (WorldImpl) o;
    return Objects.equals(uuid, world.uuid);
  }
}
