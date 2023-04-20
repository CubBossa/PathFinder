package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.Keyed;

import de.cubbossa.pathapi.node.Node;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface NodeGroup extends Keyed, Modified, Set<UUID>, Comparable<NodeGroup> {

	float getWeight();

	CompletableFuture<Collection<Node<?>>> resolve();
}
