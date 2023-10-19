package de.cubbossa.pathfinder;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.group.PermissionModifier;
import de.cubbossa.pathapi.misc.GraphEntrySolver;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.navigation.NavigationHandler;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.SimpleGroupedNode;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver;
import de.cubbossa.pathfinder.visualizer.CommonVisualizerPath;
import de.cubbossa.translations.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AbstractNavigationHandler<PlayerT> implements Listener, PathFinderExtension, NavigationHandler<PlayerT> {

  // TODO player paths separate

  @Getter
  private static AbstractNavigationHandler<?> instance;

  @Getter
  private final NamespacedKey key = CommonPathFinder.pathfinder("navigation");
  protected final PathFinder pathFinder;
  protected EventDispatcher<PlayerT> eventDispatcher;

  protected final Map<PathPlayer<PlayerT>, SearchInfo<PlayerT>> activePaths;
  protected final List<Function<NavigationRequestContext, Collection<Node>>> navigationFilter;
  private final PathSolver<GroupedNode> pathSolver = new DynamicDijkstra<>();

  // Caches
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> generatingFuture = null;
  private MutableValueGraph<GroupedNode, Double> cachedGraph = null;
  private MutableValueGraph<GroupedNode, Double> cachedGraphWithTargets = null;
  private final List<Location> cachedAgileLocations = new ArrayList<>();

  public AbstractNavigationHandler() {
    this.activePaths = new HashMap<>();
    this.pathFinder = PathFinderProvider.get();
    this.navigationFilter = new ArrayList<>();
    instance = this;
  }

  public static <PlayerT> void printResult(NavigateResult result, PathPlayer<PlayerT> player) {
    // success played from effects.
    Message message = switch (result) {
      case FAIL_BLOCKED, FAIL_EVENT_CANCELLED -> Messages.CMD_FIND_BLOCKED;
      case FAIL_EMPTY -> Messages.CMD_FIND_EMPTY;
      case FAIL_TOO_FAR_AWAY -> Messages.CMD_FIND_TOO_FAR;
      default -> null;
    };
    if (message != null) {
      player.sendMessage(message);
    }
  }

  @Override
  public void registerFindPredicate(Function<NavigationRequestContext, Collection<Node>> filter) {
    navigationFilter.add(filter);
  }

  @Override
  public boolean canFind(UUID uuid, Node node, Collection<Node> scope) {
    return filterFindables(uuid, scope).contains(node);
  }

  @Override
  public Collection<Node> filterFindables(UUID player, Collection<Node> nodes) {
    Collection<Node> nodeSet = new HashSet<>(nodes);
    for (Function<NavigationRequestContext, Collection<Node>> f : navigationFilter) {
      nodeSet = f.apply(new NavigationRequestContext(player, nodeSet));
    }
    return nodeSet;
  }

  @Override
  public @Nullable SearchInfo<PlayerT> getActivePath(PathPlayer<PlayerT> player) {
    return activePaths.get(player);
  }

  @Override
  public CompletableFuture<NavigateResult> findPathToLocation(PathPlayer<PlayerT> player, Location target) {
    return findPath(player, Set.of(CommonNavigateLocation.staticLocation(target)));
  }

  @Override
  public CompletableFuture<NavigateResult> findPathToNodes(PathPlayer<PlayerT> player, Collection<Node> targets) {
    NavigateLocation location = new CommonNavigateLocation(new PlayerNode(player));
    location.setAgile(true);
    return findPath(player, location, targets.stream()
        .map(Node::getLocation)
        .map(CommonNavigateLocation::staticLocation)
        .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, Collection<NavigateLocation> target) {
    return findPath(viewer, CommonNavigateLocation.agileNode(new PlayerNode(viewer)), target);
  }

  @Override
  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, Collection<NavigateLocation> target,
                                                    double maxDist) {
    return findPath(viewer, CommonNavigateLocation.agileNode(new PlayerNode(viewer)), target, maxDist);
  }

  @Override
  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, NavigateLocation start,
                                                    Collection<NavigateLocation> target) {
    return findPath(viewer, start, target, pathFinder.getConfiguration().getNavigation().getFindLocation().getMaxDistance());
  }

  @Override
  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, NavigateLocation start,
                                                    Collection<NavigateLocation> target, double maxDist) {

    Preconditions.checkNotNull(start);
    Preconditions.checkNotNull(target);
    Preconditions.checkArgument(target.size() >= 1, "Targets must contain at least one valid node.");

    Collection<NavigateLocation> navigateLocations = new LinkedList<>();
    navigateLocations.add(start);
    navigateLocations.addAll(target);

    // graph becomes static, so this step and all following must be repeated for each path update.
    return getGraph(Collections.singleton(start), target).thenCompose(graph -> findPath(graph, start, target)).thenApply(path -> {

      NodeGroup highest = path.get(path.size() - 1).groups().stream()
              .filter(g -> g.hasModifier(FindDistanceModifier.KEY))
              .max(NodeGroup::compareTo).orElse(null);

      double findDist = highest == null ? 1.5 : highest.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY)
              .map(FindDistanceModifier::distance).orElse(1.5);

      boolean updating = navigateLocations.stream().anyMatch(NavigateLocation::isAgile);

      return setPath(viewer, path, path.get(path.size() - 1).node().getLocation(), (float) findDist, !updating ? null : () -> {
        System.out.println("Update " + start.getNode().getLocation());
        return getGraph(Collections.singleton(start), target).thenCompose(graph -> findPath(graph, start, target)).join();
      });

    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return NavigateResult.FAIL_UNKNOWN;
    });
  }

  private CompletableFuture<List<GroupedNode>> findPath(MutableValueGraph<GroupedNode, Double> graph, NavigateLocation start,
                                                        Collection<NavigateLocation> targets) {

    return pathFinder.getStorage().loadNodes().thenApply(nodes -> {

      Map<UUID, GroupedNode> graphMapping = new HashMap<>();
      graph.nodes().forEach(groupedNode -> graphMapping.put(groupedNode.node().getNodeId(), groupedNode));
      Collection<GroupedNode> convertedTargets = targets.stream()
          .map(node -> graphMapping.get(node.getNode().getNodeId()))
          .toList();
      List<GroupedNode> path;

      try {
        pathSolver.setGraph(graph); // TODO fix for performance improvement
        path = pathSolver.solvePath(graphMapping.get(start.getNode().getNodeId()), convertedTargets);
      } catch (NoPathFoundException e) {
        throw new RuntimeException(e);
      }

      // Drop all duplicate nodes, if they share the same location and are next to each other in path
      GroupedNode last = null;
      for (GroupedNode groupedNode : new LinkedList<>(path)) {
        if (last != null && Objects.equals(last.node().getLocation(), groupedNode.node().getLocation())) {
          last.groups().addAll(groupedNode.groups());
          path.remove(groupedNode);
        }
        last = groupedNode;
      }

      return path;
    });
  }

  public NavigateResult setPath(PathPlayer<PlayerT> player, @NotNull List<GroupedNode> pathNodes, Location target,
                                float distance) {
    return setPath(player, pathNodes, target, distance, null);
  }

  public NavigateResult setPath(PathPlayer<PlayerT> player, @NotNull Supplier<List<GroupedNode>> pathNodeSupplier,
                                Location target, float distance) {

    List<GroupedNode> pathNodes = pathNodeSupplier.get();
    VisualizerPath<PlayerT> visualizerPath = new CommonVisualizerPath<>(pathNodes, player);

    boolean success = eventDispatcher.dispatchPathStart(player, visualizerPath, target, distance);
    if (!success) {
      return NavigateResult.FAIL_EVENT_CANCELLED;
    }
    return setPath(player, pathNodes, target, distance, pathNodeSupplier);
  }

  private NavigateResult setPath(PathPlayer<PlayerT> player, @NotNull List<GroupedNode> pathNodes, Location target,
                                 float distance, @Nullable Supplier<List<GroupedNode>> pathNodeSupplier) {
    VisualizerPath<PlayerT> visualizerPath = new CommonVisualizerPath<>(pathNodes, player);

    boolean success = eventDispatcher.dispatchPathStart(player, visualizerPath, target, distance);
    if (!success) {
      return NavigateResult.FAIL_EVENT_CANCELLED;
    }

    SearchInfo<PlayerT> current = activePaths.put(player, new SearchInfo<>(player, visualizerPath, target, distance));
    if (current != null) {
      current.path().removeViewer(player);
    }
    visualizerPath.addViewer(player);

    if (pathNodeSupplier != null) {
      visualizerPath.startUpdater(pathNodeSupplier, 1000);
    }

    return NavigateResult.SUCCESS;
  }

  @Override
  public void unsetPath(PathPlayer<PlayerT> playerId) {
    if (activePaths.containsKey(playerId)) {
      unsetPath(activePaths.get(playerId));
    }
  }

  @Override
  public void unsetPath(SearchInfo<PlayerT> info) {
    activePaths.remove(info.player());
    info.path().removeViewer(info.player());
    info.path().stopUpdater();

    eventDispatcher.dispatchPathStopped(info.player(), info.path(), info.target(), info.distance());
  }

  @Override
  public void cancelPath(PathPlayer<PlayerT> playerId) {
    if (activePaths.containsKey(playerId)) {
      cancelPath(activePaths.get(playerId));
    }
  }

  @Override
  public void cancelPath(SearchInfo<PlayerT> info) {
    if (!eventDispatcher.dispatchPathCancel(info.player(), info.path())) {
      return;
    }
    unsetPath(info);
  }

  @Override
  public void reachTarget(SearchInfo<PlayerT> info) {

    if (!eventDispatcher.dispatchPathTargetReached(info.player(), info.path())) {
      return;
    }
    unsetPath(info);
  }

  public void onLoad(PathFinder pathPlugin) {

    if (!pathFinder.getConfiguration().getModuleConfig().isNavigationModule()) {
      pathFinder.getExtensionRegistry().unregisterExtension(this);
    }
    this.eventDispatcher = (EventDispatcher<PlayerT>) pathFinder.getEventDispatcher();
  }

  public void onEnable(PathFinder pathPlugin) {

    registerFindPredicate(c -> {
      PathPlayer<?> player = CommonPathFinder.getInstance().wrap(c.playerId());
      Map<Node, Collection<NodeGroup>> groups = PathFinderProvider.get().getStorage().loadGroupsOfNodes(c.nodes()).join();

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
   * @param locations A collection of NavigateLocation instances. The first one might represent the start point. All points
   *                  will be inserted by finding their closest edge and creating two edges from the point to both edge ends.
   */
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> getGraph(Collection<NavigateLocation> entries, Collection<NavigateLocation> exits) {

    Collection<NavigateLocation> locations = new HashSet<>(entries);
    locations.addAll(exits);

    // check if any agile location has changed. If not, just return the last graph.
    if (false && cachedGraphWithTargets != null && locations.stream().filter(NavigateLocation::isAgile).map(NavigateLocation::getNode)
            .map(Node::getLocation).toList().equals(cachedAgileLocations)) {
      return CompletableFuture.completedFuture(cachedGraphWithTargets);
    }

    // a location has changed, we recreate the path with inserted targets
    return fetchGraph().thenApply(graph -> {
      GraphEntrySolver<GroupedNode> solver = new EdgeBasedGraphEntrySolver();

      for (NavigateLocation location : entries) {
        GroupedNode g = new SimpleGroupedNode(location.getNode(), new HashSet<>());
        graph.addNode(g);
        graph = solver.solveEntry(g, graph);
        graph.successors(g).forEach((groupedNode) -> g.groups().addAll(groupedNode.groups()));
      }
      for (NavigateLocation location : exits) {
        GroupedNode g = new SimpleGroupedNode(location.getNode(), new HashSet<>());
        graph.addNode(g);
        graph = solver.solveExit(g, graph);
        graph.successors(g).forEach((groupedNode) -> g.groups().addAll(groupedNode.groups()));
      }

      // cache results
      cachedGraphWithTargets = graph;
      cachedAgileLocations.clear();
      cachedAgileLocations.addAll(locations.stream().filter(NavigateLocation::isAgile)
              .map(NavigateLocation::getNode).map(Node::getLocation).toList());
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
    System.out.println("Create graph " + ((StorageImpl) pathFinder.getStorage()).getIoExecutor().isShutdown());
    return pathFinder.getStorage().loadNodes().thenCompose(nodes -> {
      System.out.println("one " + ((StorageImpl) pathFinder.getStorage()).getIoExecutor().isShutdown());
      Map<UUID, Node> nodeMap = new HashMap<>();
      nodes.forEach(node -> nodeMap.put(node.getNodeId(), node));
      Map<UUID, GroupedNode> map = new HashMap<>();

      return pathFinder.getStorage().loadGroupsOfNodes(nodeMap.values()).thenApply(groups -> {
        System.out.println("two");
        groups.forEach((node, gs) -> {
          map.put(node.getNodeId(), new SimpleGroupedNode(node, gs));
        });
        return map;
      });
    }).thenApply(map -> {

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

  @RequiredArgsConstructor
  @Getter
  @Setter
  static class CommonNavigateLocation implements NavigateLocation {

    private final Node node;
    private boolean agile;

    public static NavigateLocation agileNode(Node node) {
      NavigateLocation loc = new CommonNavigateLocation(node);
      loc.setAgile(true);
      return loc;
    }

    public static NavigateLocation agileLocation(Supplier<Location> loc) {
      NavigateLocation navloc = new CommonNavigateLocation(new Waypoint(UUID.randomUUID()) {
        @Override
        public Location getLocation() {
          return loc.get();
        }
      });
      navloc.setAgile(true);
      return navloc;
    }

    public static NavigateLocation staticLocation(Location location) {
      Node n = new Waypoint(UUID.randomUUID());
      n.setLocation(location);
      return new CommonNavigateLocation(n);
    }
  }
}