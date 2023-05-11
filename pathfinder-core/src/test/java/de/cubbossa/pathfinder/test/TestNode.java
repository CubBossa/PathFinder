package de.cubbossa.pathfinder.test;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestNode implements Node {

  private final UUID nodeId;
  private Location location;

  @Override
  public Collection<Edge> getEdges() {
    return new HashSet<>();
  }

  @Override
  public Optional<Edge> connect(UUID other, double weight) {
    return Optional.empty();
  }

  @Override
  public Node clone() {
    try {
      return (Node) super.clone();
    } catch (CloneNotSupportedException e) {
      return new TestNode(nodeId, location.clone());
    }
  }
}
