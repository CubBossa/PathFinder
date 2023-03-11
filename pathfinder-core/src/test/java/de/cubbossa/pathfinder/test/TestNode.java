package de.cubbossa.pathfinder.test;

import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import java.util.Collection;
import java.util.HashSet;
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

  private final int nodeId;
  private final NamespacedKey roadMapKey;
  private Location location;
  private boolean persistent;

  @Override
  public Collection<Node<?>> getGroup() {
    return new HashSet<>();
  }

  @Override
  public NodeType<TestNode> getType() {
    return null;
  }

  @Override
  public Collection<Edge> getEdges() {
    return null;
  }

  @Override
  public @Nullable Double getCurveLength() {
    return null;
  }

  @Override
  public void setCurveLength(Double value) {

  }

  @Override
  public Edge connect(Node<?> target) {
    return null;
  }

  @Override
  public void disconnect(Node<?> target) {

  }

  @Override
  public Collection<SearchTerm> getSearchTerms() {
    return null;
  }
}
