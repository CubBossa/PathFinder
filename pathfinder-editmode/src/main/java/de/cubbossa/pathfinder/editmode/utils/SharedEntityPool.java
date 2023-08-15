package de.cubbossa.pathfinder.editmode.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@NotThreadSafe
public class SharedEntityPool<KeyT, EntityT extends Entity> implements AutoCloseable {

  private final JavaPlugin plugin;
  private final EntityPool<EntityT> pool;
  private final Map<KeyT, EntityT> mapping;
  private final Map<KeyT, AtomicInteger> viewers;

  public SharedEntityPool(JavaPlugin plugin, int size, Class<EntityT> typeClass) {
    this.plugin = plugin;
    this.pool = new EntityPool<>(size, typeClass);
    this.mapping = new HashMap<>();
    this.viewers = new HashMap<>();
  }

  public EntityT get(Location location, KeyT key, Player player) {
    EntityT e = mapping.get(key);
    if (e == null) {
      e = pool.get(location);
      mapping.put(key, e);
    }
    viewers.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    player.showEntity(plugin, e);
    return e;
  }

  public void destroy(KeyT key, Player player) {
    EntityT e = mapping.get(key);
    if (e == null) {
      throw new IllegalStateException("Cannot destroy entity that is not rendered.");
    }
    AtomicInteger renderCount = viewers.get(key);
    if (renderCount.get() > 1) {
      renderCount.decrementAndGet();
      player.hideEntity(plugin, e);
      return;
    }
    mapping.remove(key);
    renderCount.set(0);
    pool.destroy(e);
  }

  @Override
  public void close() throws Exception {
    pool.close();
  }
}
