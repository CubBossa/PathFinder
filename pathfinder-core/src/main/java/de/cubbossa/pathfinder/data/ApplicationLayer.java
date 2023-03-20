package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

public interface ApplicationLayer {

  // Nodes

  <N extends Node<N>> CompletableFuture<N> createNode(NodeType<N> type, Location location);

  CompletableFuture<Void> teleportNode(UUID nodeId, Location location);

  CompletableFuture<Void> updateNode(UUID nodeId, Consumer<Node<?>> nodeConsumer);

  CompletableFuture<Void> updateNodes(NodeSelection nodes, Consumer<Node<?>> nodeConsumer);

  CompletableFuture<Void> deleteNodes(Collection<UUID> nodes);

  CompletableFuture<Void> deleteNodes(NodeSelection nodes);

  CompletableFuture<Collection<Node<?>>> getNodes();

  CompletableFuture<Collection<Edge>> getConnections(UUID start);

  default CompletableFuture<Edge> connectNodes(UUID start, UUID end) {
    return connectNodes(start, end, 1);
  }

  CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight);

  CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end);

  CompletableFuture<Void> disconnectNodes(UUID start);
  CompletableFuture<Void> disconnectNodes(UUID start, UUID end);

  CompletableFuture<Void> disconnectNodes(NodeSelection start);
  CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end);

  CompletableFuture<Collection<UUID>> getNodeGroupNodes(NamespacedKey group);

  CompletableFuture<Void> assignNodesToGroup(NamespacedKey group, NodeSelection selection);
  CompletableFuture<Void> assignNodesToGroups(Collection<NamespacedKey> groups, NodeSelection selection);

  CompletableFuture<Void> removeNodesFromGroup(NamespacedKey group, NodeSelection selection);
  CompletableFuture<Void> removeNodesFromGroups(Collection<NamespacedKey> groups, NodeSelection selection);

  CompletableFuture<Void> clearNodeGroups(NodeSelection selection);


  // NodeGroups

  CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet();

  CompletableFuture<NodeGroup> getNodeGroup(NamespacedKey key);

  CompletableFuture<Collection<NodeGroup>> getNodeGroups();

  CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination);

  CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key);

  CompletableFuture<Void> updateNodeGroup(NamespacedKey group, Consumer<NodeGroup> modifier);

  CompletableFuture<Void> deleteNodeGroup(NamespacedKey key);

  CompletableFuture<Void> assignNodeGroupModifier(NamespacedKey group, Modifier modifier);

  CompletableFuture<Void> unassignNodeGroupModifier(NamespacedKey group, Class<? extends Modifier> modifier);

  // Discovery

  DiscoverInfo createDiscoverInfo(UUID player, NodeGroup discoverable, LocalDateTime foundDate);

  Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId);

  void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey);


  <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(VisualizerType<T> type);

  <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer);

  void deletePathVisualizer(PathVisualizer<?, ?> visualizer);

  record Pagination (int offset, int limit) {
    public static Pagination page(int page, int elements) {
      return new Pagination(page * elements, elements);
    }
  }
}
