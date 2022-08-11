package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.core.node.NodeType;
import de.bossascrew.pathfinder.core.node.*;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.bossascrew.pathfinder.module.visualizing.visualizer.SimpleCurveVisualizer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;

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
	public void connect() {

	}

	@Override
	public void disconnect() {

	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, PathVisualizer pathVis, double findDist, double tangentLength) {
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
	public void deleteEdgesFrom(Node start) {

	}

	@Override
	public void deleteEdgesTo(Node end) {

	}

	@Override
	public void deleteEdge(int startId, int endId) {
		log("Delete Edge");
	}

	@Override
	public <T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, double x, double y, double z, double tangentLength, String permission) {
		T node = type.getFactory().apply(roadMap, nodeIdCounter++);
		node.setPosition(new Vector(x, y, z));
		node.setCurveLength(tangentLength);
		node.setPermission(permission);
		if (node instanceof Groupable groupable) {
			groups.forEach(groupable::addGroup);
		}
		log("Create Node");
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
	public void assignNodesToGroup(NodeGroup group, NodeSelection selection) {

	}

	@Override
	public void removeNodesFromGroup(NodeGroup group, NodeSelection selection) {

	}

	@Override
	public Map<NamespacedKey, List<Integer>> loadNodeGroupNodes() {
		return null;
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
	public Map<NamespacedKey, Collection<String>> loadSearchTerms() {
		return null;
	}

	@Override
	public void addSearchTerms(NodeGroup group, Collection<String> searchTerms) {

	}

	@Override
	public void removeSearchTerms(NodeGroup group, Collection<String> searchTerms) {

	}

	@Override
	public DiscoverInfo createFoundInfo(UUID player, Discoverable discoverable, Date foundDate) {
		log("Create Foundinfo");
		return new DiscoverInfo(player, discoverable, foundDate);
	}

	@Override
	public Map<Integer, DiscoverInfo> loadFoundInfo(int globalPlayerId, boolean group) {
		log("Load Foundinfos");
		return new TreeMap<>();
	}

	@Override
	public void deleteFoundInfo(int globalPlayerId, int nodeId, boolean group) {
		log("Delete Foundinfos");
	}

	@Override
	public SimpleCurveVisualizer newPathVisualizer(NamespacedKey key, String nameFormat, ParticleBuilder particle, ItemStack displayIcon, double particleDistance, int particleSteps, int schedulerPeriod, double curveLength) {
		log("Created Visualizer");
		var vis = new SimpleCurveVisualizer(key, nameFormat);
		vis.setParticle(particle);
		vis.setParticleDistance(particleDistance);
		vis.setParticleSteps(particleSteps);
		vis.setSchedulerPeriod(schedulerPeriod);
		return vis;
	}

	@Override
	public Map<Integer, PathVisualizer> loadPathVisualizer() {
		log("Loaded Visualizers");
		return new HashMap<>();
	}


	@Override
	public void updatePathVisualizer(PathVisualizer visualizer) {
		log("Updated Visualizer");
	}

	@Override
	public void deletePathVisualizer(PathVisualizer visualizer) {
		log("Deleted Visualizer");
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
