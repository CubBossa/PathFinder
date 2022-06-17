package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.visualizer.SimpleCurveVisualizer;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public interface DataStorage {

	RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes);

	RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, SimpleCurveVisualizer pathVis, double findDist, double tangentLength);

	Map<NamespacedKey, RoadMap> loadRoadMaps();

	void updateRoadMap(RoadMap roadMap);

	boolean deleteRoadMap(RoadMap roadMap);

	boolean deleteRoadMap(NamespacedKey key);


	Edge createEdge(Node start, Node end, float weight);

	Collection<Edge> loadEdges(RoadMap roadMap);

	void deleteEdge(Edge edge);

	void deleteEdge(Node start, Node end);


	<T extends Node> T createNode(RoadMap roadMap, Class<T> type, NamespacedKey group, Double x, Double y, Double z, String name, Double tangentLength, String permission);

	Map<Integer, Node> loadNodes(RoadMap roadMap);

	void updateNode(Node node);

	void deleteNode(int nodeId);


	NodeGroup createNodeGroup(RoadMap roadMap, NamespacedKey key, String nameFormat, boolean findable);

	Map<NamespacedKey, NodeGroup> loadNodeGroups(RoadMap roadMap);

	void updateNodeGroup(NodeGroup group);

	void deleteNodeGroup(NodeGroup group);

	void deleteNodeGroup(NamespacedKey key);


	FoundInfo createFoundInfo(UUID player, Findable findable, Date foundDate);

	Map<Integer, FoundInfo> loadFoundInfo(int globalPlayerId, boolean group);

	void deleteFoundInfo(int globalPlayerId, int nodeId, boolean group);

	SimpleCurveVisualizer newPathVisualizer(String name, @Nullable SimpleCurveVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit, @Nullable Integer particleSteps, @Nullable Integer schedulerPeriod);

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
