package de.bossascrew.pathfinder.node;

import net.kyori.adventure.text.Component;

import java.util.Collection;

public interface Navigable {

	Component getDisplayName();

	Collection<String> getSearchTerms();
}
