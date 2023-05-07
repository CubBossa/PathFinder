package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.*;
import de.cubbossa.pathapi.storage.CacheLayer;
import de.cubbossa.pathapi.storage.NodeDataStorage;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import de.cubbossa.pathfinder.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class CommonStorage implements StorageImplementation, WaypointDataStorage, InternalVisualizerDataStorage {

  final NodeTypeRegistry nodeTypeRegistry;
  final VisualizerTypeRegistry visualizerTypeRegistry;
  final ModifierRegistry modifierRegistry;
  @Getter
  @Setter
  CacheLayer cache;
  @Getter
  @Setter
  private @Nullable Logger logger;

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> resolve(VisualizerT visualizer) {
    return resolve(visualizer.getKey());
  }

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> resolve(NamespacedKey key) {
    return (VisualizerType<VisualizerT>) cache.getVisualizerTypeCache().getType(key, this::loadVisualizerType).orElseThrow(() -> {
      return new IllegalStateException("Tried to create visualizer of type '" + key + "' but could not find registered type with this key.");
    });
  }

  private Node insertGroups(Node node) {
    if (node instanceof Groupable groupable) {
      debug(" > Storage Implementation: 'insertGroups(" + node.getNodeId() + ")'");
      loadGroups(node.getNodeId()).forEach(groupable::addGroup);
    }
    return node;
  }

  private Node insertEdges(Node node) {
    debug(" > Storage Implementation: 'insertEdges(" + node.getNodeId() + ")'");
    node.getEdges().addAll(loadEdgesFrom(node.getNodeId()));
    return node;
  }

  @Override
  public <N extends Node> N createAndLoadNode(NodeType<N> type, Location location) {
    debug(
        " > Storage Implementation: 'createAndLoadNode(" + type.getKey() + ", " + location + ")'");
    N node = type.createAndLoadNode(new NodeDataStorage.Context(location));
    saveNodeType(node.getNodeId(), type);
    return node;
  }

  @Override
  public <N extends Node> Optional<N> loadNode(UUID id) {
    debug(" > Storage Implementation: 'loadNode(" + id + ")'");
    Optional<NodeType<N>> type = loadNodeType(id);
    if (type.isPresent()) {
      return (Optional<N>) type.get().loadNode(id).map(this::insertEdges).map(this::insertGroups);
    }
    throw new IllegalStateException("No type found for node with UUID '" + id + "'.");
  }

  @Override
  public Collection<Node> loadNodes() {
    debug(" > Storage Implementation: 'loadNodes()'");
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadAllNodes().stream())
        .map(this::insertEdges)
        .map(this::insertGroups)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<Node> loadNodes(Collection<UUID> ids) {
    debug(" > Storage Implementation: 'loadNodes(" + ids.stream()
        .map(UUID::toString).collect(Collectors.joining(", ")) + ")'");
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadNodes(ids).stream())
        .map(this::insertEdges)
        .map(this::insertGroups)
        .collect(Collectors.toList());
  }

  @Override
  public void saveNode(Node node) {
    debug(" > Storage Implementation: 'saveNode(" + node.getNodeId() + ")'");
    saveNodeTyped(node);
  }

  private <N extends Node> void saveNodeTyped(N node) {
    NodeType<N> type = cache.getNodeTypeCache().<N>getType(node.getNodeId(), this::loadNodeType);
    N before = type.loadNode(node.getNodeId()).orElseThrow();
    type.saveNode(node);

    if (node == before) {
      throw new IllegalStateException("Cannot have compared elements be the same object instance!");
    }

    if (before instanceof Groupable gBefore && node instanceof Groupable gAfter) {
      StorageImpl.ComparisonResult<NodeGroup> cmp =
          StorageImpl.ComparisonResult.compare(gBefore.getGroups(), gAfter.getGroups());
      cmp.toInsertIfPresent(nodeGroups -> assignToGroups(nodeGroups, List.of(node.getNodeId())));
      cmp.toDeleteIfPresent(
          nodeGroups -> unassignFromGroups(nodeGroups, List.of(node.getNodeId())));
    }
    StorageImpl.ComparisonResult<Edge> cmp =
        StorageImpl.ComparisonResult.compare(before.getEdges(), node.getEdges());
    cmp.toInsertIfPresent(edges -> {
      for (Edge edge : edges) {
        createAndLoadEdge(edge.getStart(), edge.getEnd(), edge.getWeight());
      }
    });
    cmp.toDeleteIfPresent(edges -> edges.forEach(this::deleteEdge));
  }

  protected void debug(String message) {
    if (logger != null) {
      logger.log(Level.INFO, message);
    }
  }

  @Override
  public void deleteNodes(Collection<Node> nodes) {
    Map<UUID, NodeType<?>> types = loadNodeTypes(nodes.stream().map(Node::getNodeId).toList());
    for (Node node : nodes) {
      if (node instanceof Groupable groupable) {
        unassignFromGroups(groupable.getGroups(), List.of(groupable.getNodeId()));
      }
      deleteNode(node, types.get(node.getNodeId()));
    }
    // TODO remove Type mapping, remove edge mapping
  }

  void deleteNode(Node node, NodeType type) {
    type.deleteNode(node);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> VisualizerT createAndLoadVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key) {
    String nameFormat = StringUtils.toDisplayNameFormat(key);
    VisualizerT visualizer = type.create(key, nameFormat);
    saveVisualizer(visualizer);
    return visualizer;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void deleteVisualizer(VisualizerT visualizer) {
    resolve(visualizer).getStorage().deleteVisualizer(visualizer);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void saveVisualizer(VisualizerT visualizer) {
    resolve(visualizer).getStorage().saveVisualizer(visualizer);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadVisualizers(VisualizerType<VisualizerT> type) {
    return type.getStorage().loadVisualizers();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> loadVisualizer(NamespacedKey key) {
    return (Optional<VisualizerT>) resolve(key).getStorage().loadVisualizer(key);
  }
}
