package de.cubbossa.pathfinder.node;

import java.util.Collection;
import java.util.HashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NavigateSelection extends HashSet<Node> {

  public NavigateSelection(Collection<Node> collection) {
    super(collection);
  }
}
