package de.cubbossa.pathfinder.node.selection.attribute;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.pf4j.Extension;

@Getter
@Extension(points = NodeSelectionAttribute.class)
public class SortSelectionAttribute implements NodeSelectionAttribute<SortSelectionAttribute.SortMethod> {

  private final String key = "sort";

  @Override
  public ArgumentType<SortMethod> getValueType() {
    return r -> SortMethod.valueOf(r.getRemaining().toUpperCase());
  }

  @Override
  public Type getAttributeType() {
    return Type.SORT;
  }

  @Override
  public List<Node> execute(AbstractNodeSelectionParser.NodeArgumentContext<SortMethod> context) {
    Location playerLocation = context.senderLocation;
    return switch (context.getValue()) {
      case NEAREST -> context.getScope().stream()
          .sorted(Comparator.comparingDouble(o -> o.getLocation().distance(playerLocation)))
          .collect(Collectors.toList());
      case FURTHEST -> context.getScope().stream()
          .sorted((o1, o2) -> Double.compare(o2.getLocation().distance(playerLocation),
              o1.getLocation().distance(playerLocation)))
          .collect(Collectors.toList());
      case RANDOM -> context.getScope().stream()
          .collect(Collectors.collectingAndThen(Collectors.toList(), n -> {
            Collections.shuffle(n);
            return n;
          }));
      case ARBITRARY -> context.getScope().stream()
          .sorted()
          .collect(Collectors.toList());
    };
  }

  @Override
  public List<String> getStringSuggestions(SelectionParser.SuggestionContext context) {
    return Lists.newArrayList("nearest", "furthest", "random", "arbitrary");
  }

  public enum SortMethod {
    NEAREST, FURTHEST, RANDOM, ARBITRARY;
  }
}
