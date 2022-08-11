package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.core.node.*;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.HashedRegistry;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.bossascrew.pathfinder.module.visualizing.visualizer.SimpleCurveVisualizer;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class YmlDatabase implements DataStorage {

	private static final String DIR_RM = "roadmaps";
	private static final String DIR_PV = "path_visualizer";

	private File dataDirectory;
	private File roadMapDir;
	private File pathVisualizerDir;

	private final Map<NamespacedKey, YamlConfiguration> roadmapHandles;

	public YmlDatabase(File dataDirectory) {
		if (!dataDirectory.isDirectory()) {
			throw new IllegalArgumentException("Data directory must be a directory!");
		}
		this.dataDirectory = dataDirectory;
		this.roadmapHandles = new HashMap<>();
	}

	@Override
	public void connect() {
		if (!dataDirectory.exists()) {
			dataDirectory.mkdirs();
		}
		this.roadMapDir = new File(dataDirectory, DIR_RM);
		this.roadMapDir.mkdirs();
		this.pathVisualizerDir = new File(dataDirectory, DIR_PV);
		this.pathVisualizerDir.mkdirs();
	}

	@Override
	public void disconnect() {
	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, PathVisualizer pathVis, double findDist, double curveLength) {
		File file = new File(roadMapDir, key.toString().replace(":", "_") + ".yml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new DataStorageException("Could not create roadmap file.", e);
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		cfg.set("key", key);
		cfg.set("name_format", nameFormat);
		cfg.set("world", world.getUID().toString());
		cfg.set("nodes_findable", findableNodes);
		cfg.set("path_visualizer", pathVis);
		cfg.set("node_find_distance", findDist);
		cfg.set("curve_length", curveLength);

		try {
			cfg.save(file);
		} catch (IOException e) {
			throw new DataStorageException("Could not save roadmap file.", e);
		}
		roadmapHandles.put(key, cfg);
		return new RoadMap(key, nameFormat, world, findableNodes, pathVis, findDist, curveLength);
	}

	@Override
	public Map<NamespacedKey, RoadMap> loadRoadMaps() {
		HashedRegistry<RoadMap> registry = new HashedRegistry<>();
		for (File file : Arrays.stream(roadMapDir.listFiles())
				.filter(file -> file.getName().matches("\\w+_\\w\\.yml"))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = NamespacedKey.fromString(file.getName().substring(0, file.getName().length() - 4).replace("_", ":"));
				YamlConfiguration cfg = roadmapHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

				registry.put(new RoadMap(key,
						cfg.getString("name_format"),
						Bukkit.getWorld(UUID.fromString(cfg.getString("world"))),
						cfg.getBoolean("nodes_findable"),
						null, //TODO
						cfg.getDouble("node_find_distance"),
						cfg.getDouble("curve_length")));


			} catch (Throwable t) {
				//TODO log
			}
		}
		return registry;
	}

	@Override
	public void updateRoadMap(RoadMap roadMap) {

	}

	@Override
	public boolean deleteRoadMap(NamespacedKey key) {
		return false;
	}

	@Override
	public Edge createEdge(Node start, Node end, float weight) {
		return null;
	}

	@Override
	public Collection<Edge> loadEdges(RoadMap roadMap) {
		return null;
	}

	@Override
	public void deleteEdgesFrom(Node start) {

	}

	@Override
	public void deleteEdgesTo(Node end) {

	}

	@Override
	public void deleteEdge(int startId, int endId) {

	}

	@Override
	public <T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, double x, double y, double z, double tangentLength, String permission) {
		return null;
	}

	@Override
	public Map<Integer, Node> loadNodes(RoadMap roadMap) {
		return null;
	}

	@Override
	public void updateNode(Node node) {

	}

	@Override
	public void deleteNode(int nodeId) {

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
		return null;
	}

	@Override
	public Map<NamespacedKey, NodeGroup> loadNodeGroups(RoadMap roadMap) {
		return null;
	}

	@Override
	public void updateNodeGroup(NodeGroup group) {

	}

	@Override
	public void deleteNodeGroup(NamespacedKey key) {

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
		return null;
	}

	@Override
	public Map<Integer, DiscoverInfo> loadFoundInfo(int globalPlayerId, boolean group) {
		return null;
	}

	@Override
	public void deleteFoundInfo(int globalPlayerId, int nodeId, boolean group) {

	}

	@Override
	public PathVisualizer newPathVisualizer(NamespacedKey key, String nameFormat, ParticleBuilder particle, ItemStack displayIcon, double particleDistance, int particleSteps, int schedulerPeriod, double curveLength) {
		return null;
	}

	@Override
	public Map<Integer, PathVisualizer> loadPathVisualizer() {
		return null;
	}

	@Override
	public void updatePathVisualizer(PathVisualizer visualizer) {

	}

	@Override
	public void deletePathVisualizer(PathVisualizer visualizer) {

	}

	@Override
	public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
		return null;
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
