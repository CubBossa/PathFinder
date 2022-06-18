package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.visualizer.SimpleCurveVisualizer;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InMemoryDatabase implements DataStorage {

	private final Logger logger;
	private int nodeIdCounter = 0;

	public InMemoryDatabase(Logger logger) {
		this.logger = logger;
	}

	private void log(String message) {
		logger.log(Level.INFO, "Database - " + message);
	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes) {
		return createRoadMap(key, nameFormat, world, findableNodes, null, 1f, 1f);
	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, SimpleCurveVisualizer pathVis, double findDist, double tangentLength) {
		log("Create Roadmap");
		return new RoadMap(key, nameFormat, world, findableNodes, pathVis, findDist, tangentLength);
	}

	@Override
	public Map<NamespacedKey, RoadMap> loadRoadMaps() {
		log("Load Roadmaps");
		return new HashMap<>();
	}

	@Override
	public void updateRoadMap(RoadMap roadMap) {
		log("Update Roadmap");
	}

	@Override
	public boolean deleteRoadMap(RoadMap roadMap) {
		return deleteRoadMap(roadMap.getKey());
	}

	@Override
	public boolean deleteRoadMap(NamespacedKey key) {
		log("Delete Roadmap");
		return true;
	}

	@Override
	public Edge createEdge(Node start, Node end, float weight) {
		log("Create Edge");
		return new Edge(start, end, weight);
	}

	@Override
	public Collection<Edge> loadEdges(RoadMap roadMap) {
		log("Load Edge");
		return new HashSet<>();
	}

	@Override
	public void deleteEdge(Edge edge) {
		log("Delete Edge");
	}

	@Override
	public void deleteEdge(Node start, Node end) {
		log("Delete Edge");
	}

	@Override
	public <T extends Node> T createNode(RoadMap roadMap, Class<T> type, NamespacedKey group, Double x, Double y, Double z, String name, Double tangentLength, String permission) {
		T node = (T) new Waypoint(nodeIdCounter++, roadMap, name);
		node.setPosition(new Vector(x, y, z));
		node.setBezierTangentLength(tangentLength);
		node.setPermission(permission);
		log("Delete Node");
		return node;
	}

	@Override
	public Map<Integer, Node> loadNodes(RoadMap roadMap) {
		log("Load Nodes");
		return new HashMap<>();
	}

	@Override
	public void updateNode(Node node) {
		log("Update Node");
	}

	@Override
	public void deleteNode(int nodeId) {
		log("Delete Node");
	}

	@Override
	public NodeGroup createNodeGroup(RoadMap roadMap, NamespacedKey key, String nameFormat, boolean findable) {
		log("Create Nodegroup");
		return new NodeGroup(key, roadMap, nameFormat);
	}

	@Override
	public Map<NamespacedKey, NodeGroup> loadNodeGroups(RoadMap roadMap) {
		log("Load Nodegroups");
		return new HashMap<>();
	}

	@Override
	public void updateNodeGroup(NodeGroup group) {
		log("Update Nodegroup");
	}

	@Override
	public void deleteNodeGroup(NodeGroup group) {
		log("Delete Nodegroup");
	}

	@Override
	public void deleteNodeGroup(NamespacedKey key) {
		log("Delete Nodegroup");
	}

	@Override
	public FoundInfo createFoundInfo(UUID player, Findable findable, Date foundDate) {
		log("Create Foundinfo");
		return new FoundInfo(player, findable, foundDate);
	}

	@Override
	public Map<Integer, FoundInfo> loadFoundInfo(int globalPlayerId, boolean group) {
		log("Load Foundinfos");
		return new TreeMap<>();
	}

	@Override
	public void deleteFoundInfo(int globalPlayerId, int nodeId, boolean group) {
		log("Delete Foundinfos");
	}

	@Override
	public SimpleCurveVisualizer newPathVisualizer(NamespacedKey key, String nameFormat, Particle particle, Double particleDistance, Integer particleSteps, Integer schedulerPeriod) {
		log("Created Visualizer");
		var vis = new SimpleCurveVisualizer(key, nameFormat);
		vis.setParticle( particle);
		vis.setParticleDistance(particleDistance);
		vis.setParticleSteps(particleSteps);
		vis.setSchedulerPeriod(schedulerPeriod);
		return vis;
	}

	@Override
	public Map<Integer, SimpleCurveVisualizer> loadPathVisualizer() {
		return new HashMap<>();
	}


	@Override
	public void updatePathVisualizer(SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void deletePathVisualizer(SimpleCurveVisualizer visualizer) {

	}


	@Override
	public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
		return new HashMap<>();
	}

	@Override
	public void createPlayerVisualizer(int playerId, RoadMap roadMap, SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void updatePlayerVisualizer(int playerId, RoadMap roadMap, SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void loadVisualizerStyles(Collection<SimpleCurveVisualizer> visualizers) {

	}

	@Override
	public void newVisualizerStyle(SimpleCurveVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName) {

	}

	@Override
	public void updateVisualizerStyle(SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void deleteStyleVisualizer(int visualizerId) {

	}

	@Override
	public Map<Integer, Collection<SimpleCurveVisualizer>> loadStyleRoadmapMap(Collection<SimpleCurveVisualizer> visualizers) {
		return null;
	}

	@Override
	public void addStyleToRoadMap(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer) {

	}

	@Override
	public void removeStyleFromRoadMap(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer) {

	}
}
