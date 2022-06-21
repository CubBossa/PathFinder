package de.bossascrew.pathfinder.node;

import java.util.Collection;

public interface Navigable {

	/**
	 * @return The keys that identify this navigable.
	 * Does not have to be unique. Player search for findable
	 * will be resolved in all findables that have a matching searchterm.
	 */
	Collection<String> getSearchTerms();

	/**
	 * @return The group of all nodes that belong to this navigable.
	 * This might be just one node for single nodes that implement navigable.
	 * Group content for groups.
	 */
	Collection<Node> getGroup();
}
