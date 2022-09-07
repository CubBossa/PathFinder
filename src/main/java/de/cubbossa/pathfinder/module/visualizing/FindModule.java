package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.node.implementation.EmptyNode;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.events.PathStartEvent;
import de.cubbossa.pathfinder.module.visualizing.events.PathTargetFoundEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerDistanceChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerIntervalChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.TranslationHandler;
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
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FindModule implements Listener {

	public enum NavigateResult {
		SUCCESS, FAIL_NO_VISUALIZER_SELECTED, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED
	}

	public record NavigationRequestContext(UUID playerId, Navigable navigable) {
	}

	public record SearchInfo(UUID playerId, VisualizerPath path, Location target, float distance) {
	}

	@Getter
	private static FindModule instance;

	private final Map<UUID, SearchInfo> activePaths;

	private final PathPlugin plugin;
	private MoveListener listener;
	private final List<Predicate<NavigationRequestContext>> navigationFilter;

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
			return NodeGroupHandler.getInstance().isNavigable(groupable) && NodeGroupHandler.getInstance().hasPermission(player, groupable);
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

	public NavigateResult findPath(Player player, NodeSelection targets) {
		return findPath(player, targets, RoadMapHandler.getInstance().getRoadMaps().values());
	}

	public NavigateResult findPath(Player player, NodeSelection targets, Collection<RoadMap> scope) {

		// Prepare graph:
		// Every target node will be connected with a new introduced destination node.
		// All new edges have the same weight. The shortest path can only cross a target node.
		// Finally, take a sublist of the shortest path to exclude the destination.

		Set<NamespacedKey> scopeKeys = scope.stream().map(RoadMap::getKey).collect(Collectors.toSet());
		Collection<Node> nodes = targets.stream().filter(node -> scopeKeys.contains(node.getRoadMapKey())).collect(Collectors.toSet());

		List<RoadMap> roadMaps = nodes.stream()
				.map(Node::getRoadMapKey)
				.distinct()
				.map(key -> RoadMapHandler.getInstance().getRoadMap(key))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (nodes.size() == 0) {
			return NavigateResult.FAIL_EMPTY;
		}

		RoadMap firstRoadMap = roadMaps.get(0);
		PlayerNode playerNode = new PlayerNode(player, firstRoadMap);
		Graph<Node, Edge> graph = firstRoadMap.toGraph(playerNode);

		for (RoadMap roadMap : roadMaps.subList(1, roadMaps.size())) {
			Graphs.addGraph(graph, roadMap.toGraph(null));
		}

		EmptyNode destination = new EmptyNode(firstRoadMap, player.getWorld());
		graph.addVertex(destination);
		targets.forEach(n -> {
			Edge e = new Edge(n, destination, 1);
			graph.addEdge(n, destination, e);
			graph.setEdgeWeight(e, 1);
		});

		GraphPath<Node, Edge> path = new DijkstraShortestPath<>(graph).getPath(playerNode, destination);

		if (path == null) {
			return NavigateResult.FAIL_BLOCKED;
		}

		PathVisualizer<?, ?> vis = firstRoadMap.getVisualizer();
		VisualizerPath<?> visualizerPath = new VisualizerPath<>(player.getUniqueId(), vis);
		visualizerPath.addAll(path.getVertexList().subList(0, path.getVertexList().size() - 1));
		NavigateResult result = setPath(player.getUniqueId(), visualizerPath, path.getVertexList().get(path.getVertexList().size() - 2).getLocation(),
				NodeGroupHandler.getInstance().getFindDistance((Groupable) visualizerPath.get(visualizerPath.size() - 1)));

		if (result == NavigateResult.SUCCESS) {
			// Refresh cancel-path command so that it is visible
			PathPlugin.getInstance().getCancelPathCommand().refresh(player);
		}
		return result;
	}

	public void reachTarget(SearchInfo info) {
		cancelPath(info);
		PathTargetFoundEvent event = new PathTargetFoundEvent(info.playerId(), info.path());
		Bukkit.getPluginManager().callEvent(event);

		TranslationHandler.getInstance().sendMessage(Messages.TARGET_FOUND, Bukkit.getPlayer(info.playerId()));
	}

	public NavigateResult setPath(UUID playerId, @NotNull VisualizerPath<?> path, Location target, float distance) {
		if (path.getVisualizer() == null) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_NO_VIS, Bukkit.getPlayer(playerId));
			return NavigateResult.FAIL_NO_VISUALIZER_SELECTED;
		}
		PathStartEvent event = new PathStartEvent(playerId, path, target, distance);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return NavigateResult.FAIL_EVENT_CANCELLED;
		}

		SearchInfo current = activePaths.put(playerId, new SearchInfo(playerId, path, target, distance));
		if (current != null) {
			current.path().cancel();
		}
		path.run(playerId);
		return NavigateResult.SUCCESS;
	}

	public void cancelPath(UUID playerId) {
		if (activePaths.containsKey(playerId)) {
			cancelPath(activePaths.get(playerId));
		}
	}

	public void cancelPath(SearchInfo info) {
		activePaths.remove(info.playerId());
		info.path().cancel();

		PathPlugin.getInstance().getCancelPathCommand().refresh(Bukkit.getPlayer(info.playerId()));
	}

	@EventHandler
	public void onDistanceChange(VisualizerDistanceChangedEvent event) {
		activePaths.forEach((uuid, info) -> {
			if (info.path().getVisualizer().equals(event.getVisualizer())) {
				info.path().run();
			}
		});
	}

	@EventHandler
	public void onDistanceChange(VisualizerIntervalChangedEvent event) {
		activePaths.forEach((uuid, info) -> {
			if (info.path().getVisualizer().equals(event.getVisualizer())) {
				info.path().run();
			}
		});
	}
}
