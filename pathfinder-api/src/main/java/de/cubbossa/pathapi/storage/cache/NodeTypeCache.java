package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface NodeTypeCache extends StorageCache<UUID> {

  <N extends Node> NodeType<N> getType(UUID uuid, Function<UUID, Optional<NodeType<N>>> loader);

  Map<UUID, NodeType<?>> getTypes(Collection<UUID> uuids,
                                  Function<Collection<UUID>, Map<UUID, NodeType<?>>> loader);

  void write(UUID uuid, NodeType<?> type);
}
