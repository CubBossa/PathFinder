package de.bossascrew.pathfinder.module.visualizing;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.node.*;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.module.Module;
import de.bossascrew.pathfinder.module.visualizing.events.PathStartEvent;
import de.bossascrew.pathfinder.module.visualizing.events.PathTargetFoundEvent;
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
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

		RoadMap roadMap = navigables.getRoadMap();
		if (roadMap == null) {
			return;
		}

		// Prepare graph:
		// Every target node will be connected with a new introduced destination node.
		// All new edges have the same weight. The shortest path can only cross a target node.
		// Finally, take a sublist of the shortest path to exclude the destination.

		PlayerNode playerNode = new PlayerNode(player, roadMap);
		Graph<Node, Edge> graph = roadMap.toGraph(playerNode);

		EmptyNode destination = new EmptyNode(roadMap);
		graph.addVertex(destination);
		navigables.stream().flatMap(x -> x.getGroup().stream()).distinct().forEach(n -> {
			Edge e = new Edge(n, destination, 1);
			graph.addEdge(n, destination, e);
			graph.setEdgeWeight(e, 1);
		});

		GraphPath<Node, Edge> path = new DijkstraShortestPath<>(graph).getPath(playerNode, destination);

		if (path == null) {
			player.sendMessage(":C");
			return;
		}

		ParticlePath particlePath = new ParticlePath(roadMap, player.getUniqueId(), roadMap.getVisualizer());
		particlePath.addAll(path.getVertexList().subList(0, path.getVertexList().size() - 1));
		setPath(player.getUniqueId(), particlePath, path.getVertexList().get(path.getVertexList().size() - 2).getLocation(), (float) roadMap.getNodeFindDistance());
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
