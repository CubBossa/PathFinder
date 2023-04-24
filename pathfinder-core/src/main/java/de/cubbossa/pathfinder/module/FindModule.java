package de.cubbossa.pathfinder.module;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.commands.CancelPathCommand;
import de.cubbossa.pathfinder.commands.FindCommand;
import de.cubbossa.pathfinder.commands.FindLocationCommand;
import de.cubbossa.pathfinder.events.visualizer.PathStartEvent;
import de.cubbossa.pathfinder.events.visualizer.PathTargetFoundEvent;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.SimpleDijkstra;
import de.cubbossa.pathfinder.listener.NavigationListener;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.NavigableModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.VectorUtils;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.TranslationHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoService(PathFinderExtension.class)
public class FindModule implements Listener, PathFinderExtension {

  @Getter
  private static FindModule instance;

  @Getter
  private final NamespacedKey key = PathPlugin.pathfinder("navigation");
  private final Map<PathPlayer<Player>, SearchInfo> activePaths;
  private final PathPlugin plugin;
  private final List<Predicate<NavigationRequestContext>> navigationFilter;
  private FindCommand findCommand;
  private FindLocationCommand findLocationCommand;
  private CancelPathCommand cancelPathCommand;
  private NavigationListener listener;

  public FindModule() {
    instance = this;
    this.plugin = PathPlugin.getInstance();

    this.activePaths = new HashMap<>();
    this.navigationFilter = new ArrayList<>();
  }

  public static void printResult(FindModule.NavigateResult result, Player player) {
    switch (result) {
      case SUCCESS -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND, player);
      case FAIL_BLOCKED -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_BLOCKED, player);
      case FAIL_EMPTY -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_EMPTY, player);
      case FAIL_TOO_FAR_AWAY -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_TOO_FAR, player);
    }
  }

  @Override
  public void onLoad(PathFinder pathPlugin) {

    if (!plugin.getConfiguration().moduleConfig.navigationModule) {
      plugin.getExtensionRegistry().unregisterExtension(this);
    }
    findCommand = new FindCommand(pathPlugin);
    findLocationCommand = new FindLocationCommand(pathPlugin);
    cancelPathCommand = new CancelPathCommand(pathPlugin);

    plugin.getCommandRegistry().registerCommand(findCommand);
    plugin.getCommandRegistry().registerCommand(findLocationCommand);
    plugin.getCommandRegistry().registerCommand(cancelPathCommand);
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {

    registerListener();

    registerFindPredicate(c -> {
      if (!(c.node() instanceof Groupable<?> groupable)) {
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
    double maxDist =
        PathPlugin.getInstance().getConfiguration().navigation.findLocation.maxDistance;
    return findPath(player, location, maxDist);
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<Player> player, Location location,
                                                    double maxDist) {
    return plugin.getStorage().loadNodes().thenApply(nodes -> {
      double _maxDist = maxDist < 0 ? Double.MAX_VALUE : maxDist;
      // check if x y and z are equals. Cannot cast raycast to self, therefore if statement required
      Location _location = location.equals(player.getLocation())
          ? location.add(0, 0.01, 0) : location;

      Node<?> closest = null;
      double dist = Double.MAX_VALUE;
      for (Node<?> node : nodes) {
        double curDist = node.getLocation().distance(_location);
        if (curDist < dist && curDist < _maxDist) {
          closest = node;
          dist = curDist;
        }
      }
      if (closest == null) {
        return NavigateResult.FAIL_TOO_FAR_AWAY;
      }
      final Node<?> fClosest = closest;
      Waypoint waypoint =
          new Waypoint(plugin.getNodeTypeRegistry().getWaypointNodeType(), UUID.randomUUID());
      waypoint.setLocation(_location);
      // we can savely add edges because the fClosest object is only a representation of the stored node.
      fClosest.getEdges().add(new SimpleEdge(fClosest, waypoint, 1));

      return findPath(player, new NodeSelection(waypoint)).join();
    });
  }

  public CompletableFuture<NavigateResult> findPath(PathPlayer<Player> player,
                                                    NodeSelection targets) {

    if (targets.size() == 0) {
      return CompletableFuture.completedFuture(NavigateResult.FAIL_EMPTY);
    }

    PlayerNode playerNode = new PlayerNode(player);

    return NodeHandler.getInstance().createGraph(playerNode).thenApply(graph -> {
      return plugin.getStorage().loadNodes().thenApply(nodes -> {

        PathSolver<Node<?>> pathSolver = new SimpleDijkstra<>();
        List<Node<?>> path;
        try {
          // TODO whats going on
          path = pathSolver.solvePath(graph, playerNode, targets.stream()
              .map(node -> nodes.stream().filter(n -> n.equals(node)).findAny().get())
              .map(node -> (Node<?>) node)
              .collect(Collectors.toList()));
        } catch (NoPathFoundException e) {
          return NavigateResult.FAIL_BLOCKED;
        }

        VisualizerPath<Player> visualizerPath = new VisualizerPath<>(player);
        visualizerPath.addAll(path);

        Groupable<?> last = (Groupable<?>) visualizerPath.get(visualizerPath.size() - 1);
        NodeGroup highest = last.getGroups().stream()
            .filter(g -> g.hasModifier(FindDistanceModifier.class))
            .max(NodeGroup::compareTo).orElse(null);

        double findDist =
            highest == null ? 1.5 : highest.getModifier(FindDistanceModifier.class).distance();

        NavigateResult result =
            setPath(player, visualizerPath, path.get(path.size() - 1).getLocation(),
                (float) findDist);

        if (result == NavigateResult.SUCCESS) {
          // Refresh cancel-path command so that it is visible
          cancelPathCommand.refresh(player);
          EffectHandler.getInstance()
              .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_started",
                  player.unwrap(),
                  VectorUtils.toBukkit(player.getLocation()));
        }
        return result;
      }).join();
    });
  }

  public void reachTarget(SearchInfo info) {
    unsetPath(info);
    PathTargetFoundEvent event = new PathTargetFoundEvent(info.player(), info.path());
    Bukkit.getPluginManager().callEvent(event);

    Player player = info.player().unwrap();
    EffectHandler.getInstance()
        .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_finished", player,
            player.getLocation());
  }

  public NavigateResult setPath(PathPlayer<Player> player, @NotNull VisualizerPath<Player> path,
                                Location target,
                                float distance) {
    PathStartEvent event = new PathStartEvent(player, path, target, distance);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return NavigateResult.FAIL_EVENT_CANCELLED;
    }

    SearchInfo current =
        activePaths.put(player, new SearchInfo(player, path, target, distance));
    if (current != null) {
      current.path().cancel();
    }
    path.run(player);
    return NavigateResult.SUCCESS;
  }

  public void unsetPath(PathPlayer<?> playerId) {
    if (activePaths.containsKey(playerId)) {
      unsetPath(activePaths.get(playerId));
    }
  }

  public void unsetPath(SearchInfo info) {
    activePaths.remove(info.player());
    info.path().cancel();

    Player player = info.player().unwrap();
    cancelPathCommand.refresh(info.player());
    EffectHandler.getInstance()
        .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_stopped", player,
            player.getLocation());
  }

  public void cancelPath(PathPlayer<?> playerId) {
    if (activePaths.containsKey(playerId)) {
      cancelPath(activePaths.get(playerId));
    }
  }

  public void cancelPath(SearchInfo info) {
    unsetPath(info);
    Player player = info.player().unwrap();
    EffectHandler.getInstance()
        .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_cancelled", player,
            player.getLocation());
  }

  public enum NavigateResult {
    SUCCESS, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED,
    FAIL_TOO_FAR_AWAY;
  }

  public record NavigationRequestContext(UUID playerId, Node<?> node) {
  }

  public record SearchInfo(PathPlayer<Player> player, VisualizerPath<?> path, Location target,
                           float distance) {
  }
}
