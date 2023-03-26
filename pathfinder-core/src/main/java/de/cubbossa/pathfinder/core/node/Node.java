package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.PersistencyHolder;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Node<N extends Node<N>> extends Comparable<Node<?>> {

  UUID getNodeId();

  NodeType<N> getType();

  Location getLocation();

  void setLocation(Location location);

  Collection<Edge> getEdges();

  @Override
  default int compareTo(@NotNull Node<?> o) {
    return getNodeId().compareTo(o.getNodeId());
  }
}
