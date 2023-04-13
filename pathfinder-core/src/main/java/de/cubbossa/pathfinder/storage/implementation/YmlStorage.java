//package de.cubbossa.pathfinder.storage.implementation;
//
//import de.cubbossa.pathfinder.api.group.Modifier;
//import de.cubbossa.pathfinder.core.node.Edge;
//import de.cubbossa.pathfinder.api.node.Node;
//import de.cubbossa.pathfinder.core.node.NodeType;
//import de.cubbossa.pathfinder.api.node.NodeTypeRegistry;
//import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
//import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
//import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
//import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
//import de.cubbossa.pathfinder.storage.DataStorageException;
//import de.cubbossa.pathfinder.storage.DiscoverInfo;
//import de.cubbossa.pathfinder.storage.Storage;
//import de.cubbossa.pathfinder.util.HashedRegistry;
//import de.cubbossa.pathfinder.util.NodeSelection;
//import java.io.File;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//import lombok.Getter;
//import org.bukkit.Location;
//import org.bukkit.NamespacedKey;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.configuration.file.YamlConfiguration;
//
//public class YmlStorage implements Storage {
//
//  private static final String FILE_TYPES = "node_types.yml";
//  private static final String FILE_NODES = "waypoints.yml";
//  private static final String FILE_EDGES = "edges.yml";
//  private static final String DIR_NG = "nodegroups";
//  private static final String DIR_PV = "path_visualizer";
//  private static final String DIR_USER = "users";
//  private static final Pattern FILE_REGEX = Pattern.compile("[a-zA-Z0-9_]+\\$[a-zA-Z0-9_]+\\.yml");
//  private final Meta meta = new Meta(true);
//  private final Map<NamespacedKey, YamlConfiguration> visualizerHandles;
//  @Getter
//  private final NodeTypeRegistry nodeTypeRegistry;
//  private final File dataDirectory;
//  private File nodeGroupDir;
//  private File pathVisualizerDir;
//  private File userDir;
//
//  public YmlStorage(File dataDirectory, NodeTypeRegistry nodeTypeRegistry) {
//    if (!dataDirectory.isDirectory()) {
//      throw new IllegalArgumentException("Data directory must be a directory!");
//    }
//    this.dataDirectory = dataDirectory;
//    this.visualizerHandles = new HashMap<>();
//    this.nodeTypeRegistry = nodeTypeRegistry;
//  }
//
//  public String toFileName(NamespacedKey key) {
//    return key.toString().replace(':', '$') + ".yml";
//  }
//
//  public NamespacedKey fromFileName(String name) {
//    if (name.endsWith(".yml")) {
//      name = name.substring(0, name.length() - 4);
//    }
//    return NamespacedKey.fromString(name.replace('$', ':'));
//  }
//
//  @Override
//  public void connect(Runnable initial) {
//    if (!dataDirectory.exists()) {
//      dataDirectory.mkdirs();
//      initial.run();
//    }
//    this.nodeGroupDir = new File(dataDirectory, DIR_NG);
//    this.nodeGroupDir.mkdirs();
//    this.pathVisualizerDir = new File(dataDirectory, DIR_PV);
//    this.pathVisualizerDir.mkdirs();
//    this.userDir = new File(dataDirectory, DIR_USER);
//    this.userDir.mkdirs();
//  }
//
//  @Override
//  public void disconnect() {
//  }
//
//  private CompletableFuture<Void> workOnFile(File file, Consumer<YamlConfiguration> editor) {
//    return workOnFile(file, cfg -> {
//      editor.accept(cfg);
//      return null;
//    });
//  }
//
//  private <T> CompletableFuture<T> workOnFile(File file, Function<YamlConfiguration, T> editor) {
//    if (!file.exists()) {
//      try {
//        file.createNewFile();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
//    T data = editor.apply(cfg);
//    try {
//      cfg.save(file);
//    } catch (IOException e) {
//      return CompletableFuture.failedFuture(e);
//    }
//    return CompletableFuture.completedFuture(data);
//  }
//
//  @Override
//  public CompletableFuture<NodeType<?>> getNodeType(UUID nodeId) {
//    return workOnFile(new File(dataDirectory, FILE_TYPES), cfg -> {
//      String typeString = cfg.getString(nodeId.toString());
//      if (typeString == null) {
//        throw new IllegalArgumentException("Could not find type for given UUID.");
//      }
//      NamespacedKey typeKey = NamespacedKey.fromString(typeString);
//      NodeType<?> type = nodeTypeRegistry.getNodeType(typeKey);
//      if (type == null) {
//        throw new IllegalArgumentException("Could not find type for given UUID.");
//      }
//      return type;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> setNodeType(UUID nodeId, NamespacedKey nodeType) {
//    return workOnFile(new File(dataDirectory, FILE_TYPES), cfg -> {
//      cfg.set(nodeId.toString(), nodeType.toString());
//    });
//  }
//
//  @Override
//  public CompletableFuture<Node<?>> getNode(UUID uuid) {
//    return getNodeType(uuid).thenApply(nodeType -> nodeType.getNodeFromStorage(uuid).join());
//  }
//
//  @Override
//  public CompletableFuture<Collection<NamespacedKey>> getNodeGroups(UUID node) {
//    return getNodeGroupKeySet().thenApply(nodeGroups -> {
//      Collection<CompletableFuture<?>> futures = new ArrayList<>();
//      Collection<NamespacedKey> groups = new ArrayList<>();
//      for (NamespacedKey nodeGroup : nodeGroups) {
//        futures.add(workOnFile(new File(dataDirectory, toFileName(nodeGroup)), cfg -> {
//          if (cfg.getStringList("nodes").contains(node.toString())) {
//            groups.add(nodeGroup);
//          }
//        }));
//      }
//      return CompletableFuture
//          .allOf(futures.toArray(CompletableFuture[]::new))
//          .thenApply(u -> groups)
//          .join();
//    });
//  }
//
//  @Override
//  public <N extends Node<N>> CompletableFuture<N> createNode(NodeType<N> type, Location location) {
//    return Storage.super.createNode(type, location).thenApply(n -> {
//      workOnFile(new File(dataDirectory, FILE_TYPES), cfg -> {
//        cfg.set(n.getNodeId().toString(), n.getType().getKey().toString());
//      }).join();
//      return n;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> deleteNodes(NodeSelection nodes) {
//    return Storage.super.deleteNodes(nodes).thenRun(() -> {
//      workOnFile(new File(dataDirectory, FILE_TYPES), cfg -> {
//        for (UUID n : nodes) {
//          cfg.set(n.toString(), null);
//        }
//      }).join();
//    });
//  }
//
//  @Override
//  public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
//    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
//      cfg.set(start.toString() + "." + end.toString(), weight);
//      return new Edge(start, end, (float) weight);
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end) {
//    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
//      Collection<Edge> edges = new HashSet<>();
//      for (UUID startId : start) {
//        for (UUID endId : end) {
//          cfg.set(startId.toString() + "." + endId.toString(), 1);
//          edges.add(new Edge(startId, endId, 1));
//        }
//      }
//      return edges;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> disconnectNodes(NodeSelection start) {
//    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
//      for (UUID uuid : start) {
//        cfg.set(uuid.toString(), null);
//      }
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
//    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
//      for (UUID startId : start) {
//        for (UUID endId : end) {
//          cfg.set(startId.toString() + "." + endId.toString(), null);
//        }
//      }
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<Edge>> getConnections(UUID start) {
//    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
//      ConfigurationSection nodeSec = cfg.getConfigurationSection(start.toString());
//      if (nodeSec == null) {
//        return new HashSet<>();
//      }
//      Collection<Edge> edges = new HashSet<>();
//      for (String to : nodeSec.getKeys(false)) {
//        edges.add(new Edge(start, UUID.fromString(to), (float) nodeSec.getDouble(to)));
//      }
//      return edges;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<Edge>> getConnectionsTo(UUID end) {
//    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
//      String idString = end.toString();
//      Collection<Edge> result = new HashSet<>();
//      for (String start : cfg.getKeys(false)) {
//        if (cfg.getConfigurationSection(start) != null && cfg.isSet(start + "." + idString)) {
//          result.add(new Edge(UUID.fromString(start), end,
//              (float) cfg.getDouble(start + "." + idString)));
//        }
//      }
//      return result;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<Edge>> getConnectionsTo(NodeSelection ends) {
//    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
//      Collection<Edge> result = new HashSet<>();
//      for (String start : cfg.getKeys(false)) {
//        for (UUID end : ends) {
//          String idString = end.toString();
//          if (cfg.getConfigurationSection(start) != null && cfg.isSet(start + "." + idString)) {
//            result.add(new Edge(UUID.fromString(start), end,
//                (float) cfg.getDouble(start + "." + idString)));
//          }
//        }
//      }
//      return result;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<UUID>> getNodeGroupNodes(NamespacedKey group) {
//    return workOnFile(new File(nodeGroupDir, toFileName(group)), cfg -> {
//      return cfg.getStringList("nodes").stream().map(UUID::fromString).toList();
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> clearNodeGroups(NodeSelection selection) {
//    return null;
//  }
//
//  @Override
//  public CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet() {
//    return CompletableFuture.completedFuture(Arrays.stream(nodeGroupDir.listFiles())
//        .map(File::getName)
//        .map(this::fromFileName)
//        .collect(Collectors.toList()));
//  }
//
//  @Override
//  public CompletableFuture<NodeGroup> getNodeGroup(NamespacedKey key) {
//    return workOnFile(new File(nodeGroupDir, toFileName(key)), cfg -> {
//      NodeGroup group = new NodeGroup(key);
//      group.setWeight(cfg.getDouble("weight"));
//      cfg.getStringList("nodes").stream()
//          .map(UUID::fromString)
//          .forEach(group::add);
//
//      //TODO assign modifiers
//
//      return group;
//    });
//  }
//
//  @Override
//  public CompletableFuture<Collection<NodeGroup>> getNodeGroups() {
//    return getNodeGroups(new Pagination(0, Integer.MAX_VALUE)).thenApply(n -> n);
//  }
//
//  @Override
//  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> getNodeGroups(Class<M> modifier) {
//    return getNodeGroups().thenApply(nodeGroups -> {
//      return nodeGroups.stream().filter(g -> g.hasModifier(modifier)).collect(Collectors.toList());
//    });
//  }
//
//  @Override
//  public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
//    List<File> fileList = Arrays.asList(nodeGroupDir.listFiles())
//        .subList(pagination.offset(), pagination.offset() + pagination.limit());
//    return CompletableFuture.completedFuture(fileList.stream()
//        .parallel()
//        .map(File::getName)
//        .map(this::fromFileName)
//        .map(this::getNodeGroup)
//        .map(CompletableFuture::join)
//        .collect(Collectors.toList()));
//  }
//
//  @Override
//  public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
//    File file = new File(nodeGroupDir, toFileName(key));
//    if (file.exists()) {
//      return CompletableFuture.failedFuture(
//          new IllegalArgumentException("Group with this key already exists."));
//    }
//    return workOnFile(file, cfg -> {
//      cfg.set("key", key.toString());
//      cfg.set("weight", 1);
//      return new NodeGroup(key);
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> updateNodeGroup(NamespacedKey group,
//                                                 Consumer<NodeGroup> modifier) {
//    return workOnFile(new File(nodeGroupDir, toFileName(group)), cfg -> {
//      NodeGroup g = getNodeGroup(group).join();
//      modifier.accept(g);
//      cfg.set("weight", g.getWeight());
//      cfg.set("nodes", g.stream().map(UUID::toString).toList());
//      // TODO modifiers
//    });
//  }
//
//  @Override
//  public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
//    new File(nodeGroupDir, toFileName(key)).delete();
//    return CompletableFuture.completedFuture(null);
//  }
//
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
//  public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
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
//
//  public record Meta(
//      boolean oneFileForAllUsers
//  ) {
//  }
//}
