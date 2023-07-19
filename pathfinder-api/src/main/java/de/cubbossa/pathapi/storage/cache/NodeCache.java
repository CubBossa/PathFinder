package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.Node;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface NodeCache extends StorageCache<Node> {

  <N extends Node> Optional<N> getNode(UUID uuid);

  Optional<Collection<Node>> getAllNodes();

  CacheCollection<UUID, Node> getNodes(Collection<UUID> ids);

  void writeAll(Collection<Node> nodes);

  void write(NodeGroup group);

  void invalidate(UUID uuid);
}
