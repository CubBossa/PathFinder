package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.NodeType;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface NodeTypeCache extends StorageCache<UUID> {

  <N extends Node> Optional<NodeType<N>> getType(UUID uuid);

  CacheMap<UUID, NodeType<?>> getTypes(Collection<UUID> uuids);

  void write(UUID uuid, NodeType<?> type);
}
