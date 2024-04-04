package de.cubbossa.pathfinder.node.selection.attribute;

import com.google.auto.service.AutoService;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.util.CollectionUtils;
import java.util.List;
import lombok.Getter;

@Getter
@AutoService(NodeSelectionAttribute.class)
public class OffsetSelectionAttribute implements NodeSelectionAttribute<Integer> {

  private final String key = "offset";

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
    return CollectionUtils.subList(context.getScope(), context.getValue());
  }
}
