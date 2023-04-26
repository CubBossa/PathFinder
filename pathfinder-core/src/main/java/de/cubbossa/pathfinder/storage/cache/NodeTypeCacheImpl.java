package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.cache.NodeTypeCache;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class NodeTypeCacheImpl implements NodeTypeCache {

  private final Map<UUID, NodeType<?>> types = new HashMap<>();

  @Override
  public <N extends Node> NodeType<N> getType(UUID uuid,
                                              Function<UUID, Optional<NodeType<N>>> loader) {
    if (!types.containsKey(uuid)) {
      NodeType<N> type = loader.apply(uuid).orElseThrow(
          () -> new RuntimeException("Could not access type of node '" + uuid + "'.")
      );
      types.put(uuid, type);
    }
    return (NodeType<N>) types.get(uuid);
  }

  @Override
  public Map<UUID, NodeType<?>> getTypes(Collection<UUID> uuids,
                                         Function<Collection<UUID>, Map<UUID, NodeType<?>>> loader) {
    return null;
  }

  @Override
  public void write(UUID uuid, NodeType<?> type) {

  }

  @Override
  public void write(UUID uuid) {

  }

  @Override
  public void invalidate(UUID uuid) {

  }

  @Override
  public void invalidateAll() {

  }
}
