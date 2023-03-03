package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.graph.Graph;
import de.cubbossa.pathfinder.core.graph.SimpleDijkstra;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Navigable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.events.PathStartEvent;
import de.cubbossa.pathfinder.module.visualizing.events.PathTargetFoundEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerPropertyChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.TranslationHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

public class FindModule implements Listener {

  @Getter
  private static FindModule instance;
  private final Map<UUID, SearchInfo> activePaths;
  private final PathPlugin plugin;
  private final List<Predicate<NavigationRequestContext>> navigationFilter;
  private MoveListener listener;

  public FindModule(PathPlugin plugin) {
    instance = this;

    this.plugin = plugin;
    this.activePaths = new HashMap<>();
    this.navigationFilter = new ArrayList<>();

    registerFindPredicate(navigationRequestContext -> {
      if (!(navigationRequestContext.navigable() instanceof Groupable groupable)) {
        return true;
      }
      Player player = Bukkit.getPlayer(navigationRequestContext.playerId());
      return NodeGroupHandler.getInstance().isNavigable(groupable) && NodeGroupHandler.getInstance()
          .hasPermission(player, groupable);
    });

    registerListener();
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

  public NavigateResult findPath(Player player, Location location, double maxDist) {
    Collection<Node> nodes = RoadMapHandler.getInstance().getRoadMapsStream()
        .map(RoadMap::getNodes)
        .flatMap(Collection::stream)
        .toList();

    Node closest = null;
    double dist = Double.MAX_VALUE;
    for (Node node : nodes) {
      double curDist = node.getLocation().distance(location);
      if (curDist < dist && curDist < maxDist) {
        closest = node;
        dist = curDist;
      }
    }
    if (closest == null) {
      return NavigateResult.FAIL_TOO_FAR_AWAY;
    }
    RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(closest.getRoadMapKey());
    Waypoint target = roadMap.createNode(RoadMapHandler.WAYPOINT_TYPE, location, false);
    roadMap.connectNodes(closest, target, false);

    NavigateResult result = findPath(player, new NodeSelection(target));

    roadMap.disconnectNodes(closest, target);
    roadMap.removeNodes(target);

    return result;
  }

  public NavigateResult findPath(Player player, NodeSelection targets) {
    return findPath(player, targets, RoadMapHandler.getInstance().getRoadMaps().values());
  }

  public NavigateResult findPath(Player player, NodeSelection targets, Collection<RoadMap> scope) {

    Set<NamespacedKey> scopeKeys = scope.stream().map(RoadMap::getKey).collect(Collectors.toSet());
    Collection<Node> nodes =
        targets.stream().filter(node -> scopeKeys.contains(node.getRoadMapKey()))
            .collect(Collectors.toSet());

    List<RoadMap> roadMaps = nodes.stream()
        .map(Node::getRoadMapKey)
        .distinct()
        .map(key -> RoadMapHandler.getInstance().getRoadMap(key))
        .filter(Objects::nonNull).toList();

    if (nodes.size() == 0) {
      return NavigateResult.FAIL_EMPTY;
    }

    RoadMap firstRoadMap = roadMaps.get(0);
    PlayerNode playerNode = new PlayerNode(player, firstRoadMap);
    Graph<Node> graph = firstRoadMap.toGraph(player, playerNode);

    for (RoadMap roadMap : roadMaps.subList(1, roadMaps.size())) {
      graph.merge(roadMap.toGraph(player, null));
    }

    SimpleDijkstra<Node> dijkstra = new SimpleDijkstra<>(graph);
    dijkstra.setStartNode(playerNode);
    List<Node> path = dijkstra.shortestPathToAny(targets);

    if (path == null) {
      return NavigateResult.FAIL_BLOCKED;
    }

    PathVisualizer<?, ?> vis = firstRoadMap.getVisualizer();
    VisualizerPath<?> visualizerPath = new VisualizerPath<>(player.getUniqueId(), vis);
    visualizerPath.addAll(path);
    NavigateResult result =
        setPath(player.getUniqueId(), visualizerPath, path.get(path.size() - 1).getLocation(),
            NodeGroupHandler.getInstance()
                .getFindDistance((Groupable) visualizerPath.get(visualizerPath.size() - 1)));

    if (result == NavigateResult.SUCCESS) {
      // Refresh cancel-path command so that it is visible
      PathPlugin.getInstance().getCancelPathCommand().refresh(player);
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
    PathPlugin.getInstance().getCancelPathCommand().refresh(player);
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
      case SUCCESS ->
          TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND, player);
      case FAIL_BLOCKED ->
          TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_BLOCKED, player);
      case FAIL_EMPTY ->
          TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_EMPTY, player);
    }
  }

  public enum NavigateResult {
    SUCCESS, FAIL_NO_VISUALIZER_SELECTED, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED, FAIL_TOO_FAR_AWAY;
  }

  public record NavigationRequestContext(UUID playerId, Navigable navigable) {
  }

  public record SearchInfo(UUID playerId, VisualizerPath path, Location target, float distance) {
  }
}
