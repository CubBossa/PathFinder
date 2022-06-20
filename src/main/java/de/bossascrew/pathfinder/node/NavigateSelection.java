package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.roadmap.RoadMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor
@Getter
public class NavigateSelection extends HashSet<Navigable> {

	private final RoadMap roadMap;

	public NavigateSelection(RoadMap roadMap, Collection<Navigable> collection) {
		super(collection);
		this.roadMap = roadMap;
	}
}
