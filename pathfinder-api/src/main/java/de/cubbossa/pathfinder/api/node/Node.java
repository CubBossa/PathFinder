package de.cubbossa.pathfinder.api.node;

import java.util.Collection;
import java.util.UUID;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface Node<N extends Node<N>> extends Comparable<Node<?>> {

  UUID getNodeId();

  NodeType<N> getType();

  Location getLocation();

  void setLocation(Location location);

  Collection<Edge> getEdges();

  default boolean hasEdgeTo(Node<?> node) {
    return getEdges().stream()
        .map(Edge::getEnd)
        .anyMatch(uuid -> uuid.equals(node.getNodeId()));
  }

  @Override
  default int compareTo(@NotNull Node<?> o) {
    return getNodeId().compareTo(o.getNodeId());
  }
}
