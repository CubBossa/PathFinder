package de.cubbossa.pathfinder.node;

import de.cubbossa.pathfinder.Changes;
import de.cubbossa.pathfinder.misc.Location;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * A node instance is the main structure of the virtual graph that is managed by PathFinder.
 * They are persistent data of the application that the user specifies to defines possible paths.
 * Actual pathfinding will happen on a graph built conditionally from all nodes.
 *
 * @see Edge serves as connecting structure.
 */
public interface Node extends Comparable<Node>, Cloneable {

  /**
   * The UUID of nodes must be unique for each node and serves as primary key.
   *
   * @return The UUID of this Node.
   */
  UUID getNodeId();

  /**
   * The current location of this Node. A location consists of a vec3 and a world. Worlds are abstract
   * and must not be minecraft worlds, bust most commonly are. For example, a world could also resemble a website.
   *
   * @return A referenced location of this Node.
   */
  Location getLocation();

  void setLocation(Location location);

  Changes<Edge> getEdgeChanges();

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

  Node clone();

  Node clone(UUID id);
}
