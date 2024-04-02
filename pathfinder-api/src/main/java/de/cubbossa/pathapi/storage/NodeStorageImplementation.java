package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Node;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface NodeStorageImplementation<N extends Node> {

  N createAndLoadNode(Context context);

  Optional<N> loadNode(UUID uuid);

  Collection<N> loadNodes(Collection<UUID> ids);

  Collection<N> loadAllNodes();

  void saveNode(N node);

  void deleteNodes(Collection<N> node);

  record Context(Location location) {
  }
}
