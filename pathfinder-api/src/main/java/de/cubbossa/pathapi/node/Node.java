package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.misc.Location;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface Node extends Comparable<Node> {

  UUID getNodeId();

  Location getLocation();

  void setLocation(Location location);

  Collection<Edge> getEdges();

  default Optional<Edge> connect(Node other) {
    return connect(other.getNodeId());
  }

  default Optional<Edge> connect(UUID other) {
    return connect(other, 1);
  }

  default Optional<Edge> connect(Node other, double weight) {
    return connect(other.getNodeId(), weight);
  }

  Optional<Edge> connect(UUID other, double weight);

  default void disconnectAll() {
    getEdges().clear();
  }

  default Optional<Edge> disconnect(Node other) {
    return disconnect(other.getNodeId());
  }

  default Optional<Edge> disconnect(UUID other) {
    Optional<Edge> opt = getEdges().stream().filter(edge -> edge.getEnd().equals(other)).findAny();
    opt.ifPresent(edge -> getEdges().remove(edge));
    return opt;
  }

  default boolean hasConnection(Node other) {
    return hasConnection(other.getNodeId());
  }

  default boolean hasConnection(UUID other) {
    return getEdges().stream()
        .map(Edge::getEnd)
        .anyMatch(other::equals);
  }

  default Optional<Edge> getConnection(Node other) {
    return getConnection(other.getNodeId());
  }

  default Optional<Edge> getConnection(UUID other) {
    return getEdges().stream().filter(edge -> edge.getEnd().equals(other)).findAny();
  }


  @Override
  default int compareTo(@NotNull Node o) {
    return getNodeId().compareTo(o.getNodeId());
  }
}
