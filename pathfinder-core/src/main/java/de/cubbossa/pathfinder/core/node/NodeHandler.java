package de.cubbossa.pathfinder.core.node;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.EdgesCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.EdgesDeletedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCurveLengthChangedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeLocationChangedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeTeleportEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroupHandler;
import de.cubbossa.pathfinder.core.nodegroup.modifier.NavigableModifier;
import de.cubbossa.pathfinder.core.roadmap.NoImplNodeGroupEditor;
import de.cubbossa.pathfinder.core.roadmap.NodeGroupEditor;
import de.cubbossa.pathfinder.core.roadmap.NodeGroupEditorFactory;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.data.DataStorageException;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.location.LocationWeightSolver;
import de.cubbossa.pathfinder.util.location.LocationWeightSolverPreset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class NodeHandler {

  public static NodeType<Waypoint> WAYPOINT_TYPE = new WaypointType();
  @Getter
  private static NodeHandler instance;

  private final DataStorage dataStorage;
  private final NodeGroupEditorFactory editModeFactory;
  @Getter
  private final HashedRegistry<NodeGroupEditor> editors;

  @Getter
  private final HashedRegistry<NodeType<?>> types;

  public NodeHandler(DataStorage dataStorage) {
    instance = this;
    this.dataStorage = dataStorage;
    this.types = new HashedRegistry<>();

    editors = new HashedRegistry<>();
    NodeHandler.getInstance().registerNodeType(NodeHandler.WAYPOINT_TYPE);

    ServiceLoader<NodeGroupEditorFactory> loader = ServiceLoader.load(NodeGroupEditorFactory.class,
        PathPlugin.getInstance().getClass().getClassLoader());
    NodeGroupEditorFactory factory = loader.findFirst().orElse(null);
    editModeFactory = Objects.requireNonNullElseGet(factory,
        () -> g -> new NoImplNodeGroupEditor(g.getKey()));
  }

  public <T extends Node<T>> NodeType<T> getNodeType(NamespacedKey key) {
    return (NodeType<T>) types.get(key);
  }

  public void registerNodeType(NodeType<?> type) {
    types.put(type);
  }

  public void unregisterNodeType(NodeType<?> type) {
    types.remove(type.getKey());
  }

  public void unregisterNodeType(NamespacedKey key) {
    types.remove(key);
  }

  public Graph<Node<?>> toGraph(Player permissionQuery, @Nullable PlayerNode player) {
    Graph<Node<?>> graph = new Graph<>();
    nodes.values().stream()
        .filter(node -> !(node instanceof Groupable<?> groupable) || groupable.getGroups().stream()
            .allMatch( //TODO instead use event and drop nodes while processing
                g -> g.getPermission() == null || permissionQuery.hasPermission(g.getPermission())))
        .forEach(graph::addNode);
    edges.forEach(e -> {
      try {
        graph.connect(e.getStart(), e.getEnd(), e.getWeightedLength());
      } catch (IllegalArgumentException ignore) {
        // we know that the node might not be in the graph due to its permission node.
      }
    });

    if (player != null) {
      graph.addNode(player);
      LocationWeightSolver<Node<?>> solver =
          LocationWeightSolverPreset.fromConfig(PathPlugin.getInstance()
              .getConfiguration().navigation.nearestLocationSolver);
      Map<Node<?>, Double> weighted = solver.solve(player, graph);

      weighted.forEach((node, weight) -> graph.connect(player, node, weight));
    }

    return graph;
  }

  public NavigateSelection getNavigables() {
    return new NavigateSelection(NodeGroupHandler.getInstance().getNodeGroups().stream()
        .filter(group -> group.hasModifier(NavigableModifier.class))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet()));
  }

  /**
   * Creates a new node of the given type and adds it to this roadmap.
   *
   * @param location   The location where the node should be created.
   * @param persistent If this node is persistent. If the roadmap is not persistent itself, the node cannot be persistent.
   * @return The created node.
   */
  public Waypoint createWaypoint(Location location, boolean persistent) {
    return createNode(WAYPOINT_TYPE, location, persistent);
  }

  /**
   * Creates a new node of the given type and adds it to this roadmap.
   * If you want to stick with default nodes you may want to prefer {@link #createWaypoint(Location, boolean)}, which
   * is an alias for this method with the node type already defined.
   *
   * @param type       The type of the node. You can use {@link NodeHandler#WAYPOINT_TYPE} for a default node or register
   *                   your own {@link NodeType} at the {@link NodeHandler}
   * @param location   The location where the node should be created.
   * @param persistent If this node is persistent. If the roadmap is not persistent itself, the node cannot be persistent.
   * @param groups     An optional set of groups to assign to the new node.
   * @param <T>        The node implementation class. {@link Waypoint} by default.
   * @return The created node.
   */
  public <T extends Node<T>> T createNode(NodeType<T> type, Location location, boolean persistent,
                                          NodeGroup... groups) {
    T node = type.createNode(new NodeType.NodeCreationContext(
        location,
        persistent
    ));

    addNode(node);
    if (node instanceof Groupable<?> groupable) {
      Collection<Groupable<?>> col = Collections.singleton(groupable);
      for (NodeGroup group : groups) {
        NodeGroupHandler.getInstance().addNodes(group, col);
      }
    }
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
      Bukkit.getPluginManager().callEvent(new NodeCreatedEvent<>(node));
    });
    return node;
  }

  /**
   * Adds the node instance to this roadmap. This should only be used if you already have a node instance, but it is not
   * contained in this roadmap, maybe because you're transferring it from one roadmap to another.
   * When using {@link #createNode(NodeType, Location, boolean, NodeGroup...)}, the node is already added to the roadmap.
   * <br>
   * DO NOT use this method to add nodes that have no type. Nodes should always be created by the roadmap and the given
   * type, not by creating an own instance of your node class and adding it via this method!
   *
   * @param node The node instance to add to this roadmap.
   */
  public <N extends Node<N>> void addNode(N node) {
    node.getType().updateNode(node);
  }

  public void removeNodes(NodeSelection selection) {
    removeNodes(selection.toArray(Node[]::new));
  }

  public void removeNode(int id) {
    Node<?> node = getNode(id);
    if (node != null) {
      removeNodes(node);
    }
  }

  public void removeNodes(Node<?>... nodes) {
    Collection<Edge> deleteEdges = new HashSet<>();
    Collection<Node<?>> deleteNodes = Lists.newArrayList(nodes);

    for (Node<?> node : nodes) {
      for (Edge edge : getEdgesAt(node)) {
        edge.getEnd().getEdges().remove(edge);
        edges.remove(edge);

        deleteEdges.add(edge);
      }
      this.nodes.remove(node.getNodeId());
    }
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
      Bukkit.getPluginManager().callEvent(new EdgesDeletedEvent(deleteEdges));
      Bukkit.getPluginManager().callEvent(new NodesDeletedEvent(deleteNodes));
    });
  }

  /**
   * Connects two nodes in both directions.
   * <p>
   * Alias for {@link #connectNodes(Node, Node, boolean, float, float)}. Uses an undirected edge and an edge modifier
   * of 1 for both directions as default. Just a normal edge then.
   *
   * @param start One of the nodes to connect
   * @param end   The other of the nodes to connect
   * @return The <b>first</b> edge that was created, from start to end. To get the edge instance of the second edge,
   * use {@link #getEdge(Node, Node)} with reversed parameters.
   */
  public Edge connectNodes(Node<?> start, Node<?> end) {
    return connectNodes(start, end, false, 1, 1);
  }

  /**
   * Connects two nodes with an edge. Edges are stored directed, therefore it must be stated if the new node should
   * only be from start to end or also from end to start.
   * <p>
   * This method calls the corresponding {@link EdgesCreatedEvent}. This will always be called sync, so that the
   * method can be called async.
   *
   * @param start    The node to start the edge from.
   * @param end      The node to end the edge at.
   * @param directed If another edge should be created from end to start. Prefer this method against calling it twice,
   *                 as the edit mode particle setup has to be recalculated for each edge change.
   * @return the created edge from start to end.
   */
  public Edge connectNodes(Node<?> start, Node<?> end, boolean directed) {
    if (start.equals(end)) {
      throw new IllegalArgumentException("Cannot connect node with itself.");
    }
    Edge edge;
    try {
      edge = new Edge(start, end, 1);
    } catch (DataStorageException e) {
      throw new IllegalArgumentException("Error while connecting edges: " + start + " and " + end,
          e);
    }

    start.getEdges().add(edge);
    edges.add(edge);

    Edge other = edge;
    if (!directed) {
      Edge existing = getEdge(end, start);
      if (existing == null) {
        try {
          other = new Edge(start, end, 1);
        } catch (DataStorageException e) {
          throw new IllegalArgumentException(
              "Error while connecting edges: " + start + " and " + end, e);
        }
        end.getEdges().add(other);
        edges.add(other);
      }
    }

    Edge finalOther = other;
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(),
        () -> Bukkit.getPluginManager().callEvent(new EdgesCreatedEvent(edge, finalOther)));

    return edge;
  }

  /**
   * Connects two nodes with an edge. Edges are stored directed, therefore it must be stated if the new node should
   * only be from start to end or also from end to start.
   * <p>
   * This method calls the corresponding {@link EdgesCreatedEvent}. This will always be called sync, so that the
   * method can be called async.
   *
   * @param start      The node to start the edge from.
   * @param end        The node to end the edge at.
   * @param directed   If another edge should be created from end to start. Prefer this method against calling it twice,
   *                   as the edit mode particle setup has to be recalculated for each edge change.
   * @param weight     The weight modifier for this edge. This will be taken into account when calculating the shortest path.
   *                   The actual length of the edge will be multiplied with the modifier, so when players have to crouch along some
   *                   edges of your roadmap, you might want to change the modifier to 2.
   * @param weightBack Same as weight, but for the directed edge in the opposite direction.
   * @return the created edge from start to end.
   */
  public Edge connectNodes(Node<?> start, Node<?> end, boolean directed, float weight,
                           float weightBack) {
    if (start.equals(end)) {
      throw new IllegalArgumentException("Cannot connect node with itself.");
    }
    Edge edge = new Edge(start, end, weight);

    start.getEdges().add(edge);
    edges.add(edge);

    Edge other = edge;
    if (!directed) {
      Edge existing = getEdge(end, start);
      if (existing == null) {
        other = new Edge(end, start, weightBack);
        end.getEdges().add(other);
        edges.add(other);
      }
    }

    Edge finalOther = other;
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(),
        () -> Bukkit.getPluginManager().callEvent(new EdgesCreatedEvent(edge, finalOther)));

    return edge;
  }

  public void disconnectNodes(Node<?> start, Node<?> end) {
    disconnectNodes(getEdge(start, end));
  }

  public void disconnectNode(Node<?> node) {
    for (Edge edge : new ArrayList<>(node.getEdges())) {
      disconnectNodes(edge);
    }
  }

  public void disconnectNodes(Edge edge) {
    if (edge == null) {
      return;
    }
    edge.getStart().getEdges().remove(edge);
    edge.getEnd().getEdges().remove(edge);
    edges.remove(edge);

    Bukkit.getScheduler().runTask(PathPlugin.getInstance(),
        () -> Bukkit.getPluginManager().callEvent(new EdgesDeletedEvent(edge)));
  }

  public Collection<Edge> getEdgesAt(Node<?> node) {
    return edges.stream().filter(edge -> edge.getStart().equals(node) || edge.getEnd().equals(node))
        .collect(Collectors.toSet());
  }

  public Collection<Node<?>> getNodes() {
    return nodes.values();
  }

  public @Nullable
  Edge getEdge(int aId, int bId) {
    return edges.stream()
        .filter(edge -> edge.getStart().getNodeId() == aId && edge.getEnd().getNodeId() == bId)
        .findAny()
        .orElse(null);
  }

  /**
   * This method changes the position of the node and calls the corresponding event.
   * If the event is not cancelled, the change will be updated to the database.
   * Don't call this method asynchronous, events can only be called in the main thread.
   * <p>
   * TO only modify the position without event or database update, simply call {@link Node#setLocation(Location)}
   *
   * @param nodes    The nodes to change the position for.
   * @param location The position to set. No world attribute is required, the roadmap attribute is used. Use {@link Location#toVector()}
   *                 to set a location.
   * @return true if the position was successfully set, false if the event was cancelled
   */
  public boolean setNodeLocation(NodeSelection nodes, Location location) {

    NodeTeleportEvent event = new NodeTeleportEvent(nodes, location);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return false;
    }
    for (Node node : nodes) {
      node.setLocation(event.getNewPositionModified());
    }
    Bukkit.getPluginManager()
        .callEvent(new NodeLocationChangedEvent(nodes, event.getNewPositionModified()));
    return true;
  }

  public void setNodeCurveLength(NodeSelection nodes, Double length) {
    nodes.forEach(node -> node.setCurveLength(length));
    Bukkit.getPluginManager().callEvent(new NodeCurveLengthChangedEvent(nodes, length));
  }

  // Editing

  public NodeGroupEditor getNodeGroupEditor(NamespacedKey key) {
    NodeGroupEditor editor = editors.get(key);
    if (editor == null) {
      NodeGroup group = roadMaps.get(key);
      if (roadMap == null) {
        throw new IllegalArgumentException(
            "No roadmap exists with key '" + key + "'. Cannot create editor.");
      }
      editor = editModeFactory.apply(roadMap);
      editors.put(editor);
    }
    return editor;
  }

  public void cancelAllEditModes() {
    editors.values().forEach(NodeGroupEditor::cancelEditModes);
  }

  public boolean isPlayerEditingRoadMap(Player player) {
    return editors.values().stream()
        .anyMatch(roadMapEditor -> roadMapEditor.isEditing(player));
  }

  public @Nullable NamespacedKey getGroupEditedBy(Player player) {
    return editors.values().stream()
        .filter(re -> re.isEditing(player))
        .map(NodeGroupEditor::getKey)
        .findFirst()
        .orElse(null);
  }
}
