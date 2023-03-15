package de.cubbossa.pathfinder.module.visualizing;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PathPluginExtension;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroupHandler;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.SimpleDijkstra;
import de.cubbossa.pathfinder.module.visualizing.command.CancelPathCommand;
import de.cubbossa.pathfinder.module.visualizing.command.FindCommand;
import de.cubbossa.pathfinder.module.visualizing.command.FindLocationCommand;
import de.cubbossa.pathfinder.module.visualizing.events.PathStartEvent;
import de.cubbossa.pathfinder.module.visualizing.events.PathTargetFoundEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerPropertyChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoService(PathPluginExtension.class)
public class FindModule implements Listener, PathPluginExtension {

  private List<CommandTree> commands;
  private FindCommand findCommand;
  private FindLocationCommand findLocationCommand;
  private CancelPathCommand cancelPathCommand;

  @Getter
  private static FindModule instance;

  @Getter
  private final NamespacedKey key = new NamespacedKey(PathPlugin.getInstance(), "navigation");

  private final Map<UUID, SearchInfo> activePaths;
  private final PathPlugin plugin;
  private final List<Predicate<NavigationRequestContext>> navigationFilter;
  private MoveListener listener;

  public FindModule() {
    instance = this;
    this.plugin = PathPlugin.getInstance();

    this.activePaths = new HashMap<>();
    this.navigationFilter = new ArrayList<>();


  }

  @Override
  public void onLoad() {
    if (!plugin.getConfiguration().moduleConfig.navigationModule) {
      PathPlugin.getInstance().unregisterExtension(this);
    }
  }

  @Override
  public void onEnable() {
    findCommand = new FindCommand();
    findLocationCommand = new FindLocationCommand();
    cancelPathCommand = new CancelPathCommand();

    commands = List.of(
        findCommand,
        findLocationCommand,
        cancelPathCommand
    );
    commands.forEach(CommandTree::register);

    registerListener();

    registerFindPredicate(navigationRequestContext -> {
      if (!(navigationRequestContext.node() instanceof Groupable<?> groupable)) {
        return true;
      }
      Player player = Bukkit.getPlayer(navigationRequestContext.playerId());
      return NodeGroupHandler.getInstance().isNavigable(groupable)
          && NodeGroupHandler.getInstance().hasPermission(player, groupable);
    });
  }

  @Override
  public void onDisable() {
    commands.forEach(CommandUtils::unregister);
    unregisterListener();
  }

  public void registerListener() {
    listener = new MoveListener();
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

  public @Nullable SearchInfo getActivePath(Player player) {
    return activePaths.get(player.getUniqueId());
  }

  public NavigateResult findPath(Player player, Location location) {
    double maxDist = PathPlugin.getInstance().getConfiguration().navigation.findLocation.maxDistance;
    return findPath(player, location, maxDist);
  }

  public NavigateResult findPath(Player player, Location location, double maxDist) {
    if (maxDist < 0) {
      maxDist = Double.MAX_VALUE;
    }

    // check if x y and z are equals. Cannot cast raycast to self, therefore if statement required
    if (location.toVector().equals(player.getLocation().toVector())) {
      location = location.add(0, 0.01, 0);
    }

    Collection<Node<?>> nodes = NodeHandler.getInstance().getNodes();

    Node<?> closest = null;
    double dist = Double.MAX_VALUE;
    for (Node<?> node : nodes) {
      double curDist = node.getLocation().distance(location);
      if (curDist < dist && curDist < maxDist) {
        closest = node;
        dist = curDist;
      }
    }
    if (closest == null) {
      return NavigateResult.FAIL_TOO_FAR_AWAY;
    }
    Waypoint target = NodeHandler.getInstance().createNode(NodeHandler.WAYPOINT_TYPE, location, false);
    NodeHandler.getInstance().connectNodes(closest, target, false);

    NavigateResult result = findPath(player, new NodeSelection(target));

    NodeHandler.getInstance().disconnectNodes(closest, target);
    NodeHandler.getInstance().removeNodes(target);

    return result;
  }

  public NavigateResult findPath(Player player, NodeSelection targets) {

    Collection<Node<?>> nodes = NodeHandler.getInstance().getNodes();
    if (nodes.size() == 0) {
      return NavigateResult.FAIL_EMPTY;
    }

    PlayerNode playerNode = new PlayerNode(player);
    Graph<Node<?>> graph = NodeHandler.getInstance().toGraph(player, playerNode);

    NodeHandler.getInstance().toGraph(player, playerNode);

    PathSolver<Node<?>> pathSolver = new SimpleDijkstra<>();
    List<Node<?>> path;
    try {
      path = pathSolver.solvePath(graph, playerNode, targets);
    } catch (NoPathFoundException e) {
      return NavigateResult.FAIL_BLOCKED;
    }

    // TODO we don't need 1 but one fore each segment where visualizers change
    PathVisualizer<?, ?> vis = firstRoadMap.getVisualizer();
    VisualizerPath<?> visualizerPath = new VisualizerPath<>(player.getUniqueId(), vis);
    visualizerPath.addAll(path);
    NavigateResult result =
        setPath(player.getUniqueId(), visualizerPath, path.get(path.size() - 1).getLocation(),
            NodeGroupHandler.getInstance()
                .getFindDistance((Groupable<?>) visualizerPath.get(visualizerPath.size() - 1)));

    if (result == NavigateResult.SUCCESS) {
      // Refresh cancel-path command so that it is visible
      cancelPathCommand.refresh(player);
      EffectHandler.getInstance()
          .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_started", player,
              player.getLocation());
    }
    return result;
  }

  public void reachTarget(SearchInfo info) {
    unsetPath(info);
    PathTargetFoundEvent event = new PathTargetFoundEvent(info.playerId(), info.path());
    Bukkit.getPluginManager().callEvent(event);

    Player player = Bukkit.getPlayer(info.playerId());
    EffectHandler.getInstance()
        .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_finished", player,
            player.getLocation());
  }

  public NavigateResult setPath(UUID playerId, @NotNull VisualizerPath<?> path, Location target,
                                float distance) {
    if (path.getVisualizer() == null) {
      TranslationHandler.getInstance()
          .sendMessage(Messages.CMD_FIND_NO_VIS, Bukkit.getPlayer(playerId));
      return NavigateResult.FAIL_NO_VISUALIZER_SELECTED;
    }
    PathStartEvent event = new PathStartEvent(playerId, path, target, distance);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return NavigateResult.FAIL_EVENT_CANCELLED;
    }

    SearchInfo current =
        activePaths.put(playerId, new SearchInfo(playerId, path, target, distance));
    if (current != null) {
      current.path().cancel();
    }
    path.run(playerId);
    return NavigateResult.SUCCESS;
  }

  public void unsetPath(UUID playerId) {
    if (activePaths.containsKey(playerId)) {
      unsetPath(activePaths.get(playerId));
    }
  }

  public void unsetPath(SearchInfo info) {
    activePaths.remove(info.playerId());
    info.path().cancel();

    Player player = Bukkit.getPlayer(info.playerId());
    cancelPathCommand.refresh(player);
    EffectHandler.getInstance()
        .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_stopped", player,
            player.getLocation());
  }

  public void cancelPath(UUID playerId) {
    if (activePaths.containsKey(playerId)) {
      cancelPath(activePaths.get(playerId));
    }
  }

  public void cancelPath(SearchInfo info) {
    unsetPath(info);
    Player player = Bukkit.getPlayer(info.playerId());
    EffectHandler.getInstance()
        .playEffect(PathPlugin.getInstance().getEffectsFile(), "path_cancelled", player,
            player.getLocation());
  }

  @EventHandler
  public <T> void onVisualizerChanged(VisualizerPropertyChangedEvent<T> event) {
    if (!event.isVisual()) {
      return;
    }
    activePaths.forEach((uuid, info) -> {
      if (info.path().getVisualizer().equals(event.getVisualizer())) {
        info.path().run();
      }
    });
  }

  public static void printResult(FindModule.NavigateResult result, Player player) {
    switch (result) {
      case SUCCESS -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND, player);
      case FAIL_BLOCKED ->
          TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_BLOCKED, player);
      case FAIL_EMPTY ->
          TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_EMPTY, player);
      case FAIL_TOO_FAR_AWAY ->
          TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_TOO_FAR, player);
    }
  }

  public enum NavigateResult {
    SUCCESS, FAIL_NO_VISUALIZER_SELECTED, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED,
    FAIL_TOO_FAR_AWAY;
  }

  public record NavigationRequestContext(UUID playerId, Node<?> node) {
  }

  public record SearchInfo(UUID playerId, VisualizerPath path, Location target, float distance) {
  }
}
