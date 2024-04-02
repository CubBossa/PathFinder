package de.cubbossa.pathfinder.navigationquery;

import de.cubbossa.pathapi.visualizer.query.SearchQueryAttribute;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SearchTermImpl implements SearchTerm {

  private final String identifier;

  @Override
  public boolean matches(Collection<SearchQueryAttribute> attributes) {
    return true;
  }
}
