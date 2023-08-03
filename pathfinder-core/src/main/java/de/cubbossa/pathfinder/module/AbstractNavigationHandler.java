package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.group.PermissionModifier;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.OptimizedDijkstra;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.SimpleGroupedNode;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.visualizer.CommonVisualizerPath;
import de.cubbossa.translations.Message;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AbstractNavigationHandler<PlayerT> implements Listener, PathFinderExtension {

  @Getter
  private static AbstractNavigationHandler<?> instance;

  public enum NavigateResult {
    SUCCESS, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED,
    FAIL_TOO_FAR_AWAY, FAIL_UNKNOWN;
  }

  public record NavigationRequestContext(UUID playerId, Collection<Node> nodes) {
  }

  public record SearchInfo<PlayerT>(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path, Location target,
                                    float distance) {
  }

  protected final Map<PathPlayer<PlayerT>, SearchInfo<PlayerT>> activePaths;
  protected final PathFinder pathFinder;
  protected final List<Function<NavigationRequestContext, Collection<Node>>> navigationFilter;
  @Getter
  private final NamespacedKey key = CommonPathFinder.pathfinder("navigation");
  protected EventDispatcher<PlayerT> eventDispatcher;

  private CompletableFuture<Graph<GroupedNode>> generatingFuture = null;
  private Graph<GroupedNode> cachedGraph = null;

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

  private Graph<GroupedNode> insertPlayer(Graph<GroupedNode> graph, PlayerNode player) {
    GroupedNode playerNode = new SimpleGroupedNode(player, new HashSet<>());
    graph.addNode(playerNode);
    graph = new EdgeBasedGraphEntrySolver().solve(playerNode, graph);
    graph.getEdges(playerNode).forEach((groupedNode, weight) -> {
      playerNode.groups().addAll(groupedNode.groups());
    });
    return graph;
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, Location location) {
    return findPath(player, location, pathFinder.getConfiguration().getNavigation().getFindLocation().getMaxDistance());
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, Location location, double maxDist) {


    PlayerNode playerNode = new PlayerNode(player);
    return getGraph(playerNode).thenApply(graph -> {

      Location l = location.clone();
      double _maxDist = maxDist < 0 ? Double.MAX_VALUE : maxDist;
      // check if x y and z are equals. Cannot cast raycast to self, therefore if statement required
      Location _location = l.equals(player.getLocation())
          ? l.add(0, 0.01, 0) : l;

      GroupedNode closest = null;
      double dist = Double.MAX_VALUE;
      for (GroupedNode node : graph) {
        double curDist = node.node().getLocation().distance(_location);
        if (curDist < dist && curDist < _maxDist) {
          closest = node;
          dist = curDist;
        }
      }
      if (closest == null) {
        return NavigateResult.FAIL_TOO_FAR_AWAY;
      }

      Waypoint waypoint = new Waypoint(UUID.randomUUID());
      GroupedNode wpGrouped = new SimpleGroupedNode(waypoint, new HashSet<>());
      waypoint.setLocation(_location);
      wpGrouped.groups().addAll(closest.groups());

      graph.addNode(wpGrouped);
      graph.connect(closest, wpGrouped);
      return findPath(player, graph, playerNode, new NodeSelection(waypoint)).join();
    });
  }

  private CompletableFuture<Graph<GroupedNode>> getGraph(PlayerNode playerNode) {
    return fetchGraph().thenApply(graph -> insertPlayer(graph, playerNode));
  }

  private CompletableFuture<Graph<GroupedNode>> fetchGraph() {
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

  private CompletableFuture<Graph<GroupedNode>> createGraph() {
    return pathFinder.getStorage().loadNodes().thenApply(nodes -> {
      Map<UUID, Node> nodeMap = new HashMap<>();
      nodes.forEach(node -> nodeMap.put(node.getNodeId(), node));
      Map<UUID, GroupedNode> map = new HashMap<>();

      pathFinder.getStorage().loadGroups(nodes.stream().map(Node::getNodeId).collect(Collectors.toSet())).thenAccept(groups -> {
        groups.forEach((uuid, gs) -> {
          map.put(uuid, new SimpleGroupedNode(nodeMap.get(uuid), gs));
        });
      }).join();

      Graph<GroupedNode> graph = new Graph<>();
      map.values().forEach(graph::addNode);
      for (Node node : nodes) {
        for (Edge e : node.getEdges()) {
          GroupedNode endGrouped = map.get(e.getEnd());
          Node end = endGrouped == null ? null : endGrouped.node();
          GroupedNode startGrouped = map.get(e.getStart());
          Node start = startGrouped == null ? null : startGrouped.node();
          if (end == null || start == null) {
            pathFinder.getLogger().log(Level.WARNING, "Could not resolve edge while creating graph: " + e + ". Apparently, not all nodes are part of the global group.");
            continue;
          }
          graph.connect(startGrouped, endGrouped, node.getLocation().distance(end.getLocation()) * e.getWeight());
        }
      }
      return graph;
    });
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, NodeSelection targets) {
    PlayerNode playerNode = new PlayerNode(player);
    return getGraph(playerNode).thenCompose(graph -> findPath(player, graph, playerNode, targets));
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, Graph<GroupedNode> graph, Node start, NodeSelection targets) {

    if (targets.size() == 0) {
      return CompletableFuture.completedFuture(NavigateResult.FAIL_EMPTY);
    }
    return pathFinder.getStorage().loadNodes().thenApply(nodes -> {

      PathSolver<GroupedNode> pathSolver = new OptimizedDijkstra<>();
      Map<UUID, GroupedNode> graphMapping = new HashMap<>();
      graph.forEach(groupedNode -> graphMapping.put(groupedNode.node().getNodeId(), groupedNode));
      List<GroupedNode> path;
      Collection<GroupedNode> convertedTargets = targets.stream()
          .map(node -> graphMapping.get(node.getNodeId()))
          .toList();
      try {
        path = pathSolver.solvePath(graph, graphMapping.get(start.getNodeId()), convertedTargets);
      } catch (NoPathFoundException e) {
        return NavigateResult.FAIL_BLOCKED;
      }
      GroupedNode last = null;
      for (GroupedNode groupedNode : new LinkedList<>(path)) {
        if (Objects.equals(last == null ? null : last.node().getLocation(), groupedNode.node().getLocation())) {
          last.groups().addAll(groupedNode.groups());
          path.remove(groupedNode);
        }
        last = groupedNode;
      }

      NodeGroup highest = path.get(path.size() - 1).groups().stream()
          .filter(g -> g.hasModifier(FindDistanceModifier.KEY))
          .max(NodeGroup::compareTo).orElse(null);

      double findDist = highest == null ? 1.5 : highest.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY)
          .map(FindDistanceModifier::distance).orElse(1.5);
      return setPath(player, path, path.get(path.size() - 1).node().getLocation(), (float) findDist);
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return NavigateResult.FAIL_UNKNOWN;
    });
  }

  public NavigateResult setPath(PathPlayer<PlayerT> player, @NotNull List<GroupedNode> pathNodes, Location target, float distance) {
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
}
