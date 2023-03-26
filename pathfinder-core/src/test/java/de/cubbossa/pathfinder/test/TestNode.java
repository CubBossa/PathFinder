package de.cubbossa.pathfinder.test;

import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
@Setter
public class TestNode implements Node<TestNode> {

  private final UUID nodeId;
  private Location location;

  @Override
  public NodeType<TestNode> getType() {
    return null;
  }

  @Override
  public Collection<Edge> getEdges() {
    return null;
  }
}
