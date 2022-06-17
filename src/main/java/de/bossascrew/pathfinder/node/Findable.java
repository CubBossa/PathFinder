package de.bossascrew.pathfinder.node;

import org.bukkit.NamespacedKey;

import java.util.Collection;

public interface Findable {

	NamespacedKey getIdentifier();

	Collection<Findable> getGrouped();
}
