package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.event.NodeCreateEvent;
import de.cubbossa.pathfinder.event.NodeDeleteEvent;
import de.cubbossa.pathfinder.event.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.event.NodeGroupSaveEvent;
import de.cubbossa.pathfinder.event.NodeSaveEvent;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.GroupedNodeImpl;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.GroupedVisualizerPathImpl;
import de.cubbossa.pathfinder.visualizer.PathView;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.SingleVisualizerPathImpl;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;

public class NavigatorImpl implements Navigator {

  private final PathFinder pathFinder;
  private final Function<Collection<Node>, Collection<Node>> constraint;

  // Caches
  private CompletableFuture<MutableValueGraph<Node, Double>> generatingFuture = null;
  private MutableValueGraph<Node, Double> cachedGraph = null;

  public NavigatorImpl() {
    this(Function.identity());
  }

  public NavigatorImpl(Function<Collection<Node>, Collection<Node>> constraint) {
    this.constraint = constraint;

    pathFinder = PathFinder.get();
    EventDispatcher<?> eventDispatcher = pathFinder.getEventDispatcher();
    pathFinder.getDisposer().register(pathFinder, this);

    eventDispatcher.listen(NodeCreateEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeGroupDeleteEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeSaveEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeGroupSaveEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeDeleteEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeGroupDeleteEvent.class, e -> cachedGraph = null);
  }

  @Override
  public List<Node> createPath(Route route) throws NoPathFoundException {
    try {
      ValueGraph<Node, Double> graph = fetchGraph().get();
      List<Node> path = route.calculatePath(graph).getPath();
      return removeIdenticalNeighbours(path);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof NoPathFoundException np) {
        throw np;
      }
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Node> removeIdenticalNeighbours(List<Node> path) {
    List<Node> result = new ArrayList<>();
    GroupedNode last = null;
    for (Node node : path) {
      if (!(node instanceof GroupedNode groupedNode)) {
        last = null;
        result.add(node);
        continue;
      }
      if (last != null && Objects.equals(last.node().getLocation(), groupedNode.node().getLocation())) {
        GroupedNode n = (GroupedNode) last.clone();
        n.groups().addAll(groupedNode.groups());
        result.remove(result.size() - 1);
        result.add(n);
      } else {
        result.add(node);
      }
      last = groupedNode;
    }
    return path;
  }

  @Override
  public <PlayerT> VisualizerPath<PlayerT> createRenderer(
      PathPlayer<PlayerT> viewer, Route route
  ) throws NoPathFoundException {

    VisualizerPath<PlayerT> path = new GroupedVisualizerPathImpl<>(viewer, () -> {
      try {
        return createPath(route);
      } catch (NoPathFoundException ignored) {
        return new ArrayList<>();
      }
    });
    path.addViewer(viewer);

    // load config value
    path.startUpdater(0);
    return path;
  }

  @Override
  public <PlayerT, ViewT extends PathView<PlayerT>> VisualizerPath<PlayerT> createRenderer(
      PathPlayer<PlayerT> viewer, Route route, PathVisualizer<ViewT, PlayerT> renderer
  ) throws NoPathFoundException {

    return new SingleVisualizerPathImpl<>(() -> {
      try {
        return createPath(route);
      } catch (NoPathFoundException ignored) {
        return new ArrayList<>();
      }
    }, renderer, viewer);
  }

  /**
   * Returns the (potentially completed) graph creation process or starts a new one if none exists.
   */
  private CompletableFuture<MutableValueGraph<Node, Double>> fetchGraph() {
    if (cachedGraph != null) {
      return CompletableFuture.completedFuture(cachedGraph);
    }
    if (generatingFuture == null) {
      generatingFuture = createGraph().thenApply(graph -> {
        cachedGraph = graph;
        generatingFuture = null;
        return graph;
      });
    }
    return generatingFuture;
  }

  /**
   * Generates the current world into one graph representation
   */
  private CompletableFuture<MutableValueGraph<Node, Double>> createGraph() {
    return pathFinder.getStorage().loadNodes()
        .thenCompose(nodes -> {
          Map<UUID, Node> nodeMap = new HashMap<>();
          nodes = constraint.apply(nodes);
          nodes.forEach(node -> nodeMap.put(node.getNodeId(), node));
          Map<UUID, GroupedNode> map = new HashMap<>();

          return pathFinder.getStorage().loadGroupsOfNodes(nodeMap.values()).thenApply(groups -> {
            groups.forEach((node, gs) -> {
              map.put(node.getNodeId(), new GroupedNodeImpl(node, gs));
            });
            return map;
          });
        })
        .thenApply(map -> {

          MutableValueGraph<Node, Double> graph = ValueGraphBuilder
              .directed().allowsSelfLoops(false)
              .build();

          map.values().forEach(graph::addNode);
          for (var entry : map.entrySet()) {
            Node node = entry.getValue().node();
            for (Edge e : node.getEdges()) {
              GroupedNode endGrouped = map.get(e.getEnd());
              Node end = endGrouped == null ? null : endGrouped.node();
              GroupedNode startGrouped = map.get(e.getStart());
              Node start = startGrouped == null ? null : startGrouped.node();
              if (end == null || start == null) {
                pathFinder.getLogger().log(Level.WARNING, "Could not resolve edge while creating graph: " + e
                    + ". Apparently, not all nodes are part of the global group.");
                continue;
              }
              graph.putEdgeValue(startGrouped, endGrouped, node.getLocation().distance(end.getLocation()) * e.getWeight());
            }
          }
          return graph;
        });
  }
}
