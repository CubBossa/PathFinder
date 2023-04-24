package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.Node;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public interface NodeCache extends StorageCache<Node<?>> {

	Optional<Node<?>> getNode(UUID uuid);

	Collection<Node<?>> getAllNodes(Supplier<Collection<Node<?>>> loader);

	Collection<Node<?>> getNodes(Collection<UUID> ids, Function<Collection<UUID>, Collection<? extends Node<?>>> loader);

	void write(NodeGroup group, Collection<UUID> deleted);

	void invalidate(UUID uuid);
}
