package de.cubbossa.pathfinder.module.visualizing.query;

import java.util.Collection;

public interface SearchTerm {

	String getIdentifier();

	boolean matches(Collection<SearchQueryAttribute> attributes);
}
