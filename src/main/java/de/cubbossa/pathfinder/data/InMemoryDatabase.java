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
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Triple;
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
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, PathVisualizer<?, ?> pathVis, double tangentLength) {
		log("Create Roadmap");
		return new RoadMap(key, nameFormat, pathVis, tangentLength);
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
	public List<Edge> createEdges(List<Triple<Node, Node, Float>> edges) {
		return null;
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
	public void deleteEdges(Collection<Edge> edges) {

	}

	@Override
	public void deleteEdge(Node start, Node end) {
		log("Delete Edge");
	}

	@Override
	public <T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, Location location, Double tangentLength) {
		T node = type.getFactory().apply(roadMap, nodeIdCounter++);
		node.setLocation(location);
		node.setCurveLength(tangentLength);
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
	public void deleteNodes(Integer... nodeId) {

	}

	@Override
	public void deleteNodes(Collection<Integer> nodeIds) {

	}

	@Override
	public void assignNodesToGroup(NodeGroup group, NodeSelection selection) {

	}

	@Override
	public void removeNodesFromGroup(NodeGroup group, NodeSelection selection) {

	}

	@Override
	public Map<NamespacedKey, List<Integer>> loadNodeGroupNodes() {
		log("Load Nodegroup Nodes");
		return new HashMap<>();
	}

	@Override
	public NodeGroup createNodeGroup(NamespacedKey key, String nameFormat, @Nullable String permission, boolean navigable, boolean discoverable, double findDistance) {
		log("Create Nodegroup");
		return new NodeGroup(key, nameFormat);
	}

	@Override
	public HashedRegistry<NodeGroup> loadNodeGroups() {
		log("Load Nodegroups");
		return new HashedRegistry<>();
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
		log("Load Nodegroup Searchterms");
		return new HashMap<>();
	}

	@Override
	public void addSearchTerms(NodeGroup group, Collection<String> searchTerms) {
		log("Add Searchterms");
	}

	@Override
	public void removeSearchTerms(NodeGroup group, Collection<String> searchTerms) {
		log("Remove Searchterms");
	}

	@Override
	public DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, Date foundDate) {
		log("Create Foundinfo");
		return new DiscoverInfo(player, discoverable.getUniqueKey(), foundDate);
	}

	@Override
	public Map<UUID, Map<NamespacedKey, DiscoverInfo>> loadDiscoverInfo() {
		log("Load Foundinfos");
		return new TreeMap<>();
	}

	@Override
	public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
		log("Delete Foundinfos");
	}

	@Override
	public Map<NamespacedKey, PathVisualizer<?, ?>> loadPathVisualizer() {
		log("Loaded Visualizers");
		return new HashMap<>();
	}


	@Override
	public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {
		log("Updated Visualizer");
	}

	@Override
	public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
		log("Deleted Visualizer");
	}


	@Override
	public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
		return new HashMap<>();
	}

	@Override
	public void createPlayerVisualizer(int playerId, RoadMap roadMap, ParticleVisualizer visualizer) {

	}

	@Override
	public void updatePlayerVisualizer(int playerId, RoadMap roadMap, ParticleVisualizer visualizer) {

	}

	@Override
	public void loadVisualizerStyles(Collection<ParticleVisualizer> visualizers) {

	}

	@Override
	public void newVisualizerStyle(ParticleVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName) {

	}

	@Override
	public void updateVisualizerStyle(ParticleVisualizer visualizer) {

	}

	@Override
	public void deleteStyleVisualizer(int visualizerId) {

	}

	@Override
	public Map<Integer, Collection<ParticleVisualizer>> loadStyleRoadmapMap(Collection<ParticleVisualizer> visualizers) {
		return null;
	}

	@Override
	public void addStyleToRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer) {

	}

	@Override
	public void removeStyleFromRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer) {

	}

	@Override
	public NodeBatchCreator newNodeBatch() {
		return null;
	}
}
