package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YmlDataStorage implements DataStorage {

  private static final String FILE_NODES = "nodes.yml";
  private static final String FILE_EDGES = "edges.yml";
  private static final String DIR_NG = "nodegroups";
  private static final String DIR_PV = "path_visualizer";
  private static final String DIR_USER = "users";
  private static final Pattern FILE_REGEX = Pattern.compile("[a-zA-Z0-9_]+\\$[a-zA-Z0-9_]+\\.yml");
  private final Meta meta = new Meta(true);
  private final Map<NamespacedKey, YamlConfiguration> visualizerHandles;
  private final File dataDirectory;
  private File nodeGroupDir;
  private File pathVisualizerDir;
  private File userDir;

  public YmlDataStorage(File dataDirectory) {
    if (!dataDirectory.isDirectory()) {
      throw new IllegalArgumentException("Data directory must be a directory!");
    }
    this.dataDirectory = dataDirectory;
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

  @Override
  public void connect(Runnable initial) {
    if (!dataDirectory.exists()) {
      dataDirectory.mkdirs();
      initial.run();
    }
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

  private CompletableFuture<Void> workOnFile(File file, Consumer<YamlConfiguration> editor) {
    return workOnFile(file, cfg -> {
      editor.accept(cfg);
      return null;
    });
  }

  private <T> CompletableFuture<T> workOnFile(File file, Function<YamlConfiguration, T> editor) {
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
      return CompletableFuture.failedFuture(e);
    }
    return CompletableFuture.completedFuture(data);
  }

  @Override
  public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
      cfg.set(start.toString() + "." + end.toString(), weight);
      return new Edge(start, end, (float) weight);
    });
  }

  @Override
  public CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end) {
    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
      Collection<Edge> edges = new HashSet<>();
      for (UUID startId : start) {
        for (UUID endId : end) {
          cfg.set(startId.toString() + "." + endId.toString(), 1);
          edges.add(new Edge(startId, endId, 1));
        }
      }
      return edges;
    });
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start) {
    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
      for (UUID uuid : start) {
        cfg.set(uuid.toString(), null);
      }
    });
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
      for (UUID startId : start) {
        for (UUID endId : end) {
          cfg.set(startId.toString() + "." + endId.toString(), null);
        }
      }
    });
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnections(UUID start) {
    return workOnFile(new File(dataDirectory, FILE_EDGES), cfg -> {
      ConfigurationSection nodeSec = cfg.getConfigurationSection(start.toString());
      if (nodeSec == null) {
        return new HashSet<>();
      }
      Collection<Edge> edges = new HashSet<>();
      for (String to : nodeSec.getKeys(false)) {
        edges.add(new Edge(start, UUID.fromString(to), (float) nodeSec.getDouble(to)));
      }
      return edges;
    });
  }

  @Override
  public CompletableFuture<Collection<UUID>> getNodeGroupNodes(NamespacedKey group) {
    return workOnFile(new File(nodeGroupDir, toFileName(group)), cfg -> {
      return cfg.getStringList("nodes").stream().map(UUID::fromString).toList();
    });
  }

  @Override
  public CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet() {
    return CompletableFuture.completedFuture(Arrays.stream(nodeGroupDir.listFiles())
        .map(File::getName)
        .map(this::fromFileName)
        .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<NodeGroup> getNodeGroup(NamespacedKey key) {
    return workOnFile(new File(nodeGroupDir, toFileName(key)), cfg -> {
      NodeGroup group = new NodeGroup(key);
      group.setWeight(cfg.getDouble("weight"));
      cfg.getStringList("nodes").stream()
          .map(UUID::fromString)
          .forEach(group::add);

      //TODO assign modifiers

      return group;
    });
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> getNodeGroups() {
    return getNodeGroups(new Pagination(0, Integer.MAX_VALUE)).thenApply(n -> n);
  }

  @Override
  public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
    List<File> fileList = Arrays.asList(nodeGroupDir.listFiles()).subList(pagination.offset(), pagination.offset() + pagination.limit());
    return CompletableFuture.completedFuture(fileList.stream()
        .parallel()
        .map(File::getName)
        .map(this::fromFileName)
        .map(this::getNodeGroup)
        .map(CompletableFuture::join)
        .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
    File file = new File(nodeGroupDir, toFileName(key));
    if (file.exists()) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Group with this key already exists."));
    }
    return workOnFile(file, cfg -> {
      cfg.set("key", key.toString());
      cfg.set("weight", 1);
      return new NodeGroup(key);
    });
  }

  @Override
  public CompletableFuture<Void> updateNodeGroup(NamespacedKey group, Consumer<NodeGroup> modifier) {
    return workOnFile(new File(nodeGroupDir, toFileName(group)), cfg -> {
      NodeGroup g = getNodeGroup(group).join();
      modifier.accept(g);
      cfg.set("weight", g.getWeight());
      cfg.set("nodes", g.stream().map(UUID::toString).toList());
      // TODO modifiers
    });
  }

  @Override
  public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
    new File(nodeGroupDir, toFileName(key)).delete();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public DiscoverInfo createDiscoverInfo(UUID playerId, NodeGroup discoverable, LocalDateTime foundDate) {
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
    discoveries.set(discoverable.getKey() + ".date", foundDate);
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
      map.put(nkey, new DiscoverInfo(playerId, nkey, (LocalDateTime) discoveries.get(key + ".date")));
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
  public <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(
      VisualizerType<T> type) {
    HashedRegistry<T> registry = new HashedRegistry<>();
    for (File file : Arrays.stream(pathVisualizerDir.listFiles())
        .filter(file -> file.getName().matches(FILE_REGEX.pattern()))
        .toList()
    ) {
      try {
        NamespacedKey key = fromFileName(file.getName());
        YamlConfiguration cfg =
            visualizerHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));
        if (!Objects.equals(cfg.get("type"), type.getKey().toString())) {
          continue;
        }
        registry.put(loadVis(key, type, cfg));
      } catch (Exception e) {
        throw new DataStorageException("Could not load visualizer: " + file.getName(), e);
      }
    }
    return registry;
  }

  private <T extends PathVisualizer<T, ?>> T loadVis(NamespacedKey key,
                                                     VisualizerType<T> type,
                                                     ConfigurationSection cfg) {
    if (type == null) {
      throw new IllegalStateException("Invalid visualizer type: " + cfg.getString("type"));
    }
    T vis = type.create(key, cfg.getString("display-name"));
    Map<String, Object> values = (Map<String, Object>) cfg.get("props");
    type.deserialize(vis, values == null ? new HashMap<>() : values);
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

  public record Meta(
      boolean oneFileForAllUsers
  ) {
  }
}
