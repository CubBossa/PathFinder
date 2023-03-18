package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.core.node.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

public class NodeSelection extends ArrayList<UUID> {

  @Getter
  @Setter
  private @Nullable Meta meta = null;

  public NodeSelection() {
  }

  public NodeSelection(UUID... uuids) {
    super(Arrays.asList(uuids));
  }

  public NodeSelection(Node<?>... nodes) {
    super(Arrays.stream(nodes).map(Node::getNodeId).toList());
  }

  public NodeSelection(Collection<Node<?>> other) {
    super(other.stream().map(Node::getNodeId).toList());
  }

  public record Meta(String selector, Map<String, String> arguments) {
  }
}
