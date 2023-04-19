package de.cubbossa.pathfinder.test;

import de.cubbossa.pathfinder.api.misc.Location;
import de.cubbossa.pathfinder.api.node.Edge;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.core.node.SimpleEdge;
import de.cubbossa.pathfinder.api.node.Node;
import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class TestNode implements Node<TestNode> {

  private final UUID nodeId;
  private Location location;

  @Override
  public NodeType<de.cubbossa.pathfinder.test.TestNode> getType() {
    return null;
  }

  @Override
  public Collection<Edge> getEdges() {
    return null;
  }
}
