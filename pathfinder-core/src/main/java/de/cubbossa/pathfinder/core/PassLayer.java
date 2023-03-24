package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import de.cubbossa.pathfinder.data.DiscoverInfo;
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
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public class PassLayer implements ApplicationLayer {

  @Override
  public <N extends Node<N>> CompletableFuture<N> createNode(NodeType<N> type, Location location) {
    return null;
  }

  @Override
  public CompletableFuture<Void> teleportNode(UUID nodeId, Location location) {
    return null;
  }

  @Override
  public CompletableFuture<Void> updateNode(UUID nodeId, Consumer<Node<?>> nodeConsumer) {
    return null;
  }

  @Override
  public CompletableFuture<Void> updateNodes(NodeSelection nodes, Consumer<Node<?>> nodeConsumer) {
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteNodes(NodeSelection nodes) {
    return null;
  }

  @Override
  public CompletableFuture<Node<?>> getNode(UUID uuid) {
    return null;
  }

  @Override
  public CompletableFuture<Collection<Node<?>>> getNodes() {
    return null;
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnections(UUID start) {
    return null;
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnectionsTo(UUID end) {
    return null;
  }

  @Override
  public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
    return null;
  }

  @Override
  public CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end) {
    return null;
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(UUID start) {
    return null;
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(UUID start, UUID end) {
    return null;
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start) {
    return null;
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
    return null;
  }

  @Override
  public CompletableFuture<Collection<UUID>> getNodeGroupNodes(NamespacedKey group) {
    return null;
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroup(NamespacedKey group, NodeSelection selection) {
    return null;
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroups(Collection<NamespacedKey> groups, NodeSelection selection) {
    return null;
  }

  @Override
  public CompletableFuture<Void> removeNodesFromGroup(NamespacedKey group, NodeSelection selection) {
    return null;
  }

  @Override
  public CompletableFuture<Void> removeNodesFromGroups(Collection<NamespacedKey> groups, NodeSelection selection) {
    return null;
  }

  @Override
  public CompletableFuture<Void> clearNodeGroups(NodeSelection selection) {
    return null;
  }

  @Override
  public CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet() {
    return null;
  }

  @Override
  public CompletableFuture<NodeGroup> getNodeGroup(NamespacedKey key) {
    return null;
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> getNodeGroups() {
    return null;
  }

  @Override
  public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
    return null;
  }

  @Override
  public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
    return null;
  }

  @Override
  public CompletableFuture<Void> updateNodeGroup(NamespacedKey group, Consumer<NodeGroup> modifier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
    return null;
  }

  @Override
  public CompletableFuture<Void> assignNodeGroupModifier(NamespacedKey group, Modifier modifier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> unassignNodeGroupModifier(NamespacedKey group, Class<? extends Modifier> modifier) {
    return null;
  }

  @Override
  public DiscoverInfo createDiscoverInfo(UUID player, NodeGroup discoverable, LocalDateTime foundDate) {
    return null;
  }

  @Override
  public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
    return null;
  }

  @Override
  public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {

  }

  @Override
  public <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(VisualizerType<T> type) {
    return null;
  }

  @Override
  public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {

  }

  @Override
  public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {

  }
}
