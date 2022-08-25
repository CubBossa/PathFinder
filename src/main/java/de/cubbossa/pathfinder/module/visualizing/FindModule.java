package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.NavigateSelection;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.implementation.EmptyNode;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.Module;
import de.cubbossa.pathfinder.module.visualizing.events.PathStartEvent;
import de.cubbossa.pathfinder.module.visualizing.events.PathTargetFoundEvent;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.*;
import java.util.stream.Collectors;

public class FindModule extends Module {

	public record SearchInfo(UUID playerId, ParticlePath path, Location target, float distance) {
	}

	@Getter
	private static FindModule instance;

	private final Map<UUID, Map<NamespacedKey, SearchInfo>> activePaths;

	private final PathPlugin plugin;
	private MoveListener listener;

	public FindModule(PathPlugin plugin) {
		instance = this;

		this.plugin = plugin;
		this.plugin.registerModule(this);
		this.activePaths = new HashMap<>();
	}

	@Override
	public void onEnable() {

		registerListener();
	}

	public void registerListener() {
		listener = new MoveListener();
		Bukkit.getPluginManager().registerEvents(listener, plugin);
	}

	public void unregisterListener() {
		PlayerMoveEvent.getHandlerList().unregister(listener);
	}

	public Map<NamespacedKey, SearchInfo> getActivePaths(Player player) {
		return activePaths.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>());
	}

	public void findPath(Player player, NavigateSelection navigables) {
		findPath(player, navigables, RoadMapHandler.getInstance().getRoadMaps().values());
	}

	public void findPath(Player player, NavigateSelection navigables, Collection<RoadMap> scope) {

		// Prepare graph:
		// Every target node will be connected with a new introduced destination node.
		// All new edges have the same weight. The shortest path can only cross a target node.
		// Finally, take a sublist of the shortest path to exclude the destination.

		Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {

			Set<NamespacedKey> scopeKeys = scope.stream().map(RoadMap::getKey).collect(Collectors.toSet());
			Collection<Node> nodes = navigables.stream()
					.flatMap(x -> x.getGroup().stream())
					.filter(node -> scopeKeys.contains(node.getRoadMapKey()))
					.collect(Collectors.toSet());

			List<RoadMap> roadMaps = nodes.stream()
					.map(Node::getRoadMapKey)
					.distinct()
					.map(key -> RoadMapHandler.getInstance().getRoadMap(key))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			if (nodes.size() == 0) {
				return; //TODO
			}

			RoadMap firstRoadMap = roadMaps.get(0);
			PlayerNode playerNode = new PlayerNode(player, firstRoadMap);
			Graph<Node, Edge> graph = firstRoadMap.toGraph(playerNode);

			for (RoadMap roadMap : roadMaps.subList(1, roadMaps.size())) {
				Graphs.addGraph(graph, roadMap.toGraph(null));
			}

			EmptyNode destination = new EmptyNode(firstRoadMap);
			graph.addVertex(destination);
			navigables.stream()
					.flatMap(x -> x.getGroup().stream())
					.distinct()
					.forEach(n -> {
						Edge e = new Edge(n, destination, 1);
						graph.addEdge(n, destination, e);
						graph.setEdgeWeight(e, 1);
					});

			GraphPath<Node, Edge> path = new DijkstraShortestPath<>(graph).getPath(playerNode, destination);

			if (path == null) {
				player.sendMessage(":C");
				return;
			}

			//TODO not first roadmap
			ParticlePath particlePath = new ParticlePath(firstRoadMap, player.getUniqueId(), firstRoadMap.getVisualizer());
			particlePath.addAll(path.getVertexList().subList(0, path.getVertexList().size() - 1));
			setPath(player.getUniqueId(), particlePath, path.getVertexList().get(path.getVertexList().size() - 2).getLocation(), (float) firstRoadMap.getNodeFindDistance());
		});
	}

	public void reachTarget(SearchInfo info) {
		cancelPath(info);
		PathTargetFoundEvent event = new PathTargetFoundEvent(info.playerId(), info.path());
		Bukkit.getPluginManager().callEvent(event);

		TranslationHandler.getInstance().sendMessage(Messages.TARGET_FOUND, Bukkit.getPlayer(info.playerId()));
	}

	public void setPath(UUID playerId, @NotNull ParticlePath path, Location target, float distance) {
		PathStartEvent event = new PathStartEvent(playerId, path, target, distance);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		var map = activePaths.computeIfAbsent(playerId, uuid -> new HashMap<>());
		SearchInfo current = map.put(path.getRoadMap().getKey(), new SearchInfo(playerId, path, target, distance));
		if (current != null) {
			current.path().cancel();
		}
		path.run(playerId);
	}

	public void cancelPaths(UUID playerId) {
		Map<NamespacedKey, SearchInfo> map = activePaths.computeIfAbsent(playerId, uuid -> new HashMap<>());
		map.forEach((key, searchInfo) -> searchInfo.path().cancel());
		map.clear();
	}

	public void cancelPath(UUID playerId, RoadMap roadMap) {
		SearchInfo info = activePaths.computeIfAbsent(playerId, uuid -> new HashMap<>()).remove(roadMap.getKey());
		if (info != null) {
			info.path().cancel();
		}
	}

	public void cancelPath(SearchInfo info) {
		activePaths.computeIfAbsent(info.playerId, uuid -> new HashMap<>()).remove(info.path().getRoadMap().getKey());
		info.path().cancel();
	}
}
