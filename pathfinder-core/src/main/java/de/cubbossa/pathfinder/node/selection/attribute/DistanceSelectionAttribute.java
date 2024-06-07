package de.cubbossa.pathfinder.node.selection.attribute;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.node.selection.NumberRange;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.pf4j.Extension;

@Getter
@Extension(points = NodeSelectionAttribute.class)
public class DistanceSelectionAttribute implements NodeSelectionAttribute<NumberRange> {

  private final String key = "distance";

  @Override
  public ArgumentType<NumberRange> getValueType() {
    return r -> NumberRange.fromString(r.getRemaining());
  }

  @Override
  public Type getAttributeType() {
    return Type.FILTER;
  }

  @Override
  public List<Node> execute(AbstractNodeSelectionParser.NodeArgumentContext<NumberRange> context) {
    Location senderLocation = context.senderLocation;
    if (senderLocation == null) {
      return Collections.emptyList();
    }
    return context.getScope().stream()
        .filter(n -> context.getValue().contains(n.getLocation().distance(senderLocation)))
        .collect(Collectors.toList());
  }

  @Override
  public List<Suggestion> getSuggestions(SelectionParser.SuggestionContext context) {
    return null;
  }
}
