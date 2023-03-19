package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NodeDataStorage<N extends Node<N>> {

  CompletableFuture<N> createNodeInStorage(NodeType.NodeCreationContext context);

  CompletableFuture<N> getNodeFromStorage(UUID id);

  CompletableFuture<Collection<N>> getNodesFromStorage();

  CompletableFuture<Collection<N>> getNodesFromStorage(NodeSelection ids);

  CompletableFuture<Void> updateNodeInStorage(UUID nodeId, Consumer<N> nodeConsumer);

  CompletableFuture<Void> updateNodesInStorage(NodeSelection nodeIds, Consumer<N> nodeConsumer);

  CompletableFuture<Void> deleteNodesFromStorage(NodeSelection nodes);
}
