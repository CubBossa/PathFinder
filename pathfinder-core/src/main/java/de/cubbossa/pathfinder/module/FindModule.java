package de.cubbossa.pathfinder.module;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.CancelPathCommand;
import de.cubbossa.pathfinder.command.FindCommand;
import de.cubbossa.pathfinder.command.FindLocationCommand;
import de.cubbossa.pathfinder.events.path.PathTargetFoundEvent;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.SimpleDijkstra;
import de.cubbossa.pathfinder.listener.NavigationListener;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.NavigableModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.visualizer.CommonVisualizerPath;
import de.cubbossa.translations.Message;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AutoService(PathFinderExtension.class)
public class FindModule implements Listener, PathFinderExtension {

  @Getter
  private static FindModule instance;

  @Getter
  private final NamespacedKey key = CommonPathFinder.pathfinder("navigation");
  private final Map<PathPlayer<Player>, SearchInfo> activePaths;
  private final PathFinder pathFinder;
  private final List<Predicate<NavigationRequestContext>> navigationFilter;
  private FindCommand findCommand;
  private FindLocationCommand findLocationCommand;
  private CancelPathCommand cancelPathCommand;
  private NavigationListener listener;

  public FindModule() {
    instance = this;
    this.pathFinder = PathFinderProvider.get();

    this.activePaths = new HashMap<>();
    this.navigationFilter = new ArrayList<>();
  }

  public static void printResult(FindModule.NavigateResult result, PathPlayer<Player> player) {
    Message message = switch (result) {
      case SUCCESS -> Messages.CMD_FIND;
      case FAIL_BLOCKED, FAIL_EVENT_CANCELLED -> Messages.CMD_FIND_BLOCKED;
      case FAIL_EMPTY -> Messages.CMD_FIND_EMPTY;
      case FAIL_TOO_FAR_AWAY -> Messages.CMD_FIND_TOO_FAR;
    };
    player.sendMessage(message);
  }

  @Override
  public void onLoad(PathFinder pathPlugin) {

    if (!pathFinder.getConfiguration().getModuleConfig().isNavigationModule()) {
      pathFinder.getExtensionRegistry().unregisterExtension(this);
    }
    findCommand = new FindCommand(pathPlugin);
    findLocationCommand = new FindLocationCommand(pathPlugin);
    cancelPathCommand = new CancelPathCommand(pathPlugin);

    if (pathPlugin instanceof BukkitPathFinder bpf) {
      // TODO
      bpf.getCommandRegistry().registerCommand(findCommand);
      bpf.getCommandRegistry().registerCommand(findLocationCommand);
      bpf.getCommandRegistry().registerCommand(cancelPathCommand);
    }
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {

    registerListener();

    registerFindPredicate(c -> {
      if (!(c.node() instanceof Groupable groupable)) {
        return true;
      }
      Player player = Bukkit.getPlayer(c.playerId());
      Collection<NodeGroup> groups = groupable.getGroups();

      return groups.stream()
          .allMatch(g -> {
            PermissionModifier mod = g.getModifier(PermissionModifier.class);
            return player == null || mod == null || player.hasPermission(mod.permission());
          })
          && groups.stream()
          .anyMatch(g -> g.hasModifier(NavigableModifier.class));
    });
  }

  @Override
  public void onDisable(PathFinder pathPlugin) {
    unregisterListener();
  }

  public void registerListener() {
    PathFinderPlugin plugin = PathFinderPlugin.getInstance();
    listener = new NavigationListener();
    Bukkit.getPluginManager().registerEvents(listener, plugin);
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public void unregisterListener() {
    PlayerMoveEvent.getHandlerList().unregister(listener);
  }

  public void registerFindPredicate(Predicate<NavigationRequestContext> filter) {
    navigationFilter.add(filter);
  }

  public List<Predicate<NavigationRequestContext>> getNavigationFilter() {
    return new ArrayList<>(navigationFilter);
  }

  public @Nullable SearchInfo getActivePath(PathPlayer<Player> player) {
    return activePaths.get(player);
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<Player> player, Location location) {
    return findPath(player, location, pathFinder.getConfiguration().getNavigation().getFindLocation().getMaxDistance());
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<Player> player, Location location,
                                                    double maxDist) {
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

  public CompletableFuture<NavigateResult> findPath(PathPlayer<Player> player, NodeSelection targets) {

    if (targets.size() == 0) {
      return CompletableFuture.completedFuture(NavigateResult.FAIL_EMPTY);
    }

    PlayerNode playerNode = new PlayerNode(player);

    return NodeHandler.getInstance().createGraph(playerNode).thenApply(graph -> {
      return pathFinder.getStorage().loadNodes().thenApply(nodes -> {

        PathSolver<Node> pathSolver = new SimpleDijkstra<>();
        List<Node> path;
        try {
          path = pathSolver.solvePath(graph, playerNode, targets.stream()
              .filter(nodes::contains)
              .collect(Collectors.toList()));
        } catch (NoPathFoundException e) {
          return NavigateResult.FAIL_BLOCKED;
        }

        Groupable first = (Groupable) path.get(1);
        first.getGroups().forEach(playerNode::addGroup);

        Groupable last = (Groupable) path.get(path.size() - 1);
        NodeGroup highest = last.getGroups().stream()
            .filter(g -> g.hasModifier(FindDistanceModifier.class))
            .max(NodeGroup::compareTo).orElse(null);

        double findDist = highest == null ? 1.5 : highest.getModifier(FindDistanceModifier.class).distance();

        NavigateResult result = setPath(player, path, path.get(path.size() - 1).getLocation(), (float) findDist);

        if (result == NavigateResult.SUCCESS) {
          // Refresh cancel-path command so that it is visible
          cancelPathCommand.refresh(player);
          // TODO event
        }
        return result;
      }).join();
    });
  }

  public void reachTarget(SearchInfo info) {
    unsetPath(info);
    PathTargetFoundEvent event = new PathTargetFoundEvent(info.player(), info.path());
    Bukkit.getPluginManager().callEvent(event);
    //TODO internal event
  }

  public NavigateResult setPath(PathPlayer<Player> player, @NotNull List<Node> pathNodes, Location target, float distance) {
    VisualizerPath<Player> visualizerPath = new CommonVisualizerPath<>();
    visualizerPath.prepare(pathNodes, player);

    boolean success = PathFinderProvider.get().getEventDispatcher().dispatchPathStart(player, visualizerPath, target, distance);
    if (!success) {
      return NavigateResult.FAIL_EVENT_CANCELLED;
    }

    SearchInfo current = activePaths.put(player, new SearchInfo(player, visualizerPath, target, distance));
    if (current != null) {
      current.path().cancel(player);
    }
    visualizerPath.run(player);
    return NavigateResult.SUCCESS;
  }

  public void unsetPath(PathPlayer<?> playerId) {
    if (activePaths.containsKey(playerId)) {
      unsetPath(activePaths.get(playerId));
    }
  }

  public void unsetPath(SearchInfo info) {
    activePaths.remove(info.player());
    info.path().cancel(info.player());

    Player player = info.player().unwrap();
    cancelPathCommand.refresh(info.player());
    // TODO internal path stopped event
  }

  public void cancelPath(PathPlayer<?> playerId) {
    if (activePaths.containsKey(playerId)) {
      cancelPath(activePaths.get(playerId));
    }
  }

  public void cancelPath(SearchInfo info) {
    unsetPath(info);
    Player player = info.player().unwrap();
    // TODO internal path cancelled event
  }

  public enum NavigateResult {
    SUCCESS, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED,
    FAIL_TOO_FAR_AWAY;
  }

  public record NavigationRequestContext(UUID playerId, Node node) {
  }

  public record SearchInfo(PathPlayer<Player> player, VisualizerPath<Player> path, Location target, float distance) {
  }
}
