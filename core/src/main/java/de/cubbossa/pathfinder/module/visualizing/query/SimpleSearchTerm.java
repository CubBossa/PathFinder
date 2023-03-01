package de.cubbossa.pathfinder.module.visualizing.query;

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
