package de.cubbossa.pathfinder.module;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.event.NodeCreateEvent;
import de.cubbossa.pathapi.event.NodeDeleteEvent;
import de.cubbossa.pathapi.event.NodeGroupDeleteEvent;
import de.cubbossa.pathapi.event.NodeGroupSaveEvent;
import de.cubbossa.pathapi.event.NodeSaveEvent;
import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.group.PermissionModifier;
import de.cubbossa.pathapi.misc.GraphEntrySolver;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.SimpleGroupedNode;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.visualizer.CommonVisualizerPath;
import de.cubbossa.translations.Message;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Listener;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbstractNavigationHandler<PlayerT> implements Listener, PathFinderExtension {

  @Getter
  private static AbstractNavigationHandler<?> instance;

  protected final Map<PathPlayer<PlayerT>, SearchInfo<PlayerT>> activePaths;
  protected final PathFinder pathFinder;
  protected final List<Function<NavigationRequestContext, Collection<Node>>> navigationFilter;
  @Getter
  private final NamespacedKey key = CommonPathFinder.pathfinder("navigation");
  protected EventDispatcher<PlayerT> eventDispatcher;

  private final PathSolver<GroupedNode> pathSolver = new DynamicDijkstra<>();
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> generatingFuture = null;
  private MutableValueGraph<GroupedNode, Double> cachedGraph = null;

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

  public void registerFindPredicate(Function<NavigationRequestContext, Collection<Node>> filter) {
    navigationFilter.add(filter);
  }

  public boolean canFind(UUID uuid, Node node, Collection<Node> scope) {
    return filterFindables(uuid, scope).contains(node);
  }

  public Collection<Node> filterFindables(UUID player, Collection<Node> nodes) {
    Collection<Node> nodeSet = new HashSet<>(nodes);
    for (Function<NavigationRequestContext, Collection<Node>> f : navigationFilter) {
      nodeSet = f.apply(new NavigationRequestContext(player, nodeSet));
    }
    return nodeSet;
  }

  public @Nullable SearchInfo<PlayerT> getActivePath(PathPlayer<PlayerT> player) {
    return activePaths.get(player);
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, Location target) {
    return findPath(player, Set.of(NavigateLocation.staticLocation(target)));
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, NodeSelection targets) {
    NavigateLocation location = new NavigateLocation(new PlayerNode(player));
    location.setAgile(true);
    return findPath(player, location, targets.stream()
        .map(NavigateLocation::new)
        .collect(Collectors.toList()));
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, Collection<NavigateLocation> target) {
    NavigateLocation location = new NavigateLocation(new PlayerNode(viewer));
    location.setAgile(true);
    return findPath(viewer, location, target);
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, Collection<NavigateLocation> target,
                                                    double maxDist) {
    NavigateLocation navigateLocation = new NavigateLocation(new PlayerNode(viewer));
    navigateLocation.setAgile(true);
    return findPath(viewer, navigateLocation, target, maxDist);
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, NavigateLocation start,
                                                    Collection<NavigateLocation> target) {
    return findPath(viewer, start, target, pathFinder.getConfiguration().getNavigation().getFindLocation().getMaxDistance());
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, NavigateLocation start,
                                                    Collection<NavigateLocation> target, double maxDist) {

    Preconditions.checkNotNull(start);
    Preconditions.checkNotNull(target);
    Preconditions.checkArgument(target.size() >= 1, "Targets must contain at least one valid node.");

    Collection<NavigateLocation> navigateLocations = new LinkedList<>();
    navigateLocations.add(start);
    navigateLocations.addAll(target);

    // graph becomes static, so this step and all following must be repeated for each path update.
    return getGraph(navigateLocations).thenCompose(graph -> findPath(graph, start, target)).thenApply(path -> {

      NodeGroup highest = path.get(path.size() - 1).groups().stream()
          .filter(g -> g.hasModifier(FindDistanceModifier.KEY))
          .max(NodeGroup::compareTo).orElse(null);

      double findDist = highest == null ? 1.5 : highest.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY)
          .map(FindDistanceModifier::distance).orElse(1.5);

      boolean updating = navigateLocations.stream().noneMatch(NavigateLocation::isAgile);

      return setPath(viewer, path, path.get(path.size() - 1).node().getLocation(), (float) findDist, !updating
          ? null : () -> getGraph(navigateLocations).thenCompose(graph -> findPath(graph, start, target)).join());

    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return NavigateResult.FAIL_UNKNOWN;
    });
  }

  /**
   * Solves on existing and static graph that already contains start and targets
   *
   * @return A list of grouped nodes that resemble the solved path
   */
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

  public NavigateResult setPath(PathPlayer<PlayerT> player, @NotNull List<GroupedNode> pathNodes, Location target, float distance) {
    return setPath(player, pathNodes, target, distance, null);
  }

  public NavigateResult setPath(PathPlayer<PlayerT> player, @NotNull Supplier<List<GroupedNode>> pathNodeSupplier, Location target, float distance) {

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
      visualizerPath.startUpdater(pathNodeSupplier, 100);
    }

    return NavigateResult.SUCCESS;
  }

  public void unsetPath(PathPlayer<PlayerT> playerId) {
    if (activePaths.containsKey(playerId)) {
      unsetPath(activePaths.get(playerId));
    }
  }

  public void unsetPath(SearchInfo<PlayerT> info) {
    activePaths.remove(info.player());
    info.path().removeViewer(info.player());
    info.path().stopUpdater();

    eventDispatcher.dispatchPathStopped(info.player, info.path, info.target, info.distance);
  }

  public void cancelPath(PathPlayer<PlayerT> playerId) {
    if (activePaths.containsKey(playerId)) {
      cancelPath(activePaths.get(playerId));
    }
  }

  public void cancelPath(SearchInfo<PlayerT> info) {
    if (!eventDispatcher.dispatchPathCancel(info.player, info.path)) {
      return;
    }
    unsetPath(info);
  }

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
      PathPlayer<?> player = CommonPathFinder.getInstance().wrap(c.playerId);
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
  private CompletableFuture<MutableValueGraph<GroupedNode, Double>> getGraph(Collection<NavigateLocation> locations) {
    // TODO check if locations changed and if not return cached value.

    return fetchGraph().thenApply(graph -> {
      GraphEntrySolver<GroupedNode> solver = new EdgeBasedGraphEntrySolver();

      for (NavigateLocation location : locations) {
        GroupedNode playerNode = new SimpleGroupedNode(location.getNode(), new HashSet<>());
        graph.addNode(playerNode);
        graph = solver.solve(playerNode, graph);
        graph.successors(playerNode).forEach((groupedNode) -> playerNode.groups().addAll(groupedNode.groups()));
      }

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
    return pathFinder.getStorage().loadNodes().thenApply(nodes -> {
      Map<UUID, Node> nodeMap = new HashMap<>();
      nodes.forEach(node -> nodeMap.put(node.getNodeId(), node));
      Map<UUID, GroupedNode> map = new HashMap<>();

      pathFinder.getStorage().loadGroups(nodes.stream().map(Node::getNodeId).collect(Collectors.toSet())).thenAccept(groups -> {
        groups.forEach((uuid, gs) -> {
          map.put(uuid, new SimpleGroupedNode(nodeMap.get(uuid), gs));
        });
      }).join();

      MutableValueGraph<GroupedNode, Double> graph = ValueGraphBuilder
          .directed().allowsSelfLoops(false)
          .build();

      map.values().forEach(graph::addNode);
      for (Node node : nodes) {
        for (Edge e : node.getEdges()) {
          GroupedNode endGrouped = map.get(e.getEnd());
          Node end = endGrouped == null ? null : endGrouped.node();
          GroupedNode startGrouped = map.get(e.getStart());
          Node start = startGrouped == null ? null : startGrouped.node();
          if (end == null || start == null) {
            pathFinder.getLogger().log(Level.WARNING, "Could not resolve edge while creating graph: " + e +
                ". Apparently, not all nodes are part of the global group.");
            continue;
          }
          graph.putEdgeValue(startGrouped, endGrouped, node.getLocation().distance(end.getLocation()) * e.getWeight());
        }
      }
      pathSolver.setGraph(graph);
      return graph;
    });
  }

  public enum NavigateResult {
    SUCCESS, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED,
    FAIL_TOO_FAR_AWAY, FAIL_UNKNOWN;
  }

  public record NavigationRequestContext(UUID playerId, Collection<Node> nodes) {
  }

  public record SearchInfo<PlayerT>(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path, Location target,
                                    float distance) {
  }

  /**
   * A navigation target represents any possible and possibly moving node in the navigation graph.
   */
  @RequiredArgsConstructor
  @Getter
  @Setter
  public static class NavigateLocation {
    private final Node node;
    private boolean agile;

    public static NavigateLocation agileNode(Node node) {
      NavigateLocation loc = new NavigateLocation(node);
      loc.setAgile(true);
      return loc;
    }

    public static NavigateLocation agileLocation(Supplier<Location> loc) {
      return null; // TODO make a node type that accepts location supplier
    }

    public static NavigateLocation staticLocation(Location location) {
      Node n = new Waypoint(UUID.randomUUID());
      n.setLocation(location);
      return new NavigateLocation(n);
    }
  }
}
