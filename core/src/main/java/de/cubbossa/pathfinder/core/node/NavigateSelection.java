package de.cubbossa.pathfinder.core.node;

import java.util.Collection;
import java.util.HashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NavigateSelection extends HashSet<Navigable> {

  public NavigateSelection(Collection<Navigable> collection) {
    super(collection);
  }
}
