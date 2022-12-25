package de.cubbossa.pathfinder.core.roadmap;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PersistencyHolder;
import de.cubbossa.pathfinder.core.events.node.EdgesCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.EdgesDeletedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.graph.Graph;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.NavigateSelection;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeHandler;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.data.DataStorageException;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

@Getter
@Setter
public class RoadMap implements Keyed, Named, PersistencyHolder {

  private final NamespacedKey key;
  private final boolean persistent;
  private final Map<Integer, Node> nodes;
  private final Collection<Edge> edges;
  private String nameFormat;
  private Component displayName;
  private double defaultCurveLength;
  private PathVisualizer<?, ?> visualizer;

  public RoadMap(NamespacedKey key, String name, PathVisualizer<?, ?> visualizer,
                 double defaultCurveLength) {
    this(key, name, visualizer, defaultCurveLength, true);
  }

  public RoadMap(NamespacedKey key, String name, PathVisualizer<?, ?> visualizer,
                 double defaultCurveLength, boolean persistent) {

    this.key = key;
    this.persistent = persistent;
    this.setNameFormat(name);
    this.defaultCurveLength = defaultCurveLength;

    this.nodes = new TreeMap<>();
    this.edges = new HashSet<>();

    setVisualizer(visualizer);
  }

  public void setNameFormat(String nameFormat) {
    this.nameFormat = nameFormat;
    this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
  }

  public void loadNodesAndEdges(
      Map<Integer, ? extends Collection<NamespacedKey>> nodesGroupMapping) {
    nodes.clear();
    var map1 = PathPlugin.getInstance().getDatabase().loadNodes(this);
    nodes.putAll(map1);
    for (var entry : nodesGroupMapping.entrySet()) {
      if (!nodes.containsKey(entry.getKey())) {
        continue;
      }
      Node node = nodes.get(entry.getKey());
      if (node == null) {
        PathPlugin.getInstance().getLogger()
            .log(Level.SEVERE, "Tried to map a node that doesn't exist: " + entry.getKey());
        continue;
      }
      if (!(node instanceof Groupable groupable)) {
        PathPlugin.getInstance().getLogger()
            .log(Level.SEVERE, "Tried to map a node that is not groupable: " + entry.getKey());
        continue;
      }
      for (NamespacedKey key : entry.getValue()) {
        NodeGroup group = NodeGroupHandler.getInstance().getNodeGroup(key);
        if (group == null) {
          PathPlugin.getInstance().getLogger().log(Level.SEVERE,
              "Tried to assign a node to a nodegroup that doesn't exist: " + key);
          continue;
        }
        group.add(groupable);
      }
    }

    edges.clear();
    edges.addAll(PathPlugin.getInstance().getDatabase().loadEdges(this, map1));
    for (Edge edge : edges) {
      edge.getStart().getEdges().add(edge);
    }
  }

  public Graph<Node> toGraph(Player permissionQuery, @Nullable PlayerNode player) {
    Graph<Node> graph = new Graph<>();
    nodes.values().stream()
        .filter(node -> !(node instanceof Groupable groupable) || groupable.getGroups().stream()
            .allMatch(
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
      Location playerLocation = player.getLocation();
      graph.addNode(player);
      List<Triple<Node, Double, Integer>> triples = nodes.values().stream()
          .filter(node -> Objects.equals(node.getLocation().getWorld(), playerLocation.getWorld()))
          .map(node -> new AbstractMap.SimpleEntry<>(node,
              node.getLocation().distance(playerLocation)))
          .sequential()
          .sorted(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
          .limit(10)
          .map(e -> {
            Node n = e.getKey();
            Vector dir = n.getLocation().toVector().clone().add(new Vector(0, .5f, 0))
                .subtract(playerLocation.toVector());
            double length = dir.length();
            dir.normalize();
            Location loc = player.getLocation().setDirection(dir);
            int count = 1;

            BlockIterator iterator = new BlockIterator(loc, 0, (int) length);
            int cancel = 0; // sometimes the while loop does not cancel without extra counter
            while (iterator.hasNext() && cancel++ < 100) {
              Block block = iterator.next();
              if (block.getType().isBlock() && block.getType().isSolid()) {
                count++;
              }
            }
            return Triple.of(n, length, count);
          }).toList();

      boolean anyNullCount = triples.stream().anyMatch(e -> e.getRight() == 1);

      triples.stream()
          .filter(e -> !anyNullCount || e.getRight() == 1)
          .forEach(e -> {

            Edge edge = new Edge(player, e.getLeft(),
                e.getRight() * 10_000); // prefer paths without interfering blocks
            graph.connect(player, e.getLeft(), e.getMiddle() * edge.getWeightModifier());
          });
    }

    return graph;
  }

  public NavigateSelection getNavigables() {
    return new NavigateSelection(NodeGroupHandler.getInstance().getNodeGroups().stream()
        .filter(group -> nodes.values().stream().anyMatch(group::contains))
        .collect(Collectors.toSet()));
  }

  public NavigateSelection getNavigables(String... keywords) {
    return getNavigables(Lists.newArrayList(keywords));
  }

  public NavigateSelection getNavigables(Collection<String> keywords) {
    return getNavigables().stream()
        .filter(node -> {
          for (String keyword : keywords) {
            if (node.getSearchTerms().contains(keyword)) {
              return true;
            }
          }
          return false;
        })
        .collect(Collectors.toCollection(NavigateSelection::new));
  }

  /**
   * Creates a new node of the given type and adds it to this roadmap.
   *
   * @param location   The location where the node should be created.
   * @param persistent If this node is persistent. If the roadmap is not persistent itself, the node cannot be persistent.
   * @return The created node.
   */
  public Waypoint createWaypoint(Location location, boolean persistent) {
    return createNode(RoadMapHandler.WAYPOINT_TYPE, location, persistent);
  }

  /**
   * Creates a new node of the given type and adds it to this roadmap.
   * If you want to stick with default nodes you may want to prefer {@link #createWaypoint(Location, boolean)}, which
   * is an alias for this method with the node type already defined.
   *
   * @param type       The type of the node. You can use {@link RoadMapHandler#WAYPOINT_TYPE} for a default node or register
   *                   your own {@link NodeType} at the {@link NodeTypeHandler}
   * @param location   The location where the node should be created.
   * @param persistent If this node is persistent. If the roadmap is not persistent itself, the node cannot be persistent.
   * @param groups     An optional set of groups to assign to the new node.
   * @param <T>        The node implementation class. {@link Waypoint} by default.
   * @return The created node.
   */
  public <T extends Node> T createNode(NodeType<T> type, Location location, boolean persistent,
                                       NodeGroup... groups) {

    T node = type.getFactory().apply(new NodeType.NodeCreationContext(
        this,
        RoadMapHandler.getInstance().requestNodeId(),
        location,
        persistent && this.persistent
    ));

    addNode(node);
    if (node instanceof Groupable groupable) {
      Collection<Groupable> col = Collections.singleton(groupable);
      for (NodeGroup group : groups) {
        NodeGroupHandler.getInstance().addNodes(group, col);
      }
    }
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
      Bukkit.getPluginManager().callEvent(new NodeCreatedEvent(node));
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
  public void addNode(Node node) {
    nodes.put(node.getNodeId(), node);
  }

  public void removeNodes(NodeSelection selection) {
    removeNodes(selection.toArray(Node[]::new));
  }

  public void removeNode(int id) {
    Node node = getNode(id);
    if (node != null) {
      removeNodes(node);
    }
  }

  public void removeNodes(Node... nodes) {
    Collection<Edge> deleteEdges = new HashSet<>();
    Collection<Node> deleteNodes = Lists.newArrayList(nodes);

    for (Node node : nodes) {
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

  public @Nullable
  Node getNode(int nodeId) {
    for (Node node : nodes.values()) {
      if (node.getNodeId() == nodeId) {
        return node;
      }
    }
    return null;
  }

  public Collection<Node> getNodesByGroup(NodeGroup group) {
    return group.getGroup().stream().filter(node -> node.getRoadMapKey().equals(key))
        .collect(Collectors.toList());
  }

  public Edge getEdge(Node start, Node end) {
    return edges.stream().filter(edge -> edge.getStart().equals(start) && edge.getEnd().equals(end))
        .findFirst().orElse(null);
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
  public Edge connectNodes(Node start, Node end) {
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
  public Edge connectNodes(Node start, Node end, boolean directed) {
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
  public Edge connectNodes(Node start, Node end, boolean directed, float weight, float weightBack) {
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

  public void disconnectNodes(Node start, Node end) {
    disconnectNodes(getEdge(start, end));
  }

  public void disconnectNode(Node node) {
    node.getEdges().forEach(this::disconnectNodes);
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

  public Collection<Edge> getEdgesFrom(Node node) {
    return node.getEdges();
  }

  public Collection<Edge> getEdgesTo(Node node) {
    Collection<Edge> ret = new ArrayList<>();
    for (Edge edge : edges) {
      if (edge.getEnd().equals(node)) {
        ret.add(edge);
      }
    }
    return ret;
  }

  public Collection<Edge> getEdgesAt(Node node) {
    return edges.stream().filter(edge -> edge.getStart().equals(node) || edge.getEnd().equals(node))
        .collect(Collectors.toSet());
  }

  public Collection<Node> getNodes() {
    return nodes.values();
  }

  public @Nullable
  Edge getEdge(int aId, int bId) {
    return edges.stream()
        .filter(edge -> edge.getStart().getNodeId() == aId && edge.getEnd().getNodeId() == bId)
        .findAny()
        .orElse(null);
  }

  public RoadMapBatchEditor getBatchEditor() {
    AtomicBoolean closed = new AtomicBoolean(false);

    return new RoadMapBatchEditor() {
      @Override
      public <T extends Node> void createNode(NodeType<T> type, Vector vector, String permission,
                                              NodeGroup... groups) {
        if (closed.get()) {
          throw new IllegalStateException("Batch Editor already closed.");
        }
      }

      @Override
      public void commit() {
        if (closed.get()) {
          throw new IllegalStateException("Batch Editor already closed.");
        }


        closed.set(true);
      }
    };
  }

  public interface RoadMapBatchEditor {

    <T extends Node> void createNode(NodeType<T> type, Vector vector, String permission,
                                     NodeGroup... groups);

    void commit();
  }
}
