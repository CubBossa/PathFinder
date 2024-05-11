package de.cubbossa.pathfinder.node.selection.attribute;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.pf4j.Extension;

@Getter
@Extension(points = NodeSelectionAttribute.class)
public class IdSelectionAttribute implements NodeSelectionAttribute<UUID> {

  private final String key = "id";

  @Override
  public ArgumentType<UUID> getValueType() {
    return r -> UUID.fromString(r.getRemaining());
  }

  @Override
  public Type getAttributeType() {
    return Type.FILTER;
  }

  @Override
  public List<Node> execute(AbstractNodeSelectionParser.NodeArgumentContext<UUID> context) {
    return context.getScope().stream()
        .filter(n -> context.getValue().equals(n.getNodeId()))
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getStringSuggestions(SelectionParser.SuggestionContext context) {
    return PathFinder.get().getStorage().loadNodes().join().stream()
        .map(Node::getNodeId)
        .map(integer -> integer + "")
        .collect(Collectors.toList());
  }
}
