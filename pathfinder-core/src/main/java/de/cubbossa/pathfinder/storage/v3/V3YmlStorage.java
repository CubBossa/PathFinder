package de.cubbossa.pathfinder.storage.v3;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.DataStorageException;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class V3YmlStorage implements V3Storage {

  private static final String DIR_RM = "roadmaps";
  private static final String DIR_NG = "nodegroups";
  private static final String DIR_PV = "path_visualizer";
  private static final String DIR_USER = "users";
  private static final Pattern FILE_REGEX = Pattern.compile("[a-zA-Z0-9_]+\\$[a-zA-Z0-9_]+\\.yml");
  private final Meta meta = new Meta(true);
  private final Map<NamespacedKey, YamlConfiguration> roadmapHandles;
  private final Map<NamespacedKey, YamlConfiguration> nodeGroupHandles;
  private final Map<NamespacedKey, YamlConfiguration> visualizerHandles;
  private final File dataDirectory;
  private File roadMapDir;
  private File nodeGroupDir;
  private File pathVisualizerDir;
  private File userDir;

  public V3YmlStorage(File dataDirectory) {
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
      return;
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
  public Collection<V3RoadMap> loadRoadmaps() {
    Collection<V3RoadMap> registry = new HashSet<>();

    for (File file : Arrays.stream(roadMapDir.listFiles())
        .filter(file -> file.getName().matches(FILE_REGEX.pattern())).toList()) {
      try {
        NamespacedKey key = fromFileName(file.getName());
        YamlConfiguration cfg =
            roadmapHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

        String visKeyString = cfg.getString("path-visualizer");
        registry.add(new V3RoadMap(key,
            cfg.getString("name-format"),
            NamespacedKey.fromString(visKeyString),
            cfg.getDouble("curve-length"))
        );
      } catch (Exception e) {
        throw new DataStorageException("Could not load roadmap: " + file.getName(), e);
      }
    }
    return registry;
  }

  @Override
  public Collection<V3Edge> loadEdges() {
    return Arrays.stream(Objects.requireNonNull(roadMapDir.listFiles()))
        .map(File::getName)
        .map(this::fromFileName)
        .map(this::loadEdges)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private Collection<V3Edge> loadEdges(NamespacedKey roadMap) {
    YamlConfiguration cfg = roadmapHandles.get(roadMap);
    if (cfg == null) {
      throw new DataStorageException("Tried to load edges for non existing roadmap");
    }
    Collection<V3Edge> result = new HashSet<>();
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
        result.add(new V3Edge(start, Integer.parseInt(innerKey), (float) weight));
      }
    }
    return result;
  }

  @Override
  public Collection<V3Node> loadNodes() {
    return Arrays.stream(Objects.requireNonNull(roadMapDir.listFiles()))
        .map(File::getName)
        .map(this::fromFileName)
        .map(this::loadNodes)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private Collection<V3Node> loadNodes(NamespacedKey roadMap) {
    YamlConfiguration cfg = roadmapHandles.get(roadMap);
    if (cfg == null) {
      throw new DataStorageException("Tried to load nodes for non existing roadmap");
    }
    Collection<V3Node> result = new HashSet<>();
    ConfigurationSection nodeSection = cfg.getConfigurationSection("nodes");
    if (nodeSection == null) {
      return result;
    }
    for (String key : nodeSection.getKeys(false)) {
      ConfigurationSection innerSection = nodeSection.getConfigurationSection(key);
      if (innerSection == null) {
        continue;
      }
      int id = Integer.parseInt(key);
      Location location = innerSection.getLocation("location");
      var node = new V3Node(
          id, NamespacedKey.fromString("pathfinder:waypoint"), roadMap,
          location.getX(), location.getY(), location.getZ(), location.getWorld().getUID(),
          innerSection.getDouble("curve-length")
      );
      result.add(node);
    }
    return result;
  }

  @Override
  public Collection<V3GroupNode> loadGroupNodes() {
    Collection<V3GroupNode> data = new HashSet<>();
    for (File file : Arrays.stream(nodeGroupDir.listFiles())
        .filter(file -> file.getName().matches(FILE_REGEX.pattern()))
        .collect(Collectors.toList())) {
      try {
        NamespacedKey key = fromFileName(file.getName());
        YamlConfiguration cfg =
            nodeGroupHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

        List<Integer> nodes = (List<Integer>) cfg.getList("nodes");
        if (nodes != null) {
          for (Integer node : nodes) {
            data.add(new V3GroupNode(node, key));
          }
        }
      } catch (Exception e) {
        throw new DataStorageException("Could not load nodegroup: " + file.getName(), e);
      }
    }
    return data;
  }

  @Override
  public Collection<V3NodeGroup> loadNodeGroups() {
    Collection<V3NodeGroup> registry = new HashSet<>();
    for (File file : Arrays.stream(nodeGroupDir.listFiles())
        .filter(file -> file.getName().matches(FILE_REGEX.pattern()))
        .collect(Collectors.toList())) {
      try {
        NamespacedKey key = fromFileName(file.getName());
        YamlConfiguration cfg =
            nodeGroupHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

        String nameFormat = cfg.getString("name-format");
        V3NodeGroup group = new V3NodeGroup(
            key, nameFormat, cfg.getString("permission"),
            cfg.getBoolean("navigable"),
            cfg.getBoolean("discoverable"),
            cfg.getDouble("find-distance")
        );
        registry.add(group);
      } catch (Exception e) {
        throw new DataStorageException("Could not load nodegroup: " + file.getName(), e);
      }
    }
    return registry;
  }

  @Override
  public Collection<V3SearchTerm> loadSearchTerms() {
    Collection<V3SearchTerm> registry = new HashSet<>();
    for (File file : Arrays.stream(nodeGroupDir.listFiles())
        .filter(file -> file.getName().matches(FILE_REGEX.pattern()))
        .collect(Collectors.toList())) {
      try {
        NamespacedKey key = fromFileName(file.getName());
        YamlConfiguration cfg = nodeGroupHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

        V3SearchTerm term = new V3SearchTerm(key, cfg.getString("search-terms"));
        registry.add(term);
      } catch (Exception e) {
        throw new DataStorageException("Could not load nodegroup: " + file.getName(), e);
      }
    }
    return registry;
  }

  @Override
  public Collection<V3Discovering> loadDiscoverings() {
    File file;
    Map<String, ConfigurationSection> cfgs = new HashMap<>();
    if (meta.oneFileForAllUsers()) {
      file = new File(userDir, "user_data.yml");
      ConfigurationSection cfg = YamlConfiguration.loadConfiguration(file);
      cfg.getKeys(false).forEach(s -> {
        cfgs.put(s, cfg.getConfigurationSection(s));
      });
    } else {
      Arrays.stream(Objects.requireNonNull(userDir.listFiles()))
          .forEach(f -> {
            cfgs.put(f.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(f));
          });
    }
    Collection<V3Discovering> discoverings = new HashSet<>();
    for (var e : cfgs.entrySet()) {
      ConfigurationSection cfg = e.getValue();
      ConfigurationSection discoveries = cfg.getConfigurationSection("discoveries");
      if (discoveries == null) {
        discoveries = cfg.createSection("discoveries");
      }
      for (String key : discoveries.getKeys(false)) {
        NamespacedKey nkey = NamespacedKey.fromString(key);
        discoverings.add(new V3Discovering(UUID.fromString(e.getKey()), nkey, (LocalDateTime) discoveries.get(key + ".date")));
      }
    }
    return discoverings;
  }

  @Override
  public Collection<V3Visualizer> loadVisualizers() {
    Collection<V3Visualizer> registry = new HashSet<>();
    for (File file : Arrays.stream(pathVisualizerDir.listFiles())
        .filter(file -> file.getName().matches(FILE_REGEX.pattern()))
        .toList()
    ) {
      try {
        NamespacedKey key = fromFileName(file.getName());
        YamlConfiguration cfg = visualizerHandles.computeIfAbsent(key, k -> YamlConfiguration.loadConfiguration(file));

        registry.add(new V3Visualizer(
            key, NamespacedKey.fromString(cfg.getString("type")),
            cfg.getString("display-name"),
            cfg.getString("permission"),
            cfg.getInt("interval"),
            cfg.saveToString()
        ));
      } catch (Exception e) {
        throw new DataStorageException("Could not load visualizer: " + file.getName(), e);
      }
    }
    return registry;
  }

  public record Meta(
      boolean oneFileForAllUsers
  ) {
  }
}