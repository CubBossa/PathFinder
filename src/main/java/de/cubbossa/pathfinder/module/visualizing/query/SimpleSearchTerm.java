package de.cubbossa.pathfinder.module.visualizing.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
public class SimpleSearchTerm implements SearchTerm {

	@Getter
	private final String identifier;

	@Override
	public boolean matches(Collection<SearchQueryAttribute> attributes) {
		return true;
	}
}
