package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.cache.NodeTypeCache;

import java.util.*;

public class NodeTypeCacheImpl implements NodeTypeCache {

  private final Map<UUID, NodeType<?>> types = new HashMap<>();

  @Override
  public <N extends Node> Optional<NodeType<N>> getType(UUID uuid) {
    return Optional.ofNullable((NodeType<N>) types.get(uuid));
  }

  @Override
  public CacheMap<UUID, NodeType<?>> getTypes(Collection<UUID> uuids) {
    HashMap<UUID, NodeType<?>> present = new HashMap<>();
    Collection<UUID> absent = new HashSet<>();
    for (UUID uuid : uuids) {
      if (types.containsKey(uuid)) {
        present.put(uuid, types.get(uuid));
      } else {
        absent.add(uuid);
      }
    }
    return new CacheMap<>(present, absent);
  }

  @Override
  public void write(UUID uuid, NodeType<?> type) {
    types.put(uuid, type);
  }

  @Override
  public void write(UUID uuid) {
    throw new IllegalStateException("Please call 'write(UUID, NodeType<?>) instead");
  }

  @Override
  public void invalidate(UUID uuid) {
    types.remove(uuid);
  }

  @Override
  public void invalidateAll() {
    types.clear();
  }
}
