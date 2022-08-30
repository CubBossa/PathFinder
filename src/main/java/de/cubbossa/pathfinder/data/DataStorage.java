package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jgrapht.alg.util.Triple;
import xyz.xenondevs.particle.ParticleBuilder;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;

public interface DataStorage {

	void connect();

	void disconnect();

	default RoadMap createRoadMap(NamespacedKey key, String nameFormat, PathVisualizer<?> pathVis) {
		return createRoadMap(key, nameFormat, pathVis, 1);
	}

	RoadMap createRoadMap(NamespacedKey key, String nameFormat, PathVisualizer<?> pathVis, double tangentLength);

	Map<NamespacedKey, RoadMap> loadRoadMaps();

	void updateRoadMap(RoadMap roadMap);

	default boolean deleteRoadMap(RoadMap roadMap) {
		return deleteRoadMap(roadMap.getKey());
	}

	boolean deleteRoadMap(NamespacedKey key);


	Edge createEdge(Node start, Node end, float weight);

	List<Edge> createEdges(List<Triple<Node, Node, Float>> edges);

	Collection<Edge> loadEdges(RoadMap roadMap);

	void deleteEdgesFrom(Node start);

	void deleteEdgesTo(Node end);

	void deleteEdges(Collection<Edge> edges);

	default void deleteEdge(Edge edge) {
		deleteEdge(edge.getStart().getNodeId(), edge.getEnd().getNodeId());
	}

	default void deleteEdge(Node start, Node end) {
		deleteEdge(start.getNodeId(), end.getNodeId());
	}

	void deleteEdge(int startId, int endId);

	<T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, Location location, Double tangentLength);

	Map<Integer, Node> loadNodes(RoadMap roadMap);

	void updateNode(Node node);

	default void deleteNodes(Integer... nodeId) {
		deleteNodes(Arrays.asList(nodeId));
	}

	void deleteNodes(Collection<Integer> nodeIds);


	void assignNodesToGroup(NodeGroup group, NodeSelection selection);

	void removeNodesFromGroup(NodeGroup group, NodeSelection selection);

	Map<NamespacedKey, List<Integer>> loadNodeGroupNodes();

	NodeGroup createNodeGroup(NamespacedKey key, String nameFormat, @Nullable String permission, boolean navigable, boolean discoverable, double findDistance);

	HashedRegistry<NodeGroup> loadNodeGroups();

	void updateNodeGroup(NodeGroup group);

	default void deleteNodeGroup(NodeGroup group) {
		deleteNodeGroup(group.getKey());
	}

	void deleteNodeGroup(NamespacedKey key);

	Map<NamespacedKey, Collection<String>> loadSearchTerms();

	void addSearchTerms(NodeGroup group, Collection<String> searchTerms);

	void removeSearchTerms(NodeGroup group, Collection<String> searchTerms);

	DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, Date foundDate);

	Map<UUID, Map<NamespacedKey, DiscoverInfo>> loadDiscoverInfo();

	void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey);

	PathVisualizer<?> newPathVisualizer(NamespacedKey key, String nameFormat, ParticleBuilder particle, ItemStack displayIcon, double particleDistance, int particleSteps, int schedulerPeriod, double curveLength);

	Map<Integer, PathVisualizer> loadPathVisualizer();

	void updatePathVisualizer(PathVisualizer<?> visualizer);

	void deletePathVisualizer(PathVisualizer<?> visualizer);

	Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers();

	void createPlayerVisualizer(int playerId, RoadMap roadMap, ParticleVisualizer visualizer);

	void updatePlayerVisualizer(int playerId, RoadMap roadMap, ParticleVisualizer visualizer);

	void loadVisualizerStyles(Collection<ParticleVisualizer> visualizers);

	void newVisualizerStyle(ParticleVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName);

	void updateVisualizerStyle(ParticleVisualizer visualizer);

	void deleteStyleVisualizer(int visualizerId);

	Map<Integer, Collection<ParticleVisualizer>> loadStyleRoadmapMap(Collection<ParticleVisualizer> visualizers);

	void addStyleToRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer);

	void removeStyleFromRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer);

	NodeBatchCreator newNodeBatch();

	interface NodeBatchCreator {
		<T extends Node> void createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, Location location, Double tangentLength) throws SQLException;

		Collection<? extends Node> commit() throws SQLException;
	}
}
