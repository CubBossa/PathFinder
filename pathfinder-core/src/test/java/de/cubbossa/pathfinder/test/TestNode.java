package de.cubbossa.pathfinder.test;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class TestNode implements Node {

  private final UUID nodeId;
  private Location location;

  @Override
  public Collection<Edge> getEdges() {
    return null;
  }
}
