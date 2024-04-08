package de.cubbossa.pathfinder.visualizer.query;

import java.util.Collection;

public interface SearchTermHolder {

  Collection<SearchTerm> getSearchTerms();

  void addSearchTerms(Collection<SearchTerm> searchTerms);

  void removeSearchTerms(Collection<SearchTerm> searchTerms);

  void clearSearchTerms();

  boolean matches(SearchTerm searchTerm);

  boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes);

  boolean matches(String term);

  boolean matches(String term, Collection<SearchQueryAttribute> attributes);
}
