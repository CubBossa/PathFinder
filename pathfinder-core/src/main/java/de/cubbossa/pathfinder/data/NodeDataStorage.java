package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.concurrent.CompletableFuture;

public interface NodeDataStorage<N extends Node<N>> {

  CompletableFuture<N> createNode(NodeType.NodeCreationContext context);

  CompletableFuture<Void> updateNode(N node);

  CompletableFuture<Void> deleteNodes(NodeSelection nodes);
}
