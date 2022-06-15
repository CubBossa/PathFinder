package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.data.findable.NavigationTarget;

import java.util.ArrayList;
import java.util.Collection;

public class NodeSelection extends ArrayList<NavigationTarget> {

	public NodeSelection(Collection<NavigationTarget> other) {
		super(other);
	}

}
