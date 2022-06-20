package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.Named;
import de.bossascrew.pathfinder.roadmap.RoadMap;

import java.util.Collection;

public interface Findable extends Named {


	Collection<String> getSearchTerms();

	/**
	 * @return The roadmap that this findable belongs to.
	 */
	RoadMap getRoadMap();

	/**
	 * @return The group of all nodes that belong to this findable.
	 * This might be just one node for single nodes that implement findable.
	 * Group content for groups.
	 */
	Collection<Node> getGroup();
}
