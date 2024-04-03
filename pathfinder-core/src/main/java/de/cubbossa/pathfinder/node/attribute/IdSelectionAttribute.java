package de.cubbossa.pathfinder.node.attribute;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;

public class IdSelectionAttribute extends AbstractNodeSelectionParser.NodeSelectionArgument<UUID> {

  @Getter
  private final String key = "id";

  public IdSelectionAttribute() {
    super(r -> UUID.fromString(r.getRemaining()));

    execute(c -> {
      return c.getScope().stream()
          .filter(n -> c.getValue().equals(n.getNodeId()))
          .collect(Collectors.toList());
    });

    suggestStrings(c -> {
      return PathFinderProvider.get().getStorage().loadNodes().join().stream()
          .map(Node::getNodeId)
          .map(integer -> integer + "")
          .collect(Collectors.toList());
    });
  }

  @Override
  public SelectionParser.SelectionModification modificationType() {
    return SelectionParser.SelectionModification.FILTER;
  }
}
