package de.cubbossa.pathfinder.node.attribute;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NumberRange;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.stream.Collectors;
import lombok.Getter;

public class DistanceSelectionAttribute extends AbstractNodeSelectionParser.NodeSelectionArgument<NumberRange> {

  @Getter
  private final String key = "distance";

  public DistanceSelectionAttribute() {
    super(r -> NumberRange.fromString(r.getRemaining()));

    execute(c -> {
      Location senderLocation = c.getSenderLocation();
      return c.getScope().stream()
          .filter(n -> c.getValue().contains(n.getLocation().distance(senderLocation)))
          .collect(Collectors.toList());
    });
  }

  @Override
  public SelectionParser.SelectionModification modificationType() {
    return SelectionParser.SelectionModification.FILTER;
  }
}
