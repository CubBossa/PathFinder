package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.Node;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestNode implements Node {

  private String name = null;
  private final UUID nodeId;
  private Location location;
  private final Changes<Edge> edgeChanges = new Changes<>();

  public TestNode(UUID id, Location location) {
    this.nodeId = id;
    this.location = location;
  }

  public TestNode(String name, Location location) {
    this(UUID.randomUUID(), location);
    this.name = name;
  }

  @Override
  public Collection<Edge> getEdges() {
    return new HashSet<>();
  }

  @Override
  public Optional<Edge> connect(UUID other, double weight) {
    return Optional.empty();
  }

  @Override
  public String toString() {
    return "TestNode{" + (name == null ? "id=" + nodeId.toString() : "name=" + name) + "}";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Node n && getNodeId().equals(n.getNodeId());
  }

  @Override
  public int hashCode() {
    return getNodeId().hashCode();
  }

  @Override
  public Node clone() {
    return clone(nodeId);
  }

  @Override
  public Node clone(UUID id) {
    try {
      return (Node) super.clone();
    } catch (CloneNotSupportedException e) {
      return new TestNode(id, location.clone());
    }
  }
}
