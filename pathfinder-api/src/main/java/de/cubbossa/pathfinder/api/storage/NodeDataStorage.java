package de.cubbossa.pathfinder.api.storage;

import de.cubbossa.pathfinder.api.node.Node;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

public interface NodeDataStorage<N extends Node<N>> {

  N createAndLoadNode(Context context);
  Optional<N> loadNode(UUID uuid);
  Collection<N> loadNodes(Collection<UUID> ids);
  Collection<N> loadAllNodes();
  void saveNode(N node);
  void deleteNode(N node);

  record Context(Location location) {}
}
