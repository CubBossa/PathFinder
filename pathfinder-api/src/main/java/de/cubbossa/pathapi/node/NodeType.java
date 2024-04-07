package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.storage.NodeStorageImplementation;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface NodeType<N extends Node> extends Keyed, NodeStorageImplementation<N> {

  default boolean canBeCreated(Context context) {
    return true;
  }

  @Nullable N createAndLoadNode(Context context);

  record Context(UUID id, Location location) {
  }
}
