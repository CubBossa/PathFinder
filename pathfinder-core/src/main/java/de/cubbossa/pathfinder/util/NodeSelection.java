package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.node.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

public class NodeSelection extends ArrayList<Node<?>> {

  @Getter
  @Setter
  private @Nullable Meta meta = null;

  public NodeSelection() {
  }

  public NodeSelection(Collection<Node<?>> nodes) {
    super(nodes);
  }

  public NodeSelection(Node<?>... nodes) {
    super(Arrays.stream(nodes).toList());
  }

  public record Meta(String selector, Map<String, String> arguments) {
  }
}
