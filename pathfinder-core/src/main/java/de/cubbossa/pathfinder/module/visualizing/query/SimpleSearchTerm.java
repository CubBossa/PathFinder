package de.cubbossa.pathfinder.module.visualizing.query;

import de.cubbossa.pathfinder.api.visualizer.query.SearchQueryAttribute;
import de.cubbossa.pathfinder.api.visualizer.query.SearchTerm;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleSearchTerm implements SearchTerm {

  @Getter
  private final String identifier;

  @Override
  public boolean matches(Collection<SearchQueryAttribute> attributes) {
    return true;
  }
}
