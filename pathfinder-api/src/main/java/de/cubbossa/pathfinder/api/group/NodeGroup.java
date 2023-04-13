package de.cubbossa.pathfinder.api.group;

import de.cubbossa.pathfinder.api.misc.Keyed;

import java.util.Set;
import java.util.UUID;

public interface NodeGroup extends Keyed, Modified, Set<UUID>, Comparable<NodeGroup> {

	float getWeight();
}
