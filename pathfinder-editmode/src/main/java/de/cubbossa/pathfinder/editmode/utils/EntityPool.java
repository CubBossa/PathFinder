package de.cubbossa.pathfinder.editmode.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Optional;

public class EntityPool<EntityT extends Entity> implements AutoCloseable {

  private boolean open = true;

  private final Class<EntityT> entityType;
  private final int size;
  private final HashSet<EntityT> free;
  private final HashSet<EntityT> claimed;

  public EntityPool(int size, Class<EntityT> type) {
    this.entityType = type;
    this.size = size;
    this.free = new HashSet<>();
    this.claimed = new HashSet<>();
  }

  private EntityT aquire(Location location) {
    if (location.getWorld() == null) {
      throw new IllegalArgumentException("World of location must not be null.");
    }
    EntityT e = location.getWorld().spawn(location, entityType);
    return e;
  }

  public EntityT get(Location location) {
    if (!open) {
      throw new IllegalStateException("Pool closed, make a new pool instance.");
    }

    Optional<EntityT> any = free.stream().findAny();
    if (any.isPresent() && any.get().isDead()) {
      free.remove(any.get());
      any = Optional.empty();
    }
    if (any.isEmpty()) {
      EntityT e = aquire(location);
      claimed.add(e);
      return e;
    }
    EntityT e = any.get();
    free.remove(e);
    claimed.add(e);
    if (!e.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
      throw new IllegalStateException("Could not successfully teleport entity.");
    }
    return e;
  }

  public boolean destroy(EntityT entity) {
    if (!open) {
      throw new IllegalStateException("Pool closed, make a new pool instance.");
    }
    if (!claimed.remove(entity)) {
      return false;
    }
    if (free.size() >= size) {
      entity.remove();
      return true;
    }
    free.add(entity);
    return true;
  }

  @Override
  public void close() {
    if (!open) {
      throw new IllegalStateException("Pool already closed.");
    }
    open = false;
    for (Entity e : free) {
      e.remove();
    }
    free.clear();
    for (Entity e : claimed) {
      e.remove();
    }
    claimed.clear();
  }
}
