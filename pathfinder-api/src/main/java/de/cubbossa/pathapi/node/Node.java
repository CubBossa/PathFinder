package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.misc.Location;
import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface Node extends Comparable<Node> {

  UUID getNodeId();

  Location getLocation();

  void setLocation(Location location);

  Collection<Edge> getEdges();

  default boolean hasEdgeTo(Node node) {
    return getEdges().stream()
        .map(Edge::getEnd)
        .anyMatch(uuid -> uuid.equals(node.getNodeId()));
  }

  @Override
  default int compareTo(@NotNull Node o) {
    return getNodeId().compareTo(o.getNodeId());
  }
}
