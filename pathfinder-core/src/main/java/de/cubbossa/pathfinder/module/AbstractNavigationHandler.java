package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.group.PermissionModifier;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.SimpleDijkstra;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.SimpleGroupedNode;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.visualizer.CommonVisualizerPath;
import de.cubbossa.translations.Message;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AbstractNavigationHandler<PlayerT> implements Listener, PathFinderExtension {

  @Getter
  private static AbstractNavigationHandler<?> instance;

  public enum NavigateResult {
    SUCCESS, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED,
    FAIL_TOO_FAR_AWAY;
  }

  public record NavigationRequestContext(UUID playerId, Node node) {
  }

  public record SearchInfo<PlayerT>(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path, Location target,
                                    float distance) {
  }

  protected final Map<PathPlayer<PlayerT>, SearchInfo<PlayerT>> activePaths;
  protected final PathFinder pathFinder;
  protected final List<Predicate<NavigationRequestContext>> navigationFilter;
  @Getter
  private final NamespacedKey key = CommonPathFinder.pathfinder("navigation");
  protected EventDispatcher<PlayerT> eventDispatcher;

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

  public void registerFindPredicate(Predicate<NavigationRequestContext> filter) {
    navigationFilter.add(filter);
  }

  public boolean canFind(NavigationRequestContext ctx) {
    return navigationFilter.stream().allMatch(p -> p.test(ctx));
  }

  public List<Predicate<NavigationRequestContext>> getNavigationFilter() {
    return new ArrayList<>(navigationFilter);
  }

  public @Nullable SearchInfo<PlayerT> getActivePath(PathPlayer<PlayerT> player) {
    return activePaths.get(player);
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, Location location) {
    return findPath(player, location, pathFinder.getConfiguration().getNavigation().getFindLocation().getMaxDistance());
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, Location location, double maxDist) {
    return pathFinder.getStorage().loadNodes().thenApply(nodes -> {
      double _maxDist = maxDist < 0 ? Double.MAX_VALUE : maxDist;
      // check if x y and z are equals. Cannot cast raycast to self, therefore if statement required
      Location _location = location.equals(player.getLocation())
          ? location.add(0, 0.01, 0) : location;

      Node closest = null;
      double dist = Double.MAX_VALUE;
      for (Node node : nodes) {
        double curDist = node.getLocation().distance(_location);
        if (curDist < dist && curDist < _maxDist) {
          closest = node;
          dist = curDist;
        }
      }
      if (closest == null) {
        return NavigateResult.FAIL_TOO_FAR_AWAY;
      }
      final Node fClosest = closest;
      Waypoint waypoint = new Waypoint(UUID.randomUUID());
      waypoint.setLocation(_location);
      // we can savely add edges because the fClosest object is only a representation of the stored node.
      fClosest.connect(waypoint);

      return findPath(player, new NodeSelection(waypoint)).join();
    });
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> player, NodeSelection targets) {

    if (targets.size() == 0) {
      return CompletableFuture.completedFuture(NavigateResult.FAIL_EMPTY);
    }

    PlayerNode playerNode = new PlayerNode(player);

    return NodeHandler.getInstance().createGraph(playerNode).thenCompose(graph -> {
      return pathFinder.getStorage().loadNodes().thenApply(nodes -> {

        PathSolver<Node> pathSolver = new SimpleDijkstra<>();
        List<GroupedNode> path;
        try {
          path = pathSolver.solvePath(graph, playerNode, targets.stream()
                  .filter(nodes::contains)
                  .collect(Collectors.toList())).stream().parallel()
              .map(node -> new SimpleGroupedNode(node, StorageUtil.getGroups(node)))
              .collect(Collectors.toList());

        } catch (NoPathFoundException e) {
          return NavigateResult.FAIL_BLOCKED;
        }
        GroupedNode playerGroupNode = path.get(0);
        // first is virtual -> node on the edge closest to player
        GroupedNode first = path.get(1);
        // the first true node with groups
        GroupedNode second = path.get(2);

        second.groups().forEach(g -> {
          playerGroupNode.groups().add(g);
          first.groups().add(g);
        });

        NodeGroup highest = path.stream()
            .map(GroupedNode::groups)
            .flatMap(Collection::stream)
            .filter(g -> g.hasModifier(FindDistanceModifier.KEY))
            .max(NodeGroup::compareTo).orElse(null);

        double findDist = highest == null ? 1.5 : highest.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY)
            .map(FindDistanceModifier::distance).orElse(1.5);
        return setPath(player, path, path.get(path.size() - 1).node().getLocation(), (float) findDist);
      });
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
      Collection<NodeGroup> groups = StorageUtil.getGroups(c.node());

      if (player.unwrap() == null) {
        return false;
      }

      return groups.stream()
          .allMatch(g -> {
            Optional<PermissionModifier> mod = g.getModifier(PermissionModifier.KEY);
            return mod.isEmpty() || player.hasPermission(mod.get().permission());
          });
    });
  }
}
