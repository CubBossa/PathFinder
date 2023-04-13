package de.cubbossa.pathfinder.api.visualizer.query;

import java.util.Collection;

public interface SearchTermHolder {

  Collection<SearchTerm> getSearchTerms();

  default void addSearchTerms(Collection<SearchTerm> searchTerms) {
  }

  default void removeSearchTerms(Collection<SearchTerm> searchTerms) {
  }

  default void clearSearchTerms() {
  }

  default boolean matches(SearchTerm searchTerm) {
    return false;
  }

  default boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes) {
    return false;
  }

  default boolean matches(String term) {
    return false;
  }

  default boolean matches(String term, Collection<SearchQueryAttribute> attributes) {
    return false;
  }
}
