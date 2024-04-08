package de.cubbossa.pathfinder.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor
@Getter
public class NavigateSelection extends HashSet<Node> {

  public NavigateSelection(Collection<Node> collection) {
    super(collection);
  }
}
