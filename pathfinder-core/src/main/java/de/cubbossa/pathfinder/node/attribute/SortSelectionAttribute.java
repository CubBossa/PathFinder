package de.cubbossa.pathfinder.node.attribute;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.Getter;

public class SortSelectionAttribute extends AbstractNodeSelectionParser.NodeSelectionArgument<SortSelectionAttribute.SortMethod> {

  @Getter
  private final String key = "sort";

  public SortSelectionAttribute() {
    super(r -> SortMethod.valueOf(r.getRemaining().toUpperCase()));

    execute(c -> {
      Location playerLocation = c.getSenderLocation();
      return switch (c.getValue()) {
        case NEAREST -> c.getScope().stream()
            .sorted(Comparator.comparingDouble(o -> o.getLocation().distance(playerLocation)))
            .collect(Collectors.toList());
        case FURTHEST -> c.getScope().stream()
            .sorted((o1, o2) -> Double.compare(o2.getLocation().distance(playerLocation),
                o1.getLocation().distance(playerLocation)))
            .collect(Collectors.toList());
        case RANDOM -> c.getScope().stream()
            .collect(Collectors.collectingAndThen(Collectors.toList(), n -> {
              Collections.shuffle(n);
              return n;
            }));
        case ARBITRARY -> c.getScope().stream()
            .sorted()
            .collect(Collectors.toList());
      };
    });

    suggestStrings(Lists.newArrayList("nearest", "furthest", "random", "arbitrary"));
  }

  public enum SortMethod {
    NEAREST, FURTHEST, RANDOM, ARBITRARY;
  }

  @Override
  public SelectionParser.SelectionModification modificationType() {
    return SelectionParser.SelectionModification.SORT;
  }
}
