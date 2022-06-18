package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.Named;
import org.bukkit.NamespacedKey;

import java.util.Collection;

public interface Findable extends Named {

	NamespacedKey getIdentifier();

	NamespacedKey getRoadMapKey();

	Collection<Findable> getGrouped();
}
