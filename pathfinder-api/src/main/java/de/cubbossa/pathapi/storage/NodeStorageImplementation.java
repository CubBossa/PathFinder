package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Node;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface NodeStorageImplementation<N extends Node> {

  default boolean canBeCreated(Context context) {
    return true;
  }

  @Nullable N createAndLoadNode(Context context);

  Optional<N> loadNode(UUID uuid);

  Collection<N> loadNodes(Collection<UUID> ids);

  Collection<N> loadAllNodes();

  void saveNode(N node);

  void deleteNodes(Collection<N> node);

  record Context(Location location) {
  }
}
