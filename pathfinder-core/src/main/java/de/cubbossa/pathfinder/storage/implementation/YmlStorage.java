package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import de.cubbossa.pathfinder.util.WorldImpl;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YmlStorage extends CommonStorage implements WaypointDataStorage {

  private static final String FILE_TYPES = "node_types.yml";
  private static final String FILE_NODES = "waypoints.yml";
  private static final String DIR_NG = "nodegroups";
  private static final String DIR_PV = "path_visualizer";
  private static final String DIR_USER = "users";
  private static final Pattern FILE_REGEX = Pattern.compile("[a-zA-Z0-9_]+\\$[a-zA-Z0-9_]+\\.yml");
  private final Meta meta = new Meta(true);
  private final Map<NamespacedKey, YamlConfiguration> visualizerHandles;
  private final File dataDirectory;
	@Getter
	@Setter
	private Logger logger;
  private File nodeGroupDir;
  private File pathVisualizerDir;
  private File userDir;

  public YmlStorage(File dataDirectory, NodeTypeRegistry nodeTypeRegistry) {
	  super(nodeTypeRegistry);
	  if (!dataDirectory.isDirectory()) {
      throw new IllegalArgumentException("Data directory must be a directory!");
    }
    this.dataDirectory = dataDirectory;
    this.visualizerHandles = new HashMap<>();
  }

	private File fileTypes() {
		return new File(dataDirectory, FILE_TYPES);
	}

	private File fileWaypoints() {
		return new File(dataDirectory, FILE_NODES);
	}

	private File fileGroup(NamespacedKey key) {
		return new File(DIR_NG, toFileName(key));
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

  private void workOnFile(File file, Consumer<YamlConfiguration> editor) {
	  workOnFile(file, cfg -> {
		  editor.accept(cfg);
		  return null;
	  });
  }

	private <T> Optional<T> workOnFileIfExists(File file, Function<YamlConfiguration, T> editor) {
		if (!file.exists()) {
			return Optional.empty();
		}
		return Optional.ofNullable(workOnFile(file, editor));
	}

  private <T> T workOnFile(File file, Function<YamlConfiguration, T> editor) {
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
    T data = editor.apply(cfg);
    try {
      cfg.save(file);
    } catch (IOException e) {
			throw new DataStorageException("Could not work on file '" + file.getName() + "'.", e);
    }
    return data;
  }
//  @Override
//  public DiscoverInfo createDiscoverInfo(UUID playerId, NodeGroup discoverable,
//                                         LocalDateTime foundDate) {
//    File file;
//    YamlConfiguration config;
//    ConfigurationSection cfg;
//    if (meta.oneFileForAllUsers()) {
//      file = new File(userDir, "user_data.yml");
//      config = YamlConfiguration.loadConfiguration(file);
//      if (config.getConfigurationSection(playerId + "") != null) {
//        cfg = config.getConfigurationSection(playerId + "");
//      } else {
//        cfg = config.createSection(playerId + "");
//      }
//    } else {
//      file = new File(userDir, playerId + ".yml");
//      config = YamlConfiguration.loadConfiguration(file);
//      cfg = config;
//    }
//    ConfigurationSection discoveries = cfg.getConfigurationSection("discoveries");
//    if (discoveries == null) {
//      discoveries = cfg.createSection("discoveries");
//    }
//    discoveries.set(discoverable.getKey() + ".date", foundDate);
//    try {
//      config.save(file);
//    } catch (IOException e) {
//      throw new DataStorageException("Could not save discovery info." + e);
//    }
//    return new DiscoverInfo(playerId, discoverable.getKey(), foundDate);
//  }
//
//  @Override
//  public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
//    File file;
//    ConfigurationSection cfg;
//    if (meta.oneFileForAllUsers()) {
//      file = new File(userDir, "user_data.yml");
//      cfg = YamlConfiguration.loadConfiguration(file);
//      if (cfg.getConfigurationSection(playerId + "") != null) {
//        cfg = cfg.getConfigurationSection(playerId + "");
//      } else {
//        cfg = cfg.createSection(playerId + "");
//      }
//    } else {
//      file = new File(userDir, playerId + ".yml");
//      cfg = YamlConfiguration.loadConfiguration(file);
//    }
//    ConfigurationSection discoveries = cfg.getConfigurationSection("discoveries");
//    if (discoveries == null) {
//      discoveries = cfg.createSection("discoveries");
//    }
//    Map<NamespacedKey, DiscoverInfo> map = new HashMap<>();
//    for (String key : discoveries.getKeys(false)) {
//      NamespacedKey nkey = NamespacedKey.fromString(key);
//      map.put(nkey,
//          new DiscoverInfo(playerId, nkey, (LocalDateTime) discoveries.get(key + ".date")));
//    }
//    return map;
//  }
//
//  @Override
//  public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
//    File file;
//    YamlConfiguration config;
//    ConfigurationSection cfg;
//    if (meta.oneFileForAllUsers()) {
//      file = new File(userDir, "user_data.yml");
//      config = YamlConfiguration.loadConfiguration(file);
//      if (config.getConfigurationSection(playerId + "") != null) {
//        cfg = config.getConfigurationSection(playerId + "");
//      } else {
//        cfg = config.createSection(playerId + "");
//      }
//    } else {
//      file = new File(userDir, playerId + ".yml");
//      config = YamlConfiguration.loadConfiguration(file);
//      cfg = config;
//    }
//    ConfigurationSection discoveries = cfg.getConfigurationSection("discoveries");
//    if (discoveries == null) {
//      return;
//    }
//    discoveries.set(discoverKey.toString(), null);
//    try {
//      config.save(file);
//    } catch (IOException e) {
//      throw new DataStorageException("Could not delete discovery info." + e);
//    }
//  }
//
//  @Override
//  public <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(
//      VisualizerType<T> type) {
//    HashedRegistry<T> registry = new HashedRegistry<>();
//    for (File file : Arrays.stream(pathVisualizerDir.listFiles())
//        .filter(file -> file.getName().matches(FILE_REGEX.pattern()))
//        .toList()
//    ) {
//      try {
//        NamespacedKey key = fromFileName(file.getName());
//        YamlConfiguration cfg =
//            visualizerHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));
//        if (!Objects.equals(cfg.get("type"), type.getKey().toString())) {
//          continue;
//        }
//        registry.put(loadVis(key, type, cfg));
//      } catch (Exception e) {
//        throw new DataStorageException("Could not load visualizer: " + file.getName(), e);
//      }
//    }
//    return registry;
//  }
//
//  private <T extends PathVisualizer<T, ?>> T loadVis(NamespacedKey key,
//                                                     VisualizerType<T> type,
//                                                     ConfigurationSection cfg) {
//    if (type == null) {
//      throw new IllegalStateException("Invalid visualizer type: " + cfg.getString("type"));
//    }
//    T vis = type.create(key, cfg.getString("display-name"));
//    Map<String, Object> values = (Map<String, Object>) cfg.get("props");
//    type.deserialize(vis, values == null ? new HashMap<>() : values);
//    return vis;
//  }
//
//  @Override
//  public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {
//    File file = new File(pathVisualizerDir, toFileName(visualizer.getKey()));
//    if (!file.exists()) {
//      try {
//        if (!file.createNewFile()) {
//          throw new DataStorageException("Could not create visualizer file.");
//        }
//      } catch (IOException e) {
//        throw new DataStorageException("Could not create visualizer file.", e);
//      }
//    }
//    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
//    cfg.set("type", visualizer.getType().getKey().toString());
//    cfg.set("display-name", visualizer.getNameFormat());
//    visualizer.getType().serialize(visualizer).forEach(cfg::set);
//
//    try {
//      cfg.save(file);
//    } catch (IOException e) {
//      throw new DataStorageException("Could not save visualizer file.", e);
//    }
//    visualizerHandles.put(visualizer.getKey(), cfg);
//  }
//
//  @Override
//  public void deletePathVisualizer(PathVisualizer<?, ?, ?> visualizer) {
//    File file = new File(pathVisualizerDir, toFileName(visualizer.getKey()));
//    file.deleteOnExit();
//  }
//
//  @Override
//  public CompletableFuture<Waypoint> createNodeInStorage(NodeType.NodeCreationContext context) {
//    return workOnFile(new File(dataDirectory, FILE_NODES), cfg -> {
//      UUID id = UUID.randomUUID();
//      cfg.set(id + ".location", context.location());
//      Waypoint waypoint = new Waypoint(id);
//      waypoint.setLocation(context.location());
//      return waypoint;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Waypoint> getNodeFromStorage(UUID id) {
//    return workOnFile(new File(dataDirectory, FILE_NODES), cfg -> {
//      Location location = cfg.getLocation("id.location");
//      Waypoint waypoint = new Waypoint(id);
//      waypoint.setLocation(location);
//      return waypoint;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<Waypoint>> getNodesFromStorage() {
//    return workOnFile(new File(dataDirectory, FILE_NODES), cfg -> {
//      Collection<Waypoint> waypoints = new HashSet<>();
//      for (String idString : cfg.getKeys(false)) {
//        Location location = cfg.getLocation("id.location");
//        Waypoint waypoint = new Waypoint(UUID.fromString(idString));
//        waypoint.setLocation(location);
//      }
//      return waypoints;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<Waypoint>> getNodesFromStorage(NodeSelection ids) {
//    return workOnFile(new File(dataDirectory, FILE_NODES), cfg -> {
//      Collection<String> idStrings = ids.stream().map(Objects::toString).toList();
//      Collection<Waypoint> waypoints = new HashSet<>();
//      for (String idString : cfg.getKeys(false)) {
//        if (!idStrings.contains(idString)) {
//          continue;
//        }
//        Location location = cfg.getLocation("id.location");
//        Waypoint waypoint = new Waypoint(UUID.fromString(idString));
//        waypoint.setLocation(location);
//      }
//      return waypoints;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> updateNodeInStorage(UUID nodeId, Consumer<Waypoint> nodeConsumer) {
//    return getNodeFromStorage(nodeId).thenAccept(waypoint -> {
//      workOnFile(new File(dataDirectory, FILE_NODES), cfg -> {
//        nodeConsumer.accept(waypoint);
//        cfg.set(nodeId + ".location", waypoint.getLocation());
//      });
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> updateNodesInStorage(NodeSelection nodeIds,
//                                                      Consumer<Waypoint> nodeConsumer) {
//    return workOnFile(new File(dataDirectory, FILE_NODES), cfg -> {
//      for (UUID nodeId : nodeIds) {
//        Waypoint waypoint = getNodeFromStorage(nodeId).join();
//        nodeConsumer.accept(waypoint);
//        cfg.set(nodeId + ".location", waypoint.getLocation());
//      }
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> deleteNodesFromStorage(NodeSelection nodes) {
//    return workOnFile(new File(dataDirectory, FILE_NODES), cfg -> {
//      for (UUID node : nodes) {
//        cfg.set(node.toString(), null);
//      }
//    });
//  }

	@Override
	public void init() throws Exception {
		if (!dataDirectory.exists()) {
			dataDirectory.mkdirs();
		}
		this.nodeGroupDir = new File(dataDirectory, DIR_NG);
		this.nodeGroupDir.mkdirs();
		this.pathVisualizerDir = new File(dataDirectory, DIR_PV);
		this.pathVisualizerDir.mkdirs();
		this.userDir = new File(dataDirectory, DIR_USER);
		this.userDir.mkdirs();
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void saveNodeType(UUID node, NodeType<? extends Node<?>> type) {
		workOnFile(fileTypes(), cfg -> {
			cfg.set(node.toString(), type.getKey().toString());
		});
	}

	@Override
	public void saveNodeTypes(Map<UUID, NodeType<? extends Node<?>>> typeMapping) {
		workOnFile(fileTypes(), cfg -> {
			typeMapping.forEach((uuid, nodeType) -> cfg.set(uuid.toString(), nodeType.getKey().toString()));
		});
	}

	@Override
	public <N extends Node<N>> Optional<NodeType<N>> loadNodeType(UUID node) {
		return workOnFile(fileTypes(), cfg -> {
			String keyString = cfg.getString(node.toString());
			if (keyString == null) {
				return Optional.empty();
			}
			NamespacedKey key = NamespacedKey.fromString(keyString);
			return Optional.ofNullable(nodeTypeRegistry.getType(key));
		});
	}

	@Override
	public Map<UUID, NodeType<? extends Node<?>>> loadNodeTypes(Collection<UUID> nodes) {
		return workOnFile(fileTypes(), cfg -> {
			Map<UUID, NodeType<?>> types = new HashMap<>();
			for (UUID node : nodes) {
				String keyString = cfg.getString(node.toString());
				if (keyString != null) {
					types.put(node, nodeTypeRegistry.getType(NamespacedKey.fromString(keyString)));
				}
			}
			return types;
		});
	}

	@Override
	public Edge createAndLoadEdge(UUID start, UUID end, double weight) {
		return null;
	}

	@Override
	public Collection<Edge> loadEdgesFrom(UUID start) {
		return null;
	}

	@Override
	public Collection<Edge> loadEdgesTo(UUID end) {
		return null;
	}

	@Override
	public Optional<Edge> loadEdge(UUID start, UUID end) {
		return Optional.empty();
	}

	@Override
	public void saveEdge(Edge edge) {

	}

	@Override
	public void deleteEdge(Edge edge) {

	}

	private Optional<NodeGroup> loadGroup(YamlConfiguration cfg) {
		try {
			NamespacedKey k = NamespacedKey.fromString(cfg.getString("key"));
			SimpleNodeGroup group = new SimpleNodeGroup(k);
			group.setWeight((float) cfg.getDouble("weight"));
			group.addAll(cfg.getStringList("nodes").stream()
					.map(UUID::fromString).toList());
			return Optional.of(group);
		} catch (Throwable t) {
			t.printStackTrace();
			return Optional.empty();
		}
	}

	private void writeGroup(NodeGroup group) {
		workOnFile(fileGroup(group.getKey()), cfg -> {
			cfg.set("key", group.getKey().toString());
			cfg.set("weight", group.getWeight());
			cfg.set("nodes", group.stream().map(UUID::toString).toList());
		});
	}

	@Override
	public NodeGroup createAndLoadGroup(NamespacedKey key) {
		SimpleNodeGroup group = new SimpleNodeGroup(key);
		writeGroup(group);
		return group;
	}

	@Override
	public Optional<NodeGroup> loadGroup(NamespacedKey key) {
		return workOnFileIfExists(fileGroup(key), this::loadGroup).flatMap(g -> g);
	}

	@Override
	public Collection<NodeGroup> loadGroups(Collection<NamespacedKey> key) {
		return key.stream()
				.map(this::loadGroup)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	@Override
	public List<NodeGroup> loadGroups(Pagination pagination) {
		return null;
	}

	@Override
	public Collection<NodeGroup> loadGroups(UUID node) {
		return loadAllGroups().stream()
				.filter(g -> g.contains(node))
				.collect(Collectors.toSet());
	}

	@Override
	public <M extends Modifier> Collection<NodeGroup> loadGroups(Class<M> modifier) {
		return null;
	}

	@Override
	public Collection<NodeGroup> loadAllGroups() {
		return Arrays.stream(new File(dataDirectory, DIR_NG).listFiles())
				.map(f -> workOnFile(f, cfg -> {
					return loadGroup(cfg);
				}))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	@Override
	public Collection<UUID> loadGroupNodes(NodeGroup group) {
		// nodes were already loaded with the group.
		return group;
	}

	@Override
	public void saveGroup(NodeGroup group) {
		writeGroup(group);
	}

	@Override
	public void deleteGroup(NodeGroup group) {
		fileGroup(group.getKey()).delete();
	}

	@Override
	public void assignToGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {

	}

	@Override
	public void unassignFromGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {

	}

	@Override
	public DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time) {
		return null;
	}

	@Override
	public Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key) {
		return Optional.empty();
	}

	@Override
	public void deleteDiscoverInfo(DiscoverInfo info) {

	}

	@Override
	public <T extends PathVisualizer<T, ?, ?>> T createAndLoadVisualizer(VisualizerType<T> type, NamespacedKey key) {
		return null;
	}

	@Override
	public <T extends PathVisualizer<T, ?, ?>> Map<NamespacedKey, T> loadVisualizers(VisualizerType<T> type) {
		return null;
	}

	@Override
	public <T extends PathVisualizer<T, ?, ?>> Optional<T> loadVisualizer(VisualizerType<T> type, NamespacedKey key) {
		return Optional.empty();
	}

	@Override
	public void saveVisualizer(PathVisualizer<?, ?, ?> visualizer) {

	}

	@Override
	public void deleteVisualizer(PathVisualizer<?, ?, ?> visualizer) {

	}

	private Optional<Waypoint> loadWaypoint(YamlConfiguration cfg, UUID id) {
		NodeType<Waypoint> type = ((de.cubbossa.pathfinder.node.NodeTypeRegistry) nodeTypeRegistry).getWaypointNodeType();

		ConfigurationSection sec = cfg.getConfigurationSection(id.toString());
		if (sec == null) {
			return Optional.empty();
		}
		double x = sec.getDouble("x");
		double y = sec.getDouble("y");
		double z = sec.getDouble("z");
		UUID world = UUID.fromString(sec.getString("world"));
		de.cubbossa.pathapi.misc.Location location = new de.cubbossa.pathapi.misc.Location(x, y, z, new WorldImpl(world));

		Waypoint waypoint = new Waypoint(type, id);
		waypoint.setLocation(location);

		return Optional.of(waypoint);
	}

	@Override
	public Waypoint createAndLoadWaypoint(Location location) {
		return null;
	}

	@Override
	public Optional<Waypoint> loadWaypoint(UUID uuid) {
		return workOnFile(fileWaypoints(), cfg -> {
			return loadWaypoint(cfg, uuid);
		});
	}

	@Override
	public Collection<Waypoint> loadWaypoints(Collection<UUID> ids) {
		return workOnFile(fileWaypoints(), cfg -> {
			return ids.stream()
					.map(uuid -> loadWaypoint(cfg, uuid))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toSet());
		});
	}

	@Override
	public Collection<Waypoint> loadAllWaypoints() {
		return workOnFile(fileWaypoints(), cfg -> {
			return loadWaypoints(cfg.getKeys(false).stream()
					.map(UUID::fromString).collect(Collectors.toSet()));
		});
	}

	@Override
	public void saveWaypoint(Waypoint node) {

	}

	@Override
	public void deleteWaypoints(Collection<Waypoint> waypoints) {
		workOnFile(fileWaypoints(), cfg -> {
			waypoints.forEach(waypoint -> cfg.set(waypoint.getNodeId().toString(), null));
		});
	}

	public record Meta(
      boolean oneFileForAllUsers
  ) {
  }
}
