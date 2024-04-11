package de.cubbossa.pathfinder.module;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.event.NodeCreateEvent;
import de.cubbossa.pathfinder.event.NodeDeleteEvent;
import de.cubbossa.pathfinder.event.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.event.NodeGroupSaveEvent;
import de.cubbossa.pathfinder.event.NodeSaveEvent;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.group.FindDistanceModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.group.PermissionModifier;
import de.cubbossa.pathfinder.misc.GraphEntrySolver;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.NavigationFilter;
import de.cubbossa.pathfinder.navigation.NavigationHandler;
import de.cubbossa.pathfinder.navigation.NavigationLocation;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.GroupedNodeImpl;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import de.cubbossa.pathfinder.visualizer.GroupedVisualizerPathImpl;
import de.cubbossa.pathfinder.visualizer.PathView;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.SingleVisualizerPathImpl;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.Getter;

public class AbstractNavigationHandler<PlayerT>
    extends PathFinderExtensionBase
    implements PathFinderExtension, NavigationHandler<PlayerT> {

  @Getter
  private static AbstractNavigationHandler<?> instance;

  private ExtensionPoint<NavigationFilter> filterExtensionPoint = new ExtensionPoint<>(NavigationFilter.class);

  @Getter
  private final NamespacedKey key = AbstractPathFinder.pathfinder("navigation");
  protected final PathFinder pathFinder;
  protected EventDispatcher<PlayerT> eventDispatcher;

  protected final Collection<VisualizerPath<PlayerT>> activePaths;
  protected final List<NavigationFilter> navigationFilter;
  private final PathSolver<GroupedNode, Double> pathSolver = new DynamicDijkstra<>();

  // Caches
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> generatingFuture = null;
  private MutableValueGraph<GroupedNode, Double> cachedGraph = null;
  private MutableValueGraph<GroupedNode, Double> cachedGraphWithTargets = null;
  private final List<Location> cachedAgileLocations = new ArrayList<>();

  public AbstractNavigationHandler() {
    instance = this;
    this.activePaths = new HashSet<>();
    this.pathFinder = PathFinderProvider.get();
    this.pathFinder.getDisposer().register(this.pathFinder, this);
    this.navigationFilter = new ArrayList<>();

    this.filterExtensionPoint.getExtensions().forEach(this::registerFindPredicate);
  }

  @Override
  public void dispose() {
    instance = null;
  }

  @Override
  public void registerFindPredicate(NavigationFilter filter) {
    navigationFilter.add(filter);
  }

  @Override
  public boolean canFind(UUID uuid, Node node, Collection<Node> scope) {
    return filterFindables(uuid, scope).contains(node);
  }

  @Override
  public Collection<Node> filterFindables(UUID player, Collection<Node> nodes) {
    Collection<Node> nodeSet = new HashSet<>(nodes);
    for (NavigationFilter f : navigationFilter) {
      nodeSet = f.filterTargetNodes(player, nodeSet);
    }
    return nodeSet;
  }

  @Override
  public Collection<VisualizerPath<PlayerT>> getActivePaths(PathPlayer<PlayerT> player) {
    return activePaths.stream().filter(p -> p.getTargetViewer().equals(player)).toList();
  }

  @Override
  public List<Node> removeIdenticalNeighbours(List<Node> path) {
    List<Node> result = new ArrayList<>();
    GroupedNode last = null;
    for (Node node : path) {
      if (last != null && Objects.equals(last.node().getLocation(), groupedNode.node().getLocation())) {
        last.groups().addAll(groupedNode.groups());
        path.remove(groupedNode);
      }
      last = groupedNode;
    }

    return path;
  }

  @Override
  public CompletableFuture<VisualizerPath<PlayerT>> renderPath(PathPlayer<PlayerT> viewer, Route route) {

    return fetchGraph().thenApply(g -> {
      VisualizerPath<PlayerT> path = new GroupedVisualizerPathImpl<>(viewer, route.calculatePaths(g));

      activePaths.add(path);
      path.startUpdater(1000);
      return path;
    });

  }

  @Override
  public <ViewT extends PathView<PlayerT>> CompletableFuture<VisualizerPath<PlayerT>> renderPath(
      PathPlayer<PlayerT> viewer, Route route, PathVisualizer<ViewT, PlayerT> renderer
  ) throws NoPathFoundException {

    return fetchGraph().thenApply(g -> {
      return new SingleVisualizerPathImpl<>(
          route.calculatePath(g).getPath(), renderer, viewer
      );
    });
  }

  @Override
  public void cancelPathWhenTargetReached(VisualizerPath<PlayerT> path) {
    NodeGroup highest = path.get(path.size() - 1).groups().stream()
        .filter(g -> g.hasModifier(FindDistanceModifier.KEY))
        .max(NodeGroup::compareTo).orElse(null);

    double findDist = highest == null ? 1.5 : highest.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY)
        .map(FindDistanceModifier::distance).orElse(1.5);

    boolean updating = navigateLocations.stream().anyMatch(NavigateLocation::isFixedPosition);

    return renderPath(viewer, path, path.get(path.size() - 1).node().getLocation(), (float) findDist, !updating ? null : () -> {
      return getGraph(Collections.singleton(start), target).thenCompose(graph -> findPath(graph, start, target)).join();
    });
  }

  public void unsetPath(PathPlayer<PlayerT> playerId) {
    if (activePaths.containsKey(playerId)) {
      unsetPath(activePaths.get(playerId));
    }
  }

  public void onLoad(PathFinder pathPlugin) {

    if (!pathFinder.getConfiguration().getModuleConfig().isNavigationModule()) {
      disable();
    }
    this.eventDispatcher = (EventDispatcher<PlayerT>) pathFinder.getEventDispatcher();
  }

  public void onEnable(PathFinder pathPlugin) {

    registerFindPredicate((playerId, scope) -> {
      PathPlayer<?> player = AbstractPathFinder.getInstance().wrap(playerId);
      Map<Node, Collection<NodeGroup>> groups = PathFinderProvider.get().getStorage().loadGroupsOfNodes(scope).join();

      if (player.unwrap() == null) {
        return new HashSet<>();
      }

      return groups.entrySet().stream()
          .filter(e -> {
            return e.getValue().stream().allMatch(group -> {
              Optional<PermissionModifier> mod = group.getModifier(PermissionModifier.KEY);
              return mod.isEmpty() || player.hasPermission(mod.get().permission());
            });
          })
          .map(Map.Entry::getKey)
          .collect(Collectors.toList());
    });

    eventDispatcher.listen(NodeCreateEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeGroupDeleteEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeSaveEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeGroupSaveEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeDeleteEvent.class, e -> cachedGraph = null);
    eventDispatcher.listen(NodeGroupDeleteEvent.class, e -> cachedGraph = null);

    fetchGraph();
  }

  /**
   * Fetches the node setup as graph (might be cached) and inserts a collection of NavigateLocations into the graph.
   * The result might again be cached if all NavigateLocations are either static or agile but haven't changed their
   * location since the last call.
   *
   * @param exits A collection of NavigateLocation instances. The first one might represent the start point. All points
   *              will be inserted by finding their closest edge and creating two edges from the point to both edge ends.
   */
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> getGraph(Collection<NavigationLocation> entries, Collection<NavigationLocation> exits) {

    Collection<NavigationLocation> locations = new HashSet<>(entries);
    locations.addAll(exits);

    // TODO
    // check if any agile location has changed. If not, just return the last graph.
    if (false && cachedGraphWithTargets != null && locations.stream().filter(l -> !l.isFixedPosition()).map(NavigationLocation::getNode)
        .map(Node::getLocation).toList().equals(cachedAgileLocations)) {
      return CompletableFuture.completedFuture(cachedGraphWithTargets);
    }

    // a location has changed, we recreate the path with inserted targets
    return fetchGraph().thenApply(graph -> {
      GraphEntrySolver<GroupedNode> solver = new EdgeBasedGraphEntrySolver();

      for (NavigationLocation location : entries) {
        if (!location.isExternal()) {
          continue;
        }

        GroupedNode g = new GroupedNodeImpl(location.getNode(), new HashSet<>());
        graph.addNode(g);
        graph = solver.solveEntry(g, graph);
        graph.successors(g).forEach((groupedNode) -> g.groups().addAll(groupedNode.groups()));
      }
      for (NavigationLocation location : exits) {
        if (!location.isExternal()) {
          continue;
        }

        GroupedNode g = new GroupedNodeImpl(location.getNode(), new HashSet<>());
        graph.addNode(g);
        graph = solver.solveExit(g, graph);
        graph.predecessors(g).forEach((groupedNode) -> g.groups().addAll(groupedNode.groups()));
      }

      // cache results
      cachedGraphWithTargets = graph;
      cachedAgileLocations.clear();
      cachedAgileLocations.addAll(locations.stream().filter(NavigationLocation::isFixedPosition)
          .map(NavigationLocation::getNode).map(Node::getLocation).toList());
      return graph;
    });
  }

  /**
   * Returns the (potentially completed) graph creation process or starts a new one if none exists.
   */
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> fetchGraph() {
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
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> createGraph() {
    return pathFinder.getStorage().loadNodes()
        .thenCompose(nodes -> {
          Map<UUID, Node> nodeMap = new HashMap<>();
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

          MutableValueGraph<GroupedNode, Double> graph = ValueGraphBuilder
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
          pathSolver.setGraph(graph);
          return graph;
        });
  }
}