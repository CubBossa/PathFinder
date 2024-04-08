package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.misc.World;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
    return resolve().map(org.bukkit.World::getName).orElse("-Unknown World-");
  }

  private Optional<org.bukkit.World> resolve() {
    if (world == null) {
      world = Bukkit.getWorld(uuid);
    }
    return Optional.ofNullable(world);
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
