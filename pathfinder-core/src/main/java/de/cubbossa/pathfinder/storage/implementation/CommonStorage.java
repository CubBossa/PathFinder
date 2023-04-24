package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.CacheLayer;
import de.cubbossa.pathapi.storage.NodeDataStorage;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public abstract class CommonStorage implements StorageImplementation, WaypointDataStorage {

  final NodeTypeRegistry nodeTypeRegistry;
  final ModifierRegistry modifierRegistry;
  @Getter
  @Setter
  CacheLayer cache;
  @Getter
  @Setter
  private @Nullable Logger logger;

  private Node<?> insertGroups(Node<?> node) {
    if (node instanceof Groupable<?> groupable) {
      debug(" > Storage Implementation: 'insertGroups(" + node.getNodeId() + ")'");
      loadGroups(node.getNodeId()).forEach(groupable::addGroup);
    }
    return node;
  }

  private Node<?> insertEdges(Node<?> node) {
    debug(" > Storage Implementation: 'insertEdges(" + node.getNodeId() + ")'");
    node.getEdges().addAll(loadEdgesFrom(node.getNodeId()));
    return node;
  }

  @Override
  public <N extends Node<N>> N createAndLoadNode(NodeType<N> type, Location location) {
    debug(
        " > Storage Implementation: 'createAndLoadNode(" + type.getKey() + ", " + location + ")'");
    N node = type.createAndLoadNode(new NodeDataStorage.Context(location));
    saveNodeType(node.getNodeId(), type);
    return node;
  }

  @Override
  public <N extends Node<N>> Optional<N> loadNode(UUID id) {
    debug(" > Storage Implementation: 'loadNode(" + id + ")'");
    Optional<NodeType<N>> type = loadNodeType(id);
    if (type.isPresent()) {
      return (Optional<N>) type.get().loadNode(id).map(this::insertEdges).map(this::insertGroups);
    }
    throw new IllegalStateException("No type found for node with UUID '" + id + "'.");
  }

  @Override
  public Collection<Node<?>> loadNodes() {
    debug(" > Storage Implementation: 'loadNodes()'");
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadAllNodes().stream())
        .map(this::insertEdges)
        .map(this::insertGroups)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<Node<?>> loadNodes(Collection<UUID> ids) {
    debug(" > Storage Implementation: 'loadNodes(" + ids.stream()
        .map(UUID::toString).collect(Collectors.joining(", ")) + ")'");
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadNodes(ids).stream())
        .map(this::insertEdges)
        .map(this::insertGroups)
        .collect(Collectors.toList());
  }

  @Override
  public void saveNode(Node<?> node) {
    debug(" > Storage Implementation: 'saveNode(" + node.getNodeId() + ")'");
    saveNodeTyped(node);
  }

  private <N extends Node<N>> void saveNodeTyped(Node<?> node) {
    NodeType<N> type = (NodeType<N>) node.getType();
    N before = type.loadNode(node.getNodeId()).orElseThrow();
    type.saveNode((N) node);

    if (before instanceof Groupable<?> gBefore && node instanceof Groupable<?> gAfter) {
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
  public void deleteNodes(Collection<Node<?>> nodes) {
    Map<UUID, NodeType<?>> types = loadNodeTypes(nodes.stream().map(Node::getNodeId).toList());
    for (Node<?> node : nodes) {
      if (node instanceof Groupable<?> groupable) {
        unassignFromGroups(groupable.getGroups(), List.of(groupable.getNodeId()));
      }
      deleteNode(node, types.get(node.getNodeId()));
    }
    // TODO remove Type mapping, remove edge mapping
  }

  void deleteNode(Node node, NodeType type) {
    type.deleteNode(node);
  }
}
