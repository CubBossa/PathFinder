package de.cubbossa.pathfinder.module.visualizing.query;

import java.util.Collection;

public interface SearchTermHolder {

	Collection<SearchTerm> getSearchTerms();

	boolean matches(SearchTerm searchTerm);

	boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes);

	boolean matches(String term);

	boolean matches(String term, Collection<SearchQueryAttribute> attributes);
}
