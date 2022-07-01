package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.visualizer.PathVisualizer;
import de.bossascrew.pathfinder.visualizer.SimpleCurveVisualizer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleBuilder;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public interface DataStorage {

	/*TODO before publishing
	- Should groups have permissions?
	- Should roadmaps have a custom tangent length?
	 */

	void connect();

	void disconnect();

	default RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, PathVisualizer pathVis) {
		return createRoadMap(key, nameFormat, world, findableNodes, pathVis, 3, 3);
	}

	RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, PathVisualizer pathVis, double findDist, double tangentLength);

	Map<NamespacedKey, RoadMap> loadRoadMaps();

	void updateRoadMap(RoadMap roadMap);

	default boolean deleteRoadMap(RoadMap roadMap) {
		return deleteRoadMap(roadMap.getKey());
	}

	boolean deleteRoadMap(NamespacedKey key);


	Edge createEdge(Node start, Node end, float weight);

	Collection<Edge> loadEdges(RoadMap roadMap);

	void deleteEdgesFrom(Node start);

	void deleteEdgesTo(Node end);

	default void deleteEdge(Edge edge) {
		deleteEdge(edge.getStart().getNodeId(), edge.getEnd().getNodeId());
	}

	default void deleteEdge(Node start, Node end) {
		deleteEdge(start.getNodeId(), end.getNodeId());
	}

	void deleteEdge(int startId, int endId);

	<T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, double x, double y, double z, double tangentLength, String permission);

	Map<Integer, Node> loadNodes(RoadMap roadMap);

	void updateNode(Node node);

	void deleteNode(int nodeId);


	NodeGroup createNodeGroup(RoadMap roadMap, NamespacedKey key, String nameFormat, boolean findable);

	Map<NamespacedKey, NodeGroup> loadNodeGroups(RoadMap roadMap);

	void updateNodeGroup(NodeGroup group);

	default void deleteNodeGroup(NodeGroup group) {
		deleteNodeGroup(group.getKey());
	}

	void deleteNodeGroup(NamespacedKey key);

	void addSearchTerms(NodeGroup group, Collection<String> searchTerms);

	void removeSearchTerms(NodeGroup group, Collection<String> searchTerms);

	FoundInfo createFoundInfo(UUID player, Findable findable, Date foundDate);

	Map<Integer, FoundInfo> loadFoundInfo(int globalPlayerId, boolean group);

	void deleteFoundInfo(int globalPlayerId, int nodeId, boolean group);

	SimpleCurveVisualizer newPathVisualizer(NamespacedKey key, String nameFormat, ParticleBuilder particle, ItemStack displayIcon, double particleDistance, int particleSteps, int schedulerPeriod, double curveLength);

	Map<Integer, SimpleCurveVisualizer> loadPathVisualizer();

	void updatePathVisualizer(SimpleCurveVisualizer visualizer);

	void deletePathVisualizer(SimpleCurveVisualizer visualizer);

	Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers();

	void createPlayerVisualizer(int playerId, RoadMap roadMap, SimpleCurveVisualizer visualizer);

	void updatePlayerVisualizer(int playerId, RoadMap roadMap, SimpleCurveVisualizer visualizer);

	void loadVisualizerStyles(Collection<SimpleCurveVisualizer> visualizers);

	void newVisualizerStyle(SimpleCurveVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName);

	void updateVisualizerStyle(SimpleCurveVisualizer visualizer);

	void deleteStyleVisualizer(int visualizerId);

	Map<Integer, Collection<SimpleCurveVisualizer>> loadStyleRoadmapMap(Collection<SimpleCurveVisualizer> visualizers);

	void addStyleToRoadMap(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer);

	void removeStyleFromRoadMap(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer);
}
