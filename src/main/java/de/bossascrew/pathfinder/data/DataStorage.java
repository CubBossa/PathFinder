package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.node.Edge;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public interface DataStorage {


	RoadMap createRoadMap(NamespacedKey key, World world, boolean findableNodes);

	RoadMap createRoadMap(NamespacedKey key, World world, boolean findableNodes, int pathVis, int editModeVis, double findDist, double tangentLength);

	Map<NamespacedKey, RoadMap> loadRoadMaps();

	void updateRoadMap(RoadMap roadMap);

	boolean deleteRoadMap(RoadMap roadMap);

	boolean deleteRoadMap(NamespacedKey key);


	Edge createEdge(Node start, Node end, float weight);

	Collection<Edge> loadEdges(RoadMap roadMap);

	void deleteEdge(Edge edge);

	void deleteEdge(Node start, Node end);


	<T extends Node> T createNode(RoadMap roadMap, Class<T> type, NamespacedKey group, Double x, Double y, Double z, String name, Double tangentLength, String permission);

	Map<Integer, Waypoint> loadNodes(RoadMap roadMap);

	void updateFindable(@NotNull Waypoint findable);

	void deleteNode(int nodeId);


	NodeGroup createNodeGroup(RoadMap roadMap, NamespacedKey key, boolean findable);

	Map<NamespacedKey, NodeGroup> loadNodeGroups(RoadMap roadMap);

	void updateNodeGroup(NodeGroup group);

	void deleteNodeGroup(NodeGroup group);

	void deleteNodeGroup(NamespacedKey key);


	FoundInfo newFoundInfo(int globalPlayerId, int foundId, boolean group, Date foundDate);

	Map<Integer, FoundInfo> loadFoundNodes(int globalPlayerId, boolean group);

	void deleteFoundNode(int globalPlayerId, int nodeId, boolean group);

	EditModeVisualizer newEditModeVisualizer(String name, @Nullable EditModeVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit, @Nullable Integer schedulerPeriod, @Nullable Integer nodeHeadId, @Nullable Integer edgeHeadId);

	Map<Integer, EditModeVisualizer> loadEditModeVisualizer();

	PathVisualizer newPathVisualizer(String name, @Nullable PathVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit, @Nullable Integer particleSteps, @Nullable Integer schedulerPeriod);

	Map<Integer, PathVisualizer> loadPathVisualizer();

	void updateEditModeVisualizer(EditModeVisualizer visualizer);

	void updatePathVisualizer(PathVisualizer visualizer);

	void deletePathVisualizer(PathVisualizer visualizer);

	void deleteEditModeVisualizer(EditModeVisualizer visualizer);

	Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers();

	void createPlayerVisualizer(int playerId, RoadMap roadMap, PathVisualizer visualizer);

	void updatePlayerVisualizer(int playerId, RoadMap roadMap, PathVisualizer visualizer);

	void loadVisualizerStyles(Collection<PathVisualizer> visualizers);

	void newVisualizerStyle(PathVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName);

	void updateVisualizerStyle(PathVisualizer visualizer);

	void deleteStyleVisualizer(int visualizerId);

	Map<Integer, Collection<PathVisualizer>> loadStyleRoadmapMap(Collection<PathVisualizer> visualizers);

	void addStyleToRoadMap(RoadMap roadMap, PathVisualizer pathVisualizer);

	void removeStyleFromRoadMap(RoadMap roadMap, PathVisualizer pathVisualizer);
}
