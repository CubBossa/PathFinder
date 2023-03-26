package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public interface DataStorage extends ApplicationLayer, NodeDataStorage<Waypoint> {

  default void connect() throws IOException {
    connect(() -> {
    });
  }

  /**
   * Sets up the database files or does nothing if the database is already setup.
   * If the database hasn't yet existed, the initial callback will be executed.
   *
   * @param initial A callback to be executed if the database was initially created
   */
  void connect(Runnable initial) throws IOException;

  void disconnect();

  CompletableFuture<NodeType<?>> getNodeType(UUID nodeId);

  CompletableFuture<Void> setNodeType(UUID nodeId, NamespacedKey nodeType);

  @Override
  default <N extends Node<N>> CompletableFuture<N> createNode(NodeType<N> type, Location location) {
    return type.createNodeInStorage(new NodeType.NodeCreationContext(location))
        .thenApply(n -> {
          setNodeType(n.getNodeId(), type.getKey()).join();
          return n;
        });
  }

  @Override
  default CompletableFuture<Void> teleportNode(UUID nodeId, Location location) {
    return updateNode(nodeId, node -> node.setLocation(location));
  }

  default CompletableFuture<Void> updateNode(UUID nodeId, Consumer<Node<?>> nodeConsumer) {
    return getNodeType(nodeId).thenAccept(nodeType -> {
      nodeType.updateNodeInStorage(nodeId, nodeConsumer::accept);
    });
  }

  default CompletableFuture<Void> updateNodes(NodeSelection nodes, Consumer<Node<?>> nodeConsumer) {
    nodes.stream()
        .parallel()
        .map(uid -> updateNode(uid, nodeConsumer))
        .forEach(CompletableFuture::join);
    return CompletableFuture.completedFuture(null);
  }

  default CompletableFuture<Void> deleteNodes(NodeSelection nodes) {
    Map<NodeType<?>, NodeSelection> mapping = new HashMap<>();
    for (UUID node : nodes) {
      mapping.computeIfAbsent(getNodeType(node).join(), t -> new NodeSelection()).add(node);
    }
    List<CompletableFuture<?>> futures = new ArrayList<>();
    mapping.forEach((nodeType, uuids) -> futures.add(nodeType.deleteNodesFromStorage(uuids)));
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }

  default CompletableFuture<Collection<Node<?>>> getNodes() {
    List<Node<?>> nodes = new ArrayList<>();
    for (NodeType<?> nodeType : NodeHandler.getInstance().getTypes().values()) {
      CompletableFuture<? extends Collection<?>> nodesFromStorage = nodeType.getNodesFromStorage();
      Collection<?> join = nodesFromStorage.join();
      nodes.add((Node<?>) join);
    }
    return CompletableFuture.completedFuture(nodes);
  }

  default CompletableFuture<Collection<Node<?>>> getNodes(NodeSelection nodes) {
    Map<NodeType<?>, NodeSelection> mapping = new HashMap<>();
    for (UUID node : nodes) {
      mapping.computeIfAbsent(getNodeType(node).join(), t -> new NodeSelection()).add(node);
    }
    Collection<Node<?>> result = new HashSet<>();
    List<CompletableFuture<?>> futures = new ArrayList<>();
    mapping.forEach((nodeType, uuids) -> futures.add(nodeType.getNodesFromStorage(uuids).thenAccept(result::addAll)));
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(unused -> result);
  }

  @Override
  default <M extends Modifier> CompletableFuture<Map<Node<?>, M>> getNodes(Class<M> modifier) {
    return getNodeGroups(modifier).thenApply(nodeGroups -> {
      Map<UUID, NodeGroup> temp = new HashMap<>();
      for (NodeGroup nodeGroup : nodeGroups) {
        for (UUID uuid : nodeGroup) {
          NodeGroup pres = temp.get(uuid);
          if (pres == null || nodeGroup.getWeight() > pres.getWeight()) {
            temp.put(uuid, pres);
          }
        }
      }
      Map<Node<?>, M> result = new HashMap<>();
      temp.forEach((uuid, group) -> {
        result.put(PathFinderAPI.get().getNode(uuid).join(), group.getModifier(modifier));
      });
      return result;
    });
  }

  @Override
  default CompletableFuture<Node<?>> getNode(UUID uuid) {
    return getNodeType(uuid).thenApply(nodeType -> nodeType.getNodeFromStorage(uuid).join());
  }

  @Override
  default CompletableFuture<Void> disconnectNodes(UUID start) {
    return disconnectNodes(new NodeSelection(start));
  }

  @Override
  default CompletableFuture<Void> disconnectNodes(UUID start, UUID end) {
    return disconnectNodes(new NodeSelection(start), new NodeSelection(end));
  }

  CompletableFuture<Collection<NamespacedKey>> getNodeGroups(UUID node);

  @Override
  default CompletableFuture<Void> assignNodesToGroup(NamespacedKey group, NodeSelection selection) {
    return updateNodeGroup(group, g -> g.addAll(selection));
  }

  @Override
  default CompletableFuture<Void> assignNodesToGroups(Collection<NamespacedKey> groups, NodeSelection selection) {
    groups.forEach(key -> {
      updateNodeGroup(key, g -> g.addAll(selection)).join();
    });
    return CompletableFuture.completedFuture(null);
  }

  @Override
  default CompletableFuture<Void> removeNodesFromGroup(NamespacedKey group, NodeSelection selection) {
    return updateNodeGroup(group, g -> selection.forEach(g::remove));
  }


  @Override
  default CompletableFuture<Void> removeNodesFromGroups(Collection<NamespacedKey> groups, NodeSelection selection) {
    groups.forEach(key -> {
      updateNodeGroup(key, g -> selection.forEach(g::remove)).join();
    });
    return CompletableFuture.completedFuture(null);
  }

  @Override
  default CompletableFuture<Void> assignNodeGroupModifier(NamespacedKey group, Modifier modifier) {
    return updateNodeGroup(group, g -> g.addModifier(modifier));
  }

  @Override
  default CompletableFuture<Void> unassignNodeGroupModifier(NamespacedKey group, Class<? extends Modifier> modifier) {
    return updateNodeGroup(group, g -> g.removeModifier(modifier));
  }
}
