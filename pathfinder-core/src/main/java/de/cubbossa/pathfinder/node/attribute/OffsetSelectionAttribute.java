package de.cubbossa.pathfinder.node.attribute;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.util.CollectionUtils;
import de.cubbossa.pathfinder.util.SelectionParser;
import lombok.Getter;

public class OffsetSelectionAttribute extends AbstractNodeSelectionParser.NodeSelectionArgument<Integer> {

  @Getter
  private final String key = "offset";

  public OffsetSelectionAttribute() {
    super(IntegerArgumentType.integer(0));

    execute(c -> CollectionUtils.subList(c.getScope(), c.getValue()));
  }

  @Override
  public SelectionParser.SelectionModification modificationType() {
    return SelectionParser.SelectionModification.FILTER;
  }
}
