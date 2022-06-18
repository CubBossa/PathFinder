package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.Named;

import java.util.Collection;

public interface Navigable extends Named {

	Collection<String> getSearchTerms();
}
