package de.cubbossa.pathapi.node;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface NodeSelection extends List<Node> {

  static NodeSelection of(String selection) {
    if (NodeSelectionProvider.provider == null) {
      throw new IllegalStateException("NodeSelectionProvider not yet assigned!");
    }
    return NodeSelectionProvider.provider.of(selection);
  }

  static NodeSelection of(String selection, Iterable<Node> scope) {
    if (NodeSelectionProvider.provider == null) {
      throw new IllegalStateException("NodeSelectionProvider not yet assigned!");
    }
    return NodeSelectionProvider.provider.of(selection, scope);
  }

  static NodeSelection of(Iterable<Node> scope) {
    if (NodeSelectionProvider.provider == null) {
      throw new IllegalStateException("NodeSelectionProvider not yet assigned!");
    }
    return NodeSelectionProvider.provider.of(scope);
  }

  static NodeSelection ofSender(String selection, Object sender) {
    if (NodeSelectionProvider.provider == null) {
      throw new IllegalStateException("NodeSelectionProvider not yet assigned!");
    }
    return NodeSelectionProvider.provider.ofSender(selection, sender);
  }

  static NodeSelection ofSender(String selection, Iterable<Node> scope, Object sender) {
    if (NodeSelectionProvider.provider == null) {
      throw new IllegalStateException("NodeSelectionProvider not yet assigned!");
    }
    return NodeSelectionProvider.provider.ofSender(selection, scope, sender);
  }

  @Nullable String getSelectionString();

  default Collection<UUID> getIds() {
    return this.stream().map(Node::getNodeId).toList();
  }

  @Contract(pure = true)
  default NodeSelection apply(String selectionFilter) {
    return of(selectionFilter, this);
  }
}
