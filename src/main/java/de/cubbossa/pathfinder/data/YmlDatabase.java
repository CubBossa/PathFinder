package de.cubbossa.pathfinder.data;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YmlDatabase implements DataStorage {


	public record Meta(
			boolean oneFileForAllUsers
	) {
	}

	private static final String DIR_RM = "roadmaps";
	private static final String DIR_NG = "nodegroups";
	private static final String DIR_PV = "path_visualizer";
	private static final String DIR_USER = "users";
	private static final Pattern FILE_REGEX = Pattern.compile("[a-zA-Z0-9_]+\\$[a-zA-Z0-9_]+\\.yml");

	private final Meta meta = new Meta(true);

	private File dataDirectory;
	private File roadMapDir;
	private File nodeGroupDir;
	private File pathVisualizerDir;
	private File userDir;

	private final Map<NamespacedKey, YamlConfiguration> roadmapHandles;
	private final Map<NamespacedKey, YamlConfiguration> nodeGroupHandles;
	private final Map<NamespacedKey, YamlConfiguration> visualizerHandles;

	public YmlDatabase(File dataDirectory) {
		if (!dataDirectory.isDirectory()) {
			throw new IllegalArgumentException("Data directory must be a directory!");
		}
		this.dataDirectory = dataDirectory;
		this.roadmapHandles = new HashMap<>();
		this.nodeGroupHandles = new HashMap<>();
		this.visualizerHandles = new HashMap<>();
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
		this.userDir = new File(dataDirectory, DIR_USER);
		this.userDir.mkdirs();
	}

	@Override
	public void disconnect() {
	}


	@Override
	public Map<NamespacedKey, RoadMap> loadRoadMaps() {
		HashedRegistry<RoadMap> registry = new HashedRegistry<>();
		for (File file : Arrays.stream(roadMapDir.listFiles())
				.filter(file -> file.getName().matches(FILE_REGEX.pattern()))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = fromFileName(file.getName());
				YamlConfiguration cfg = roadmapHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

				registry.put(new RoadMap(key,
						cfg.getString("name-format"),
						VisualizerHandler.getInstance().getPathVisualizer(NamespacedKey.fromString(cfg.getString("path-visualizer"))),
						cfg.getDouble("curve-length")));


			} catch (Exception e) {
				throw new DataStorageException("Could not load roadmap: " + file.getName(), e);
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
		cfg.set("key", roadMap.getKey().toString());
		cfg.set("name-format", roadMap.getNameFormat());
		cfg.set("path-visualizer", roadMap.getVisualizer() == null ? null : roadMap.getVisualizer().getKey().toString());
		cfg.set("curve-length", roadMap.getDefaultBezierTangentLength());

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
	public void saveEdges(Collection<Edge> edges) {
		Map<NamespacedKey, Collection<Edge>> mappedEdges = new HashMap<>();
		for (Edge edge : edges) {
			mappedEdges.computeIfAbsent(edge.getStart().getRoadMapKey(), key -> new HashSet<>()).add(edge);
		}
		for (var entry : mappedEdges.entrySet()) {
			NamespacedKey roadMapKey = entry.getKey();
			YamlConfiguration cfg = roadmapHandles.get(roadMapKey);
			if (cfg == null) {
				throw new DataStorageException("Tried to save edge for not existing roadmap");
			}
			for (Edge edge : entry.getValue()) {
				cfg.set("edges." + edge.getStart().getNodeId() + "." + edge.getEnd().getNodeId(), edge.getWeightModifier());
			}
			saveRoadMapFile(roadMapKey);
		}
	}

	@Override
	public Collection<Edge> loadEdges(RoadMap roadMap, Map<Integer, Node> scope) {
		YamlConfiguration cfg = roadmapHandles.get(roadMap.getKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to load edges for non existing roadmap");
		}
		Collection<Edge> result = new HashSet<>();
		ConfigurationSection edgeSection = cfg.getConfigurationSection("edges");
		if (edgeSection == null) {
			return result;
		}
		for (String key : edgeSection.getKeys(false)) {
			ConfigurationSection innerSection = edgeSection.getConfigurationSection(key);
			if (innerSection == null) {
				continue;
			}
			int start = Integer.parseInt(key);
			for (String innerKey : innerSection.getKeys(false)) {
				double weight = innerSection.getDouble(innerKey);
				result.add(new Edge(
						scope.get(start),
						scope.get(Integer.parseInt(innerKey)),
						(float) weight));
			}
		}
		return result;
	}

	@Override
	public void deleteEdgesFrom(Node start) {
		YamlConfiguration cfg = roadmapHandles.get(start.getRoadMapKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to save edge for non existing roadmap");
		}
		cfg.set("edges." + start.getNodeId(), null);
		saveRoadMapFile(start.getRoadMapKey());
	}

	@Override
	public void deleteEdgesTo(Node end) {
		YamlConfiguration cfg = roadmapHandles.get(end.getRoadMapKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to save edge for non existing roadmap");
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
		YamlConfiguration cfg = roadmapHandles.computeIfAbsent(end.getRoadMapKey(), key ->
				YamlConfiguration.loadConfiguration(new File(roadMapDir, toFileName(key))));
		cfg.set("edges." + start.getNodeId() + "." + end.getNodeId(), null);
		saveRoadMapFile(end.getRoadMapKey());
	}

	@Override
	public Map<Integer, Node> loadNodes(RoadMap roadMap) {
		YamlConfiguration cfg = roadmapHandles.get(roadMap.getKey());
		if (cfg == null) {
			throw new DataStorageException("Tried to load nodes for non existing roadmap");
		}
		Map<Integer, Node> result = new HashMap<>();
		ConfigurationSection nodeSection = cfg.getConfigurationSection("nodes");
		if (nodeSection == null) {
			return result;
		}
		for (String key : nodeSection.getKeys(false)) {
			ConfigurationSection innerSection = nodeSection.getConfigurationSection(key);
			if (innerSection == null) {
				continue;
			}
			//TODO for now i parse them only to waypoints, but lateron they will have a datastructure like pathvisualizers
			int id = Integer.parseInt(key);
			Location location = innerSection.getLocation("location");
			Waypoint node = RoadMapHandler.WAYPOINT_TYPE.getFactory()
					.apply(new NodeType.NodeCreationContext(roadMap, id, location));
			node.setCurveLength(innerSection.getDouble("curve-length"));
			result.put(id, node);
		}
		return result;
	}


	@Override
	public void updateNode(Node node) {
		YamlConfiguration cfg = roadmapHandles.computeIfAbsent(node.getRoadMapKey(), key ->
				YamlConfiguration.loadConfiguration(new File(roadMapDir, toFileName(key))));
		ConfigurationSection nodeSection = cfg.getConfigurationSection("nodes." + node.getNodeId());
		if (nodeSection == null) {
			nodeSection = cfg.createSection("nodes." + node.getNodeId());
		}
		nodeSection.set("type", node.getType().getKey().toString());
		nodeSection.set("location", node.getLocation());
		nodeSection.set("curve-length", node.getCurveLength());

		saveRoadMapFile(node.getRoadMapKey());
	}

	@Override
	public void deleteNodes(Integer... nodeId) {
		deleteNodes(Lists.newArrayList(nodeId));
	}

	@Override
	public void deleteNodes(Collection<Integer> nodeIds) {
		for (File file : Arrays.stream(roadMapDir.listFiles())
				.filter(file -> file.getName().matches(FILE_REGEX.pattern()))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = fromFileName(file.getName());
				YamlConfiguration cfg = roadmapHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));
				for (int id : nodeIds) {
					cfg.set("node." + id, null);
				}
				saveRoadMapFile(key);
			} catch (Exception e) {
				throw new DataStorageException("Could not delete nodes for roadmap in file " + file, e);
			}
		}
	}

	@Override
	public void assignNodesToGroup(NodeGroup group, NodeSelection selection) {
		updateNodeGroup(group);
	}

	@Override
	public void removeNodesFromGroup(NodeGroup group, Iterable<Groupable> selection) {
		updateNodeGroup(group);
	}

	@Override
	public Map<Integer, ? extends Collection<NamespacedKey>> loadNodeGroupNodes() {
		Map<Integer, HashSet<NamespacedKey>> map = new HashMap<>();
		for (File file : Arrays.stream(nodeGroupDir.listFiles())
				.filter(file -> file.getName().matches(FILE_REGEX.pattern()))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = fromFileName(file.getName());
				YamlConfiguration cfg = nodeGroupHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

				List<Integer> nodes = (List<Integer>) cfg.getList("nodes");
				if (nodes != null) {
					nodes.forEach(n -> map.computeIfAbsent(n, i -> new HashSet<>()).add(key));
				}
			} catch (Exception e) {
				throw new DataStorageException("Could not load nodegroup: " + file.getName(), e);
			}
		}
		return map;
	}


	@Override
	public HashedRegistry<NodeGroup> loadNodeGroups() {
		HashedRegistry<NodeGroup> registry = new HashedRegistry<>();
		for (File file : Arrays.stream(nodeGroupDir.listFiles())
				.filter(file -> file.getName().matches(FILE_REGEX.pattern()))
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
			} catch (Exception e) {
				throw new DataStorageException("Could not load nodegroup: " + file.getName(), e);
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
		cfg.set("key", group.getKey().toString());
		cfg.set("name-format", group.getNameFormat());
		cfg.set("permission", group.getPermission());
		cfg.set("navigable", group.isNavigable());
		cfg.set("discoverable", group.isDiscoverable());
		cfg.set("find-distance", group.getFindDistance());
		cfg.set("search-terms", new ArrayList<>(group.getSearchTerms()));
		cfg.set("nodes", group.stream().map(Groupable::getNodeId).collect(Collectors.toList()));

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
	public DiscoverInfo createDiscoverInfo(UUID playerId, Discoverable discoverable, Date foundDate) {
		File file;
		YamlConfiguration config;
		ConfigurationSection cfg;
		if (meta.oneFileForAllUsers()) {
			file = new File(userDir, "user_data.yml");
			config = YamlConfiguration.loadConfiguration(file);
			if (config.getConfigurationSection(playerId + "") != null) {
				cfg = config.getConfigurationSection(playerId + "");
			} else {
				cfg = config.createSection(playerId + "");
			}
		} else {
			file = new File(userDir, playerId + ".yml");
			config = YamlConfiguration.loadConfiguration(file);
			cfg = config;
		}
		ConfigurationSection discoveries = cfg.getConfigurationSection("discoveries");
		if (discoveries == null) {
			discoveries = cfg.createSection("discoveries");
		}
		discoveries.set(discoverable.getKey().toString() + ".date", foundDate);
		try {
			config.save(file);
		} catch (IOException e) {
			throw new DataStorageException("Could not save discovery info." + e);
		}
		return new DiscoverInfo(playerId, discoverable.getKey(), foundDate);
	}

	@Override
	public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
		File file;
		ConfigurationSection cfg;
		if (meta.oneFileForAllUsers()) {
			file = new File(userDir, "user_data.yml");
			cfg = YamlConfiguration.loadConfiguration(file);
			if (cfg.getConfigurationSection(playerId + "") != null) {
				cfg = cfg.getConfigurationSection(playerId + "");
			} else {
				cfg = cfg.createSection(playerId + "");
			}
		} else {
			file = new File(userDir, playerId + ".yml");
			cfg = YamlConfiguration.loadConfiguration(file);
		}
		ConfigurationSection discoveries = cfg.getConfigurationSection("discoveries");
		if (discoveries == null) {
			discoveries = cfg.createSection("discoveries");
		}
		Map<NamespacedKey, DiscoverInfo> map = new HashMap<>();
		for (String key : discoveries.getKeys(false)) {
			NamespacedKey nkey = NamespacedKey.fromString(key);
			map.put(nkey, new DiscoverInfo(playerId, nkey, (Date) discoveries.get(key + ".date")));
		}
		return map;
	}

	@Override
	public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
		File file;
		YamlConfiguration config;
		ConfigurationSection cfg;
		if (meta.oneFileForAllUsers()) {
			file = new File(userDir, "user_data.yml");
			config = YamlConfiguration.loadConfiguration(file);
			if (config.getConfigurationSection(playerId + "") != null) {
				cfg = config.getConfigurationSection(playerId + "");
			} else {
				cfg = config.createSection(playerId + "");
			}
		} else {
			file = new File(userDir, playerId + ".yml");
			config = YamlConfiguration.loadConfiguration(file);
			cfg = config;
		}
		ConfigurationSection discoveries = cfg.getConfigurationSection("discoveries");
		if (discoveries == null) {
			return;
		}
		discoveries.set(discoverKey.toString(), null);
		try {
			config.save(file);
		} catch (IOException e) {
			throw new DataStorageException("Could not delete discovery info." + e);
		}
	}

	@Override
	public Map<NamespacedKey, PathVisualizer<?, ?>> loadPathVisualizer() {
		HashedRegistry<PathVisualizer<?, ?>> registry = new HashedRegistry<>();
		for (File file : Arrays.stream(pathVisualizerDir.listFiles())
				.filter(file -> file.getName().matches("\\w+$\\w+\\.yml"))
				.collect(Collectors.toList())) {
			try {
				NamespacedKey key = fromFileName(file.getName());
				YamlConfiguration cfg = visualizerHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));
				registry.put(loadVis(key, cfg));
			} catch (Exception e) {
				throw new DataStorageException("Could not load visualizer: " + file.getName(), e);
			}
		}
		return registry;
	}

	private <T extends PathVisualizer<T, D>, D> PathVisualizer<T, D> loadVis(NamespacedKey key, ConfigurationSection cfg) {
		VisualizerType<T> type = VisualizerHandler.getInstance().getVisualizerType(NamespacedKey.fromString(cfg.getString("type")));
		if (type == null) {
			throw new IllegalStateException("Invalid visualizer type: " + cfg.getString("type"));
		}
		T vis = type.create(key, cfg.getString("display-name"));
		type.deserialize(vis, (Map<String, Object>) cfg.get("props"));
		return vis;
	}

	@Override
	public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {
		File file = new File(pathVisualizerDir, toFileName(visualizer.getKey()));
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
					throw new DataStorageException("Could not create visualizer file.");
				}
			} catch (IOException e) {
				throw new DataStorageException("Could not create visualizer file.", e);
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		cfg.set("type", visualizer.getType().getKey().toString());
		cfg.set("display-name", visualizer.getNameFormat());
		visualizer.getType().serialize(visualizer).forEach(cfg::set);

		try {
			cfg.save(file);
		} catch (IOException e) {
			throw new DataStorageException("Could not save visualizer file.", e);
		}
		visualizerHandles.put(visualizer.getKey(), cfg);
	}

	@Override
	public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
		roadmapHandles.remove(visualizer.getKey());
		File file = new File(roadMapDir, toFileName(visualizer.getKey()));
		file.deleteOnExit();
	}

	@Override
	public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
		return null;
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
}
