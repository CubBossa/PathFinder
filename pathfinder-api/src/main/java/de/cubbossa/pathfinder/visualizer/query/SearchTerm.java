package de.cubbossa.pathfinder.visualizer.query;

import java.util.Collection;

public interface SearchTerm {

  String getIdentifier();

  boolean matches(Collection<SearchQueryAttribute> attributes);
}
