package de.cubbossa.pathfinder.node.selection.attribute;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.cubbossa.pathfinder.misc.Range;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.util.CollectionUtils;
import java.util.List;
import lombok.Getter;
import org.pf4j.Extension;

@Getter
@Extension(points = NodeSelectionAttribute.class)
public class LimitSelectionAttribute implements NodeSelectionAttribute<Integer> {

  private final String key = "limit";
  private final List<String> executeAfter = List.of("offset");

  @Override
  public ArgumentType<Integer> getValueType() {
    return IntegerArgumentType.integer(0);
  }

  @Override
  public Type getAttributeType() {
    return Type.FILTER;
  }

  @Override
  public List<Node> execute(AbstractNodeSelectionParser.NodeArgumentContext<Integer> context) {
    return CollectionUtils.subList(context.getScope(), Range.range(0, context.getValue()));
  }
}
