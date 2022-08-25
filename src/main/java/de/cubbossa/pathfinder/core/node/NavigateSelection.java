package de.cubbossa.pathfinder.core.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor
@Getter
public class NavigateSelection extends HashSet<Navigable> {

	public NavigateSelection(Collection<Navigable> collection) {
		super(collection);
	}
}
