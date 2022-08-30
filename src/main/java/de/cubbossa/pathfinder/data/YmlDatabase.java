package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Triple;
import xyz.xenondevs.particle.ParticleBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class YmlDatabase implements DataStorage {

	private static final String DIR_RM = "roadmaps";
	private static final String DIR_NG = "nodegroups";
	private static final String DIR_PV = "path_visualizer";

	private File dataDirectory;
	private File roadMapDir;
	private File nodeGroupDir;
	private File pathVisualizerDir;

	private final Map<NamespacedKey, YamlConfiguration> roadmapHandles;
	private final Map<NamespacedKey, YamlConfiguration> nodeGroupHandles;

	public YmlDatabase(File dataDirectory) {
		if (!dataDirectory.isDirectory()) {
			throw new IllegalArgumentException("Data directory must be a directory!");
		}
		this.dataDirectory = dataDirectory;
		this.roadmapHandles = new HashMap<>();
		this.nodeGroupHandles = new HashMap<>();
	}

	public String toFileName(NamespacedKey key) {
		return key.toString().replace(':', '$') + ".yml";
	}

	public NamespacedKey fromFileName(String name) {
		if (name.endsWith(".yml")) {
			name = name.substring(0, name.length() - 4);
		}
		return NamespacedKey.fromString(name.replace('$', ':'));
	}

	private void saveRoadMapFile(NamespacedKey key) {
		File file = new File(roadMapDir, toFileName(key));
		try {
			roadmapHandles.get(key).save(file);
		} catch (IOException e) {
			throw new DataStorageException("Could not save roadmap yml-file for " + key);
		}
	}

	@Override
	public void connect() {
		if (!dataDirectory.exists()) {
			dataDirectory.mkdirs();
		}
		this.roadMapDir = new File(dataDirectory, DIR_RM);
		this.roadMapDir.mkdirs();
		this.nodeGroupDir = new File(dataDirectory, DIR_NG);
		this.nodeGroupDir.mkdirs();
		this.pathVisualizerDir = new File(dataDirectory, DIR_PV);
		this.pathVisualizerDir.mkdirs();
	}

	@Override
	public void disconnect() {
	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, PathVisualizer<?> pathVis, double curveLength) {
		RoadMap roadMap = new RoadMap(key, nameFormat, pathVis, curveLength);
		updateRoadMap(roadMap);
		return roadMap;
	}

	@Override
	public Map<NamespacedKey, RoadMap> loadRoadMaps() {
		HashedRegistry<RoadMap> registry = new HashedRegistry<>();
		for (File file : Arrays.stream(roadMapDir.listFiles())
				.filter(file -> file.getName().matches("\\w+_\\w+\\.yml"))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = fromFileName(file.getName());
				YamlConfiguration cfg = roadmapHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

				registry.put(new RoadMap(key,
						cfg.getString("name_format"),
						VisualizerHandler.getInstance().getPathVisualizer(NamespacedKey.fromString(cfg.getString("path_visualizer"))),
						cfg.getDouble("curve_length")));


			} catch (Throwable t) {
				//TODO log
			}
		}
		return registry;
	}

	@Override
	public void updateRoadMap(RoadMap roadMap) {
		File file = new File(roadMapDir, toFileName(roadMap.getKey()));
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
					throw new DataStorageException("Could not create roadmap file.");
				}
			} catch (IOException e) {
				throw new DataStorageException("Could not create roadmap file.", e);
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		cfg.set("key", roadMap.getKey());
		cfg.set("name_format", roadMap.getNameFormat());
		cfg.set("path_visualizer", roadMap.getVisualizer().getKey().toString());
		cfg.set("curve_length", roadMap.getDefaultBezierTangentLength());

		try {
			cfg.save(file);
		} catch (IOException e) {
			throw new DataStorageException("Could not save roadmap file.", e);
		}
		roadmapHandles.put(roadMap.getKey(), cfg);
	}

	@Override
	public boolean deleteRoadMap(NamespacedKey key) {
		roadmapHandles.remove(key);
		File file = new File(roadMapDir, toFileName(key));
		boolean exists = file.exists();
		file.deleteOnExit();
		return exists;
	}

	@Override
	public Edge createEdge(Node start, Node end, float weight) {
		YamlConfiguration cfg = roadmapHandles.get(start.getRoadMapKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to save edge for not existing roadmap");
		}
		cfg.set("edges." + start.getNodeId() + "." + end.getNodeId(), weight);
		saveRoadMapFile(start.getRoadMapKey());
		return new Edge(start, end, weight);
	}

	@Override
	public List<Edge> createEdges(List<Triple<Node, Node, Float>> edges) {
		return null;
	}

	@Override
	public Collection<Edge> loadEdges(RoadMap roadMap) {
		return null;
	}

	@Override
	public void deleteEdgesFrom(Node start) {
		YamlConfiguration cfg = roadmapHandles.get(start.getRoadMapKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to save edge for not existing roadmap");
		}
		cfg.set("edges." + start.getNodeId(), null);
		saveRoadMapFile(start.getRoadMapKey());
	}

	@Override
	public void deleteEdgesTo(Node end) {
		YamlConfiguration cfg = roadmapHandles.get(end.getRoadMapKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to save edge for not existing roadmap");
		}
		ConfigurationSection section = cfg.getConfigurationSection("edges");
		for (String key : section.getKeys(false)) {
			section.set(key + "." + end.getNodeId(), null);
		}
		saveRoadMapFile(end.getRoadMapKey());
	}

	@Override
	public void deleteEdges(Collection<Edge> edges) {
		if (edges.size() == 0) {
			return;
		}
		Map<NamespacedKey, Collection<Edge>> sorted = new HashMap<>();
		for (Edge edge : edges) {
			sorted.computeIfAbsent(edge.getStart().getRoadMapKey(), roadMap -> new ArrayList<>()).add(edge);
		}
		for (var entry : sorted.entrySet()) {
			YamlConfiguration cfg = roadmapHandles.get(entry.getKey());
			if (cfg == null) {
				throw new DataStorageException("Tried to save edge for not existing roadmap");
			}
			for (Edge edge : entry.getValue()) {
				cfg.set("edges." + edge.getStart().getNodeId() + "." + edge.getEnd().getNodeId(), null);
			}
			saveRoadMapFile(entry.getKey());
		}
	}

	@Override
	public void deleteEdge(Node start, Node end) {
		YamlConfiguration cfg = roadmapHandles.get(end.getRoadMapKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to save edge for not existing roadmap");
		}
		cfg.set("edges." + start.getNodeId() + "." + end.getNodeId(), null);
		saveRoadMapFile(end.getRoadMapKey());
	}

	@Override
	public <T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, Location location, Double tangentLength) {
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
	public void deleteNodes(Integer... nodeId) {

	}

	@Override
	public void deleteNodes(Collection<Integer> nodeIds) {

	}

	@Override
	public void assignNodesToGroup(NodeGroup group, NodeSelection selection) {
		updateNodeGroup(group);
	}

	@Override
	public void removeNodesFromGroup(NodeGroup group, NodeSelection selection) {
		updateNodeGroup(group);
	}

	@Override
	public Map<NamespacedKey, List<Integer>> loadNodeGroupNodes() {
		Map<NamespacedKey, List<Integer>> map = new HashMap<>();
		for (File file : Arrays.stream(nodeGroupDir.listFiles())
				.filter(file -> file.getName().matches("\\w+_\\w+\\.yml"))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = fromFileName(file.getName());
				YamlConfiguration cfg = nodeGroupHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

				map.put(key, (List<Integer>) cfg.getList("nodes"));
			} catch (Throwable t) {
				//TODO log
			}
		}
		return map;
	}

	@Override
	public NodeGroup createNodeGroup(NamespacedKey key, String nameFormat, @Nullable String permission, boolean navigable, boolean discoverable, double findDistance) {
		NodeGroup group = new NodeGroup(key, nameFormat);
		group.setPermission(permission);
		group.setNavigable(navigable);
		group.setDiscoverable(discoverable);
		group.setFindDistance((float) findDistance);
		updateNodeGroup(group);
		return group;
	}

	@Override
	public HashedRegistry<NodeGroup> loadNodeGroups() {
		HashedRegistry<NodeGroup> registry = new HashedRegistry<>();
		for (File file : Arrays.stream(nodeGroupDir.listFiles())
				.filter(file -> file.getName().matches("\\w+_\\w+\\.yml"))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = fromFileName(file.getName());
				YamlConfiguration cfg = nodeGroupHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

				String nameFormat = cfg.getString("name-format");
				NodeGroup group = new NodeGroup(key, nameFormat);
				group.setPermission(cfg.getString("permission"));
				group.setNavigable(cfg.getBoolean("navigable"));
				group.setDiscoverable(cfg.getBoolean("discoverable"));
				group.setFindDistance((float) cfg.getDouble("find-distance"));
				group.setSearchTerms(cfg.getStringList("search-terms"));

				registry.put(group);
			} catch (Throwable t) {
				//TODO log
			}
		}
		return registry;
	}

	@Override
	public void updateNodeGroup(NodeGroup group) {
		File file = new File(nodeGroupDir, toFileName(group.getKey()));
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
					throw new DataStorageException("Could not create nodegroup file.");
				}
			} catch (IOException e) {
				throw new DataStorageException("Could not create nodegroup file.", e);
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		cfg.set("key", group.getKey());
		cfg.set("name-format", group.getNameFormat());
		cfg.set("permission", group.getPermission());
		cfg.set("navigable", group.isNavigable());
		cfg.set("discoverable", group.isDiscoverable());
		cfg.set("find-distance", group.getFindDistance());
		cfg.set("search-terms", group.getSearchTerms());
		cfg.set("nodes", group.stream().map(Groupable::getNodeId).collect(Collectors.toSet()));

		try {
			cfg.save(file);
		} catch (IOException e) {
			throw new DataStorageException("Could not save nodegroup file.", e);
		}
		nodeGroupHandles.put(group.getKey(), cfg);
	}

	@Override
	public void deleteNodeGroup(NamespacedKey key) {
		nodeGroupHandles.remove(key);
		new File(nodeGroupDir, toFileName(key)).deleteOnExit();
	}

	@Override
	public Map<NamespacedKey, Collection<String>> loadSearchTerms() {
		// already loaded in the load method
		return new HashMap<>();
	}

	@Override
	public void addSearchTerms(NodeGroup group, Collection<String> searchTerms) {
		updateNodeGroup(group);
	}

	@Override
	public void removeSearchTerms(NodeGroup group, Collection<String> searchTerms) {
		updateNodeGroup(group);
	}

	@Override
	public DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, Date foundDate) {
		return null;
	}

	@Override
	public Map<UUID, Map<NamespacedKey, DiscoverInfo>> loadDiscoverInfo() {
		return null;
	}

	@Override
	public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {

	}

	@Override
	public PathVisualizer<?> newPathVisualizer(NamespacedKey key, String nameFormat, ParticleBuilder particle, ItemStack displayIcon, double particleDistance, int particleSteps, int schedulerPeriod, double curveLength) {
		return null;
	}

	@Override
	public Map<Integer, PathVisualizer> loadPathVisualizer() {
		return null;
	}

	@Override
	public void updatePathVisualizer(PathVisualizer<?> visualizer) {

	}

	@Override
	public void deletePathVisualizer(PathVisualizer<?> visualizer) {

	}

	@Override
	public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
		return null;
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
