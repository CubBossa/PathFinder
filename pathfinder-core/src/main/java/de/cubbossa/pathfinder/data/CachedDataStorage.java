package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.exlll.configlib.Configuration;
import org.bukkit.NamespacedKey;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CachedDataStorage implements DataStorage {

	private final DataStorage storage;
	private final Map<UUID, NodeType<?>> nodeTypeCache = new HashMap<>();
	private final Map<UUID, Node<?>> nodeCache = new HashMap<>();
	private final Map<NamespacedKey, NodeGroup> groupCache = new HashMap<>();
	private final Map<UUID, Map<UUID, Edge>> edgeToCache = new HashMap<>();
	private final Map<UUID, Map<UUID, Edge>> edgeFromCache = new HashMap<>();

	private final CacheConfiguration configuration;

	public CachedDataStorage(DataStorage storage) {
		this(storage, new CacheConfiguration());
	}

	public CachedDataStorage(DataStorage storage, CacheConfiguration configuration) {
		this.storage = storage;
		this.configuration = configuration;
	}


	@Override
	public CompletableFuture<Collection<Edge>> getConnections(UUID start) {
		if (configuration.cacheScope) {
			Map<UUID, Edge> map = edgeFromCache.get(start);
			if (map == null) {
				return storage.getConnections(start);
			}
			return CompletableFuture.completedFuture(map.values());
		}
		return storage.getConnections(start);
	}

	@Override
	public CompletableFuture<Collection<Edge>> getConnectionsTo(UUID end) {
		if (configuration.cacheScope) {
			Map<UUID, Edge> map = edgeToCache.get(end);
			if (map == null) {
				return storage.getConnections(end);
			}
			return CompletableFuture.completedFuture(map.values());
		}
		return storage.getConnections(end);
	}

	@Override
	public CompletableFuture<Collection<Edge>> getConnectionsTo(NodeSelection end) {
		return null;
	}

	@Override
	public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
		return storage.connectNodes(start, end, weight).thenApply(edge -> {
			edgeToCache.computeIfAbsent(start, uuid -> new HashMap<>()).put(end, edge);
			edgeFromCache.computeIfAbsent(end, uuid -> new HashMap<>()).put(start, edge);
			return edge;
		});
	}

	@Override
	public CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end) {
		return storage.connectNodes(start, end).thenApply(edges -> {
			for (Edge edge : edges) {
				edgeToCache.computeIfAbsent(edge.getStart(), uuid -> new HashMap<>()).put(edge.getEnd(), edge);
				edgeFromCache.computeIfAbsent(edge.getEnd(), uuid -> new HashMap<>()).put(edge.getStart(), edge);
			}
			return edges;
		});
	}

	@Override
	public CompletableFuture<Void> disconnectNodes(NodeSelection start) {
		start.forEach(e -> {
			edgeFromCache.computeIfAbsent(e, uuid -> new HashMap<>()).clear();
			edgeToCache.forEach((k, v) -> v.remove(e));
		});
		return returnFuture(storage.disconnectNodes(start));
	}

	@Override
	public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
		return null;
	}

	@Override
	public CompletableFuture<Collection<UUID>> getNodeGroupNodes(NamespacedKey group) {
		return null;
	}

	@Override
	public CompletableFuture<Void> clearNodeGroups(NodeSelection selection) {
		return null;
	}

	@Override
	public CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet() {
		return configuration.cacheScope
				? CompletableFuture.completedFuture(groupCache.keySet())
				: storage.getNodeGroupKeySet();
	}

	@Override
	public CompletableFuture<NodeGroup> getNodeGroup(NamespacedKey key) {
		NodeGroup group = groupCache.get(key);
		if (group == null) {
			return storage.getNodeGroup(key);
		}
		return CompletableFuture.completedFuture(group);
	}

	@Override
	public CompletableFuture<Collection<NodeGroup>> getNodeGroups() {
		return CompletableFuture.completedFuture(groupCache.values());
	}

	@Override
	public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> getNodeGroups(Class<M> modifier) {
		return null;
	}

	@Override
	public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
		return null;
	}

	@Override
	public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
		return storage.createNodeGroup(key)
				.thenApply(g -> {
					groupCache.put(g.getKey(), g);
					return g;
				});
	}

	@Override
	public CompletableFuture<Void> updateNodeGroup(NamespacedKey group, Consumer<NodeGroup> modifier) {
		NodeGroup present = groupCache.get(group);
		if (present == null) {
			return storage.updateNodeGroup(group, modifier);
		}
		modifier.accept(present);
		return returnFuture(storage.updateNodeGroup(group, modifier));
	}

	@Override
	public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
		groupCache.remove(key);
		return returnFuture(storage.deleteNodeGroup(key));
	}

	@Override
	public DiscoverInfo createDiscoverInfo(UUID player, NodeGroup discoverable, LocalDateTime foundDate) {
		return null;
	}

	@Override
	public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
		return null;
	}

	@Override
	public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {

	}

	@Override
	public <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(VisualizerType<T> type) {
		return null;
	}

	@Override
	public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {

	}

	@Override
	public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {

	}

	@Override
	public void connect(Runnable initial) throws IOException {
		if (configuration.cacheScope) {
			getNodeGroups();
			getNodes();
		}
		storage.connect(initial);
	}

	@Override
	public void disconnect() {
		nodeCache.clear();
		nodeTypeCache.clear();
		groupCache.clear();
		storage.disconnect();
	}

	@Override
	public CompletableFuture<NodeType<?>> getNodeType(UUID nodeId) {
		NodeType<?> type = nodeTypeCache.get(nodeId);
		if (type == null) {
			return storage.getNodeType(nodeId);
		}
		return CompletableFuture.completedFuture(type);
	}

	@Override
	public CompletableFuture<Void> setNodeType(UUID nodeId, NamespacedKey nodeType) {
		nodeTypeCache.remove(nodeId);
		storage.setNodeType(nodeId, nodeType);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public NodeTypeRegistry getNodeTypeRegistry() {
		return storage.getNodeTypeRegistry();
	}

	@Override
	public CompletableFuture<Collection<NamespacedKey>> getNodeGroups(UUID node) {
		return null;
	}

	@Override
	public CompletableFuture<Waypoint> createNodeInStorage(NodeType.NodeCreationContext context) {
		return storage.createNodeInStorage(context).thenApply(waypoint -> {
			nodeCache.put(waypoint.getNodeId(), waypoint);
			return waypoint;
		});
	}

	@Override
	public CompletableFuture<Waypoint> getNodeFromStorage(UUID id) {
		Node<?> node = nodeCache.get(id);
		if (node instanceof Waypoint waypoint) {
			return CompletableFuture.completedFuture(waypoint);
		}
		return storage.getNodeFromStorage(id);
	}

	@Override
	public CompletableFuture<Collection<Waypoint>> getNodesFromStorage() {
		return configuration.cacheScope
				? CompletableFuture.completedFuture(nodeCache.values().stream()
				.filter(n -> n instanceof Waypoint)
				.map(n -> (Waypoint) n)
				.collect(Collectors.toList()))
				: storage.getNodesFromStorage();
	}

	@Override
	public CompletableFuture<Collection<Waypoint>> getNodesFromStorage(NodeSelection ids) {
		return configuration.cacheScope
				? CompletableFuture.completedFuture(nodeCache.values().stream()
				.filter(n -> n instanceof Waypoint)
				.map(n -> (Waypoint) n)
				.filter(w -> ids.contains(w.getNodeId()))
				.collect(Collectors.toList()))
				: storage.getNodesFromStorage(ids);
	}

	@Override
	public CompletableFuture<Void> updateNodeInStorage(UUID nodeId, Consumer<Waypoint> nodeConsumer) {
		Waypoint present = (Waypoint) nodeCache.get(nodeId);
		if (present == null) {
			return storage.updateNodeInStorage(nodeId, nodeConsumer);
		}
		nodeConsumer.accept(present);
		return returnFuture(storage.updateNodeInStorage(nodeId, nodeConsumer));
	}

	@Override
	public CompletableFuture<Void> updateNodesInStorage(NodeSelection nodeIds, Consumer<Waypoint> nodeConsumer) {
		for (UUID nodeId : nodeIds) {
			Waypoint present = (Waypoint) nodeCache.get(nodeId);
			if (present == null) {
				continue;
			}
			nodeConsumer.accept(present);
		}
		return returnFuture(storage.updateNodesInStorage(nodeIds, nodeConsumer));
	}

	@Override
	public CompletableFuture<Void> deleteNodesFromStorage(NodeSelection nodes) {
		nodes.forEach(nodeCache::remove);
		return returnFuture(storage.deleteNodesFromStorage(nodes));
	}

	private CompletableFuture<Void> returnFuture(CompletableFuture<Void> future) {
		return returnFuture(future, null);
	}

	private <T> CompletableFuture<T> returnFuture(CompletableFuture<T> future, T data) {
		if (configuration.awaitPersistence) {
			return future;
		}
		return CompletableFuture.completedFuture(data);
	}

	@Configuration
	public static class CacheConfiguration {
		public boolean cacheScope = true;
		public boolean awaitPersistence = false;
	}
}
