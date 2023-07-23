package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Range;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApiStatus.Experimental
@Deprecated(forRemoval = true)
public abstract class YmlStorage extends CommonStorage {

  private static final String FILE_NODE_TYPES = "node_types.yml";
  private static final String FILE_VIS_TYPES = "visualizer_types.yml";
  private static final String FILE_NODES = "waypoints.yml";
  private static final String FILE_EDGES = "edges.yml";
  private static final String DIR_NG = "nodegroups";
  private static final String DIR_PV = "path_visualizer";
  private static final String DIR_USER = "users";
  private static final Pattern FILE_REGEX = Pattern.compile("[a-zA-Z0-9_]+\\$[a-zA-Z0-9_]+\\.yml");
  private final Meta meta = new Meta(true);
  private final File dataDirectory;
  @Getter
  @Setter
  private Logger logger;
  private File nodeGroupDir;
  private File pathVisualizerDir;
  private File userDir;

  public YmlStorage(File dataDirectory, NodeTypeRegistry nodeTypeRegistry,
                    VisualizerTypeRegistry visualizerTypeRegistry,
                    ModifierRegistry modifierRegistry) {
    super(nodeTypeRegistry, visualizerTypeRegistry, modifierRegistry);
    if (!dataDirectory.isDirectory()) {
      throw new IllegalArgumentException("Data directory must be a directory!");
    }
    this.dataDirectory = dataDirectory;
  }

  private File fileNodeTypes() {
    return new File(dataDirectory, FILE_NODE_TYPES);
  }

  private File fileVisualizerTypes() {
    return new File(dataDirectory, FILE_VIS_TYPES);
  }

  private File fileWaypoints() {
    return new File(dataDirectory, FILE_NODES);
  }

  private File fileEdges() {
    return new File(dataDirectory, FILE_EDGES);
  }

  private File fileGroup(NamespacedKey key) {
    return new File(nodeGroupDir, toFileName(key));
  }

  private File filePlayer(UUID id) {
    return new File(userDir, id + ".yml");
  }

  private File fileVisualizer(NamespacedKey visualizer) {
    return new File(pathVisualizerDir, toFileName(visualizer));
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
  public void saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping) {
    workOnFile(fileNodeTypes(), cfg -> {
      typeMapping.forEach((uuid, nodeType) -> cfg.set(uuid.toString(), nodeType.getKey().toString()));
    });
  }

  @Override
  public Map<UUID, NodeType<?>> loadNodeTypeMapping(Collection<UUID> nodes) {
    return workOnFile(fileNodeTypes(), cfg -> {
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
  public void deleteNodeTypeMapping(Collection<UUID> nodes) {
    workOnFile(fileNodeTypes(), cfg -> {
      nodes.forEach(n -> cfg.set(n.toString(), null));
    });
  }

  Optional<Edge> readEdge(UUID start, UUID end, ConfigurationSection startSection) {
    if (startSection == null) {
      return Optional.empty();
    }
    double weight = startSection.getDouble(end.toString());
    return Optional.of(new SimpleEdge(start, end, (float) weight));
  }

  void writeEdge(Edge edge, ConfigurationSection section) {
    section.set(edge.getEnd().toString(), edge.getWeight());
  }

  public Edge createAndLoadEdge(UUID start, UUID end, double weight) {
    return workOnFile(fileEdges(), cfg -> {
      SimpleEdge edge = new SimpleEdge(start, end, (float) weight);
      writeEdge(edge, cfg.createSection(start.toString()));
      return edge;
    });
  }

  public Collection<Edge> loadEdgesFrom(UUID start) {
    return workOnFile(fileEdges(), cfg -> {
      ConfigurationSection section = cfg.getConfigurationSection(start.toString());
      if (section == null) {
        return new HashSet<>();
      }
      return section.getKeys(false).stream()
          .map(end -> readEdge(start, UUID.fromString(end), section))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());
    });
  }

  public Collection<Edge> loadEdgesTo(UUID end) {
    return workOnFile(fileEdges(), cfg -> {
      Collection<Edge> edges = new HashSet<>();
      for (String startKey : cfg.getKeys(false)) {
        ConfigurationSection section = cfg.getConfigurationSection(startKey);
        if (section != null && section.contains(end.toString())) {
          readEdge(UUID.fromString(startKey), end, section).ifPresent(edges::add);
        }
      }
      return edges;
    });
  }

  public Optional<Edge> loadEdge(UUID start, UUID end) {
    return workOnFile(fileEdges(), cfg -> {
      return readEdge(start, end, cfg.getConfigurationSection(start.toString()));
    });
  }

  public void saveEdge(Edge edge) {
    workOnFile(fileEdges(), cfg -> {
      writeEdge(edge, cfg.createSection(edge.getStart().toString()));
    });
  }

  public void deleteEdge(Edge edge) {
    workOnFile(fileEdges(), cfg -> {
      ConfigurationSection start = cfg.getConfigurationSection(edge.getStart().toString());
      if (start != null) {
        start.set(edge.getEnd().toString(), null);
      }
    });
  }

  @Override
  public void deleteEdgesTo(Collection<UUID> end) {
    workOnFile(fileEdges(), cfg -> {
      for (String inner : cfg.getKeys(false)) {
        end.forEach(uuid -> cfg.set(inner + "." + uuid, null));
      }
    });
  }

  private Optional<NodeGroup> loadGroup(YamlConfiguration cfg) {
    try {
      if (!cfg.contains("key")) {
        return Optional.empty();
      }
      NamespacedKey k = NamespacedKey.fromString(cfg.getString("key"));
      SimpleNodeGroup group = new SimpleNodeGroup(k);
      group.setWeight((float) cfg.getDouble("weight"));
      group.addAll(cfg.getStringList("nodes").stream()
          .map(UUID::fromString).toList());

      ConfigurationSection modifiers = cfg.getConfigurationSection("modifier");
      if (modifiers != null) {
        for (String key : modifiers.getKeys(false)) {
          NamespacedKey namespacedKey;
          try {
            namespacedKey = NamespacedKey.fromString(key);
          } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Error while loading group.", t);
            continue;
          }
          Optional<ModifierType<Modifier>> type = modifierRegistry.getType(namespacedKey);
          if (type.isEmpty()) {
            logger.log(Level.WARNING, "Could not load modifier, no registered type by name '" + key + "'.");
            continue;
          }
          group.addModifier(type.get().deserialize(modifiers.getConfigurationSection(key).getValues(false)));
        }
      }

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
      cfg.set("modifier", null);
      group.getModifiers().forEach(modifier -> {
        Optional<ModifierType<Modifier>> type = modifierRegistry.getType(modifier.getKey());
        if (type.isEmpty()) {
          logger.log(Level.WARNING, "Could not store modifier of type '" + modifier.getClass() + "'.");
          return;
        }
        cfg.set("modifier." + modifier.getKey(), type.get().serialize(modifier));
      });
      cfg.set("nodes", group.stream().map(UUID::toString).toList());
      group.getModifierChanges().flush();
      group.getContentChanges().flush();
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
  public Collection<NodeGroup> loadGroupsByMod(Collection<NamespacedKey> key) {
    return key.stream()
        .map(this::loadGroup)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  @Override
  public List<NodeGroup> loadGroups(Range range) {
    return CollectionUtils.subList(Arrays.stream(new File(dataDirectory, DIR_NG).listFiles()).toList(), range)
        .stream()
        .map(f -> workOnFile(f, cfg -> {
          return loadGroup(cfg);
        }))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<NodeGroup> loadGroups(UUID node) {
    return loadAllGroups().stream()
        .filter(g -> g.contains(node))
        .collect(Collectors.toSet());
  }

  @Override
  public <M extends Modifier> Collection<NodeGroup> loadGroups(NamespacedKey modifier) {
    return loadAllGroups().stream()
        .filter(g -> g.hasModifier(modifier))
        .collect(Collectors.toSet());
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
  public DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                LocalDateTime time) {
    return workOnFile(filePlayer(player), cfg -> {
      DiscoverInfo info = new DiscoverInfo(player, key, time);
      cfg.set(info.discoverable().toString(), info.foundDate());
      return info;
    });
  }

  @Override
  public Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key) {
    return workOnFileIfExists(filePlayer(player),
        (Function<YamlConfiguration, Optional<DiscoverInfo>>) cfg -> {
          LocalDateTime time = cfg.getObject(key.toString(), LocalDateTime.class);
          if (time == null) {
            return Optional.empty();
          }
          return Optional.of(new DiscoverInfo(player, key, time));
        }).flatMap(Function.identity());
  }

  @Override
  public void deleteDiscoverInfo(DiscoverInfo info) {
    workOnFile(filePlayer(info.playerId()), cfg -> {
      cfg.set(info.discoverable().toString(), null);
    });
  }

  @Override
  public void saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types) {
    workOnFile(fileVisualizerTypes(), cfg -> {
      types.forEach((key, type) -> {
        cfg.set(key.toString(), type.getKey().toString());
      });
    });
  }

  @Override
  public Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    return workOnFile(fileVisualizerTypes(), cfg -> {
      Map<NamespacedKey, VisualizerType<?>> result = new HashMap<>();
      for (NamespacedKey key : keys) {
        try {
          if (!cfg.contains(key.toString())) {
            continue;
          }
          visualizerTypeRegistry.getType(NamespacedKey.fromString(cfg.getString(key.toString())))
              .ifPresent(type -> result.put(key, type));
        } catch (Throwable t) {
          throw new DataStorageException("Error while retrieving visualizer type for '" + key + "'.", t);
        }
      }
      return result;
    });
  }

  @Override
  public void deleteVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    workOnFile(fileVisualizerTypes(), cfg -> {
      keys.forEach(key -> cfg.set(key.toString(), null));
    });
  }

  private <VisualizerT extends PathVisualizer<?, ?>> void writeInternalVisualizer(VisualizerType<VisualizerT> type,
                                                                                  VisualizerT visualizer) {
    workOnFile(fileVisualizer(visualizer.getKey()), cfg -> {
      cfg.set("type", type.getKey().toString());
      type.serialize(visualizer).forEach(cfg::set);
    });
  }

  private <T extends PathVisualizer<?, ?>> T readInternalVisualizer(VisualizerType<T> type,
                                                                    NamespacedKey key,
                                                                    ConfigurationSection cfg) {
    if (type == null) {
      throw new IllegalStateException("Invalid visualizer type: " + cfg.getString("type"));
    }
    T vis = type.create(key);
    Map<String, Object> values = (Map<String, Object>) cfg.get("props");
    type.deserialize(vis, values == null ? new HashMap<>() : values);
    return vis;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> VisualizerT createAndLoadInternalVisualizer(
      VisualizerType<VisualizerT> type, NamespacedKey key) {
    VisualizerT visualizer = type.create(key);
    writeInternalVisualizer(type, visualizer);
    return visualizer;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> loadInternalVisualizer(
      VisualizerType<VisualizerT> type, NamespacedKey key) {
    return workOnFileIfExists(fileVisualizer(key), cfg -> readInternalVisualizer(type, key, cfg));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadInternalVisualizers(
      VisualizerType<VisualizerT> type) {
    Collection<NamespacedKey> keys = new HashSet<>();
    workOnFile(fileVisualizerTypes(), cfg -> {
      String typeString = type.getKey().toString();
      for (String key : cfg.getKeys(false)) {
        if (Objects.equals(cfg.getString(key), typeString)) {
          keys.add(NamespacedKey.fromString(key));
        }
      }
    });
    return keys.stream()
        .map(this::fileVisualizer)
        .map(file -> workOnFile(file, cfg -> {
          return readInternalVisualizer(type, fromFileName(file.getName()), cfg);
        }))
        .collect(Collectors.toMap(Keyed::getKey, Function.identity()));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void saveInternalVisualizer(VisualizerType<VisualizerT> type,
                                                                                VisualizerT visualizer) {
    writeInternalVisualizer(type, visualizer);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void deleteInternalVisualizer(VisualizerT visualizer) {
    File file = new File(pathVisualizerDir, toFileName(visualizer.getKey()));
    file.deleteOnExit();
  }

  /**
   * don't deal with edges and groups, are introduced by {@link de.cubbossa.pathapi.storage.Storage#loadNode(UUID)}
   */
  private Optional<Waypoint> readWaypoint(YamlConfiguration cfg, UUID id) {

    ConfigurationSection sec = cfg.getConfigurationSection(id.toString());
    if (sec == null) {
      return Optional.empty();
    }
    double x = sec.getDouble("x");
    double y = sec.getDouble("y");
    double z = sec.getDouble("z");
    UUID world = UUID.fromString(sec.getString("world"));
    de.cubbossa.pathapi.misc.Location location =
        new de.cubbossa.pathapi.misc.Location(x, y, z, worldLoader.loadWorld(world));

    Waypoint waypoint = new Waypoint(id);
    waypoint.setLocation(location);

    return Optional.of(waypoint);
  }

  private void writeWaypoint(Waypoint waypoint, ConfigurationSection cfg) {
    cfg.set("x", waypoint.getLocation().getX());
    cfg.set("y", waypoint.getLocation().getY());
    cfg.set("z", waypoint.getLocation().getZ());
    cfg.set("world", waypoint.getLocation().getWorld().getUniqueId().toString());
  }

  @Override
  public Waypoint createAndLoadWaypoint(Location location) {
    return workOnFile(fileWaypoints(), cfg -> {
      Waypoint waypoint = new Waypoint(UUID.randomUUID());
      waypoint.setLocation(location);
      writeWaypoint(waypoint, cfg.createSection(waypoint.getNodeId().toString()));
      return waypoint;
    });
  }

  @Override
  public Optional<Waypoint> loadWaypoint(UUID uuid) {
    return workOnFile(fileWaypoints(), cfg -> {
      return readWaypoint(cfg, uuid);
    });
  }

  @Override
  public Collection<Waypoint> loadWaypoints(Collection<UUID> ids) {
    return workOnFile(fileWaypoints(), cfg -> {
      return ids.stream()
          .map(uuid -> readWaypoint(cfg, uuid))
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
    workOnFile(fileWaypoints(), cfg -> {
      writeWaypoint(node, cfg.createSection(node.getNodeId().toString()));
    });
    for (Edge e : node.getEdgeChanges().getAddList()) {
      saveEdge(e);
    }
    for (Edge e : node.getEdgeChanges().getRemoveList()) {
      deleteEdge(e);
    }
    node.getEdgeChanges().flush();
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
