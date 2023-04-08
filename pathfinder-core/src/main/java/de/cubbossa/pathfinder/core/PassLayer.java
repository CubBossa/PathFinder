package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.storage.ApplicationLayer;
import de.cubbossa.pathfinder.storage.DiscoverInfo;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

@RequiredArgsConstructor
public class PassLayer implements ApplicationLayer {

  private final ApplicationLayer layer;
  
  @Override
  public <N extends Node<N>> CompletableFuture<N> createNode(NodeType<N> type, Location location) {
    return layer.createNode(type, location);
  }

  @Override
  public CompletableFuture<Void> teleportNode(UUID nodeId, Location location) {
    return layer.teleportNode(nodeId, location);
  }

  @Override
  public CompletableFuture<Void> updateNode(UUID nodeId, Consumer<Node<?>> nodeConsumer) {
    return layer.updateNode(nodeId, nodeConsumer);
  }

  @Override
  public CompletableFuture<Void> updateNodes(NodeSelection nodes, Consumer<Node<?>> nodeConsumer) {
    return layer.updateNodes(nodes, nodeConsumer);
  }

  @Override
  public CompletableFuture<Void> deleteNodes(NodeSelection nodes) {
    return layer.deleteNodes(nodes);
  }

  @Override
  public CompletableFuture<Node<?>> getNode(UUID uuid) {
    return layer.getNode(uuid);
  }

  @Override
  public CompletableFuture<Collection<Node<?>>> getNodes() {
    return layer.getNodes();
  }

  @Override
  public CompletableFuture<Collection<Node<?>>> getNodes(NodeSelection selection) {
    return layer.getNodes(selection);
  }

  @Override
  public <M extends Modifier> CompletableFuture<Map<Node<?>, M>> getNodes(Class<M> modifier) {
    return layer.getNodes(modifier);
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnections(UUID start) {
    return layer.getConnections(start);
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnectionsTo(UUID end) {
    return layer.getConnectionsTo(end);
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnectionsTo(NodeSelection end) {
    return layer.getConnectionsTo(end);
  }

  @Override
  public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
    return layer.connectNodes(start, end, weight);
  }

  @Override
  public CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end) {
    return layer.connectNodes(start, end);
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(UUID start) {
    return layer.disconnectNodes(start);
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(UUID start, UUID end) {
    return layer.disconnectNodes(start, end);
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start) {
    return layer.disconnectNodes(start);
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
    return layer.disconnectNodes(start, end);
  }

  @Override
  public CompletableFuture<Collection<UUID>> getNodeGroupNodes(NamespacedKey group) {
    return layer.getNodeGroupNodes(group);
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroup(NamespacedKey group, NodeSelection selection) {
    return layer.assignNodesToGroup(group, selection);
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroups(Collection<NamespacedKey> groups, NodeSelection selection) {
    return layer.assignNodesToGroups(groups, selection);
  }

  @Override
  public CompletableFuture<Void> removeNodesFromGroup(NamespacedKey group, NodeSelection selection) {
    return layer.removeNodesFromGroup(group, selection);
  }

  @Override
  public CompletableFuture<Void> removeNodesFromGroups(Collection<NamespacedKey> groups, NodeSelection selection) {
    return layer.removeNodesFromGroups(groups, selection);
  }

  @Override
  public CompletableFuture<Void> clearNodeGroups(NodeSelection selection) {
    return layer.clearNodeGroups(selection);
  }

  @Override
  public CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet() {
    return layer.getNodeGroupKeySet();
  }

  @Override
  public CompletableFuture<NodeGroup> getNodeGroup(NamespacedKey key) {
    return layer.getNodeGroup(key);
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> getNodeGroups() {
    return layer.getNodeGroups();
  }

  @Override
  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> getNodeGroups(Class<M> modifier) {
    return layer.getNodeGroups(modifier);
  }

  @Override
  public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
    return layer.getNodeGroups(pagination);
  }

  @Override
  public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
    return layer.createNodeGroup(key);
  }

  @Override
  public CompletableFuture<Void> updateNodeGroup(NamespacedKey group, Consumer<NodeGroup> modifier) {
    return layer.updateNodeGroup(group, modifier);
  }

  @Override
  public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
    return layer.deleteNodeGroup(key);
  }

  @Override
  public CompletableFuture<Void> assignNodeGroupModifier(NamespacedKey group, Modifier modifier) {
    return layer.assignNodeGroupModifier(group, modifier);
  }

  @Override
  public CompletableFuture<Void> unassignNodeGroupModifier(NamespacedKey group, Class<? extends Modifier> modifier) {
    return layer.unassignNodeGroupModifier(group, modifier);
  }

  @Override
  public DiscoverInfo createDiscoverInfo(UUID player, NodeGroup discoverable, LocalDateTime foundDate) {
    return layer.createDiscoverInfo(player, discoverable, foundDate);
  }

  @Override
  public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
    return layer.loadDiscoverInfo(playerId);
  }

  @Override
  public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
    layer.deleteDiscoverInfo(playerId, discoverKey);
  }

  @Override
  public <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(VisualizerType<T> type) {
    return layer.loadPathVisualizer(type);
  }

  @Override
  public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {
    layer.updatePathVisualizer(visualizer);
  }

  @Override
  public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
    layer.deletePathVisualizer(visualizer);
  }
}
