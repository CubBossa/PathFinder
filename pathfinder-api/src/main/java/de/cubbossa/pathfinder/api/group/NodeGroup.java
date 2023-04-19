package de.cubbossa.pathfinder.api.group;

import de.cubbossa.pathfinder.api.misc.Keyed;

import de.cubbossa.pathfinder.api.node.Node;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface NodeGroup extends Keyed, Modified, Set<UUID>, Comparable<NodeGroup> {

	float getWeight();

	CompletableFuture<Collection<Node<?>>> resolve();
}
