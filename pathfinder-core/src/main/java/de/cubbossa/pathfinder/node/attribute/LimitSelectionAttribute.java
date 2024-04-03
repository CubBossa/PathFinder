package de.cubbossa.pathfinder.node.attribute;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.cubbossa.pathapi.misc.Range;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.util.CollectionUtils;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collection;
import java.util.List;
import lombok.Getter;

public class LimitSelectionAttribute extends AbstractNodeSelectionParser.NodeSelectionArgument<Integer> {

  @Getter
  private final String key = "limit";
  private final List<String> executeAfter = List.of("offset");

  public LimitSelectionAttribute() {
    super(IntegerArgumentType.integer(0));

    execute(c -> CollectionUtils.subList(c.getScope(), Range.range(0, c.getValue())));
  }

  @Override
  public SelectionParser.SelectionModification modificationType() {
    return SelectionParser.SelectionModification.FILTER;
  }

  @Override
  public Collection<String> executeAfter() {
    return executeAfter;
  }
}
