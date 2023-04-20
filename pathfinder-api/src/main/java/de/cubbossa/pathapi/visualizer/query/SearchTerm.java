package de.cubbossa.pathapi.visualizer.query;

import java.util.Collection;

public interface SearchTerm {

  String getIdentifier();

  boolean matches(Collection<SearchQueryAttribute> attributes);
}
