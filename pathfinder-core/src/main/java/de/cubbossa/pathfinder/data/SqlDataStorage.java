package de.cubbossa.pathfinder.data;

import static de.cubbossa.pathfinder.jooq.tables.PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderEdges.PATHFINDER_EDGES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroups.PATHFINDER_NODEGROUPS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodes.PATHFINDER_NODES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderRoadmaps.PATHFINDER_ROADMAPS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS;

import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderEdgesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderRoadmapsRecord;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.jooq.BatchBindStep;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

public abstract class SqlDataStorage implements DataStorage {

  abstract ConnectionProvider getConnectionProvider();

  private DSLContext create;

  // +-----------------------------------------+
  // |  Roadmap Table                          |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderRoadmapsRecord, RoadMap> roadmapMapper = record -> {
    String visKeyString = record.getPathVisualizer();;
    NamespacedKey visKey = visKeyString == null ? null : NamespacedKey.fromString(visKeyString);

    return new RoadMap(
        record.getKey(),
        record.getNameFormat(),
        visKey == null ? null : VisualizerHandler.getInstance().getPathVisualizer(visKey),
        record.getPathCurveLength()
    );
  };

  // +-----------------------------------------+
  // |  Node Table                             |
  // +-----------------------------------------+

  private final Function<RoadMap, RecordMapper<? super PathfinderNodesRecord, Waypoint>> nodeMapper = roadmap -> {
    final RoadMap rm = roadmap;
    return record -> {
      int id = record.getId();
      double x = record.getX();
      double y = record.getY();
      double z = record.getZ();
      UUID worldUid = UUID.fromString(record.getWorld());
      Double curveLength = record.getPathCurveLength();

      Waypoint node = new Waypoint(id, rm, true);
      node.setLocation(new Location(Bukkit.getWorld(worldUid), x, y, z));
      node.setCurveLength(curveLength);
      return node;
    };
  };

  // +-----------------------------------------+
  // |  Edge Table                             |
  // +-----------------------------------------+

  private final Function<Map<Integer, Node<?>>, RecordMapper<? super PathfinderEdgesRecord, Edge>> edgeMapper =
      map -> record -> {
        int startId = record.getStartId();
        int endId = record.getEndId();
        double weight = record.getWeightModifier();

        Node<?> start = map.get(startId);
        Node<?> end = map.get(endId);

        if (start == null || end == null) {
          deleteEdge(startId, endId);
        }
        return new Edge(start, end, (float) weight);
      };

  // +-----------------------------------------+
  // |  Nodegroup Table                        |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderNodegroupsRecord, NodeGroup> groupMapper = record -> {
    NamespacedKey key = record.getKey();
    String nameFormat = record.getNameFormat();
    String permission = record.getPermission();
    boolean navigable = record.getNavigable();
    boolean discoverable = record.getDiscoverable();
    double findDistance = record.getFindDistance();

    NodeGroup group = new NodeGroup(key, nameFormat);
    group.setPermission(permission);
    group.setNavigable(navigable);
    group.setDiscoverable(discoverable);
    group.setFindDistance((float) findDistance);
    return group;
  };

  private final SQLDialect dialect;

  public SqlDataStorage(SQLDialect dialect) {
    this.dialect = dialect;
  }

  @Override
  public void connect(Runnable initial) throws IOException {

    System.setProperty("org.jooq.no-logo", "true");
    System.setProperty("org.jooq.no-tips", "true");

    create = DSL.using(getConnectionProvider(), dialect, new Settings()
        .withRenderQuotedNames(RenderQuotedNames.ALWAYS)
        .withRenderSchema(dialect != SQLDialect.SQLITE));

    createPathVisualizerTable();
    createRoadMapTable();
    createNodeTable();
    createNodeGroupTable();
    createNodeGroupSearchTermsTable();
    createNodeGroupNodesTable();
    createEdgeTable();
    createDiscoverInfoTable();
  }

  private void createRoadMapTable() {
    create
        .createTableIfNotExists(PATHFINDER_ROADMAPS)
        .columns(PATHFINDER_ROADMAPS.fields())
        .execute();
  }

  private void createNodeTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODES)
        .columns(PATHFINDER_NODES.fields())
        .execute();
  }

  private void createEdgeTable() {
    create
        .createTableIfNotExists(PATHFINDER_EDGES)
        .columns(PATHFINDER_EDGES.fields())
        .execute();
  }

  private void createNodeGroupTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUPS)
        .columns(PATHFINDER_NODEGROUPS.fields())
        .execute();
  }

  private void createNodeGroupSearchTermsTable() {
    create
        .createTableIfNotExists(PATHFINDER_SEARCH_TERMS)
        .columns(PATHFINDER_SEARCH_TERMS.fields())
        .execute();
  }

  private void createNodeGroupNodesTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUP_NODES)
        .columns(PATHFINDER_NODEGROUP_NODES.fields())
        .execute();
  }

  private void createPathVisualizerTable() {
    create
        .createTableIfNotExists(PATHFINDER_PATH_VISUALIZER)
        .columns(PATHFINDER_PATH_VISUALIZER.fields())
        .execute();
  }

  private void createDiscoverInfoTable() {
    create
        .createTableIfNotExists(PATHFINDER_DISCOVERINGS)
        .columns(PATHFINDER_DISCOVERINGS.fields())
        .execute();
  }


  @Override
  public Map<NamespacedKey, RoadMap> loadRoadMaps() {
    HashedRegistry<RoadMap> registry = new HashedRegistry<>();
    create
        .selectFrom(PATHFINDER_ROADMAPS)
        .fetch(roadmapMapper)
        .forEach(registry::put);
    return registry;
  }

  @Override
  public void updateRoadMap(RoadMap roadMap) {
    create
        .insertInto(PATHFINDER_ROADMAPS)
        .values(
            roadMap.getKey(),
            roadMap.getNameFormat(),
            roadMap.getVisualizer() == null ? null : roadMap.getVisualizer()
                .getKey().toString(),
            roadMap.getDefaultCurveLength())
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_ROADMAPS.NAME_FORMAT, roadMap.getNameFormat())
        .set(PATHFINDER_ROADMAPS.PATH_VISUALIZER, roadMap.getVisualizer() == null ? null : roadMap
            .getVisualizer().getKey().toString())
        .set(PATHFINDER_ROADMAPS.PATH_CURVE_LENGTH, roadMap.getDefaultCurveLength())
        .execute();
  }

  @Override
  public void deleteRoadMap(NamespacedKey key) {
    create
        .deleteFrom(PATHFINDER_ROADMAPS)
        .where(PATHFINDER_ROADMAPS.KEY.eq(key))
        .execute();
  }

  @Override
  public void saveEdges(Collection<Edge> edges) {
    BatchBindStep step = create.batch(create
        .insertInto(PATHFINDER_EDGES)
        .columns(PATHFINDER_EDGES.START_ID, PATHFINDER_EDGES.END_ID, PATHFINDER_EDGES.WEIGHT_MODIFIER)
        .values(1, 1, 1.)
        .onConflictDoNothing()
    );
    for (Edge e : edges) {
      step = step.bind(e.getStart().getNodeId(), e.getEnd().getNodeId(), e.getWeightModifier());
    }
    step.execute();
  }

  @Override
  public Collection<Edge> loadEdges(RoadMap roadMap, Map<Integer, Node<?>> scope) {
    Collection<Integer> ids = scope.keySet();
    return new HashSet<>(create
        .selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(ids))
        .or(PATHFINDER_EDGES.END_ID.in(ids))
        .fetch(edgeMapper.apply(scope)));
  }

  @Override
  public void deleteEdgesFrom(Node<?> start) {
    create.deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start.getNodeId()))
        .execute();
  }

  @Override
  public void deleteEdgesTo(Node<?> end) {
    create.deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end.getNodeId()))
        .execute();
  }

  public void deleteEdge(Node<?> start, Node<?> end) {
    deleteEdge(start.getNodeId(), end.getNodeId());
  }

  public void deleteEdge(int start, int end) {
    create.deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start))
        .and(PATHFINDER_EDGES.END_ID.eq(end))
        .execute();
  }

  @Override
  public void deleteEdges(Collection<Edge> edges) {
    create.batched(configuration -> {
      for (Edge edge : edges) {
        DSL.using(configuration)
            .deleteFrom(PATHFINDER_EDGES)
            .where(PATHFINDER_EDGES.START_ID.eq(edge.getStart().getNodeId()))
            .and(PATHFINDER_EDGES.END_ID.eq(edge.getEnd().getNodeId()))
            .execute();
      }
    });
  }

  @Override
  public Map<Integer, Waypoint> loadNodes(RoadMap roadMap) {

    Map<Integer, Waypoint> map = new TreeMap<>();
    create
        .selectFrom(PATHFINDER_NODES)
        .where(PATHFINDER_NODES.ROADMAP_KEY.eq(roadMap.getKey()))
        .fetch(nodeMapper.apply(roadMap))
        .forEach(node -> map.put(node.getNodeId(), node));
    return map;
  }

  @Override
  public void updateNode(Waypoint node) {
    create
        .insertInto(PATHFINDER_NODES)
        .values(
            node.getNodeId(),
            node.getType().getKey().toString(),
            node.getRoadMapKey().toString(),
            node.getLocation().getX(),
            node.getLocation().getY(),
            node.getLocation().getZ(),
            node.getLocation().getWorld().getUID().toString(),
            node.getCurveLength()
        )
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_NODES.TYPE, node.getType().getKey())
        .set(PATHFINDER_NODES.ROADMAP_KEY, node.getRoadMapKey())
        .set(PATHFINDER_NODES.X, node.getLocation().getX())
        .set(PATHFINDER_NODES.Y, node.getLocation().getY())
        .set(PATHFINDER_NODES.Z, node.getLocation().getZ())
        .set(PATHFINDER_NODES.WORLD, node.getLocation().getWorld().getUID().toString())
        .execute();
  }

  @Override
  public void deleteNodes(Collection<Integer> nodeIds) {
    create
        .deleteFrom(PATHFINDER_NODES)
        .where(PATHFINDER_NODES.ID.in(nodeIds))
        .execute();
  }

  @Override
  public void assignNodesToGroup(NodeGroup group, NodeSelection selection) {
    create.batched(configuration -> {
      for (Node<?> node : selection) {
        DSL.using(configuration)
            .insertInto(PATHFINDER_NODEGROUP_NODES)
            .values(group.getKey(), node.getNodeId())
            .onDuplicateKeyIgnore()
            .execute();
      }
    });
  }

  @Override
  public void removeNodesFromGroup(NodeGroup group, Iterable<Groupable<?>> selection) {
    Collection<Integer> ids = new HashSet<>();
    selection.forEach(g -> ids.add(g.getNodeId()));

    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(group.getKey()))
        .and(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(ids))
        .execute();
  }

  @Override
  public Map<Integer, ? extends Collection<NamespacedKey>> loadNodeGroupNodes() {
    Map<Integer, HashSet<NamespacedKey>> result = new LinkedHashMap<>();
    create
        .selectFrom(PATHFINDER_NODEGROUP_NODES)
        .fetch()
        .forEach(record -> {
          result.computeIfAbsent(record.getNodeId(), id -> new HashSet<>()).add(record.getGroupKey());
        });
    return result;
  }

  @Override
  public HashedRegistry<NodeGroup> loadNodeGroups() {
    HashedRegistry<NodeGroup> registry = new HashedRegistry<>();
    create
        .selectFrom(PATHFINDER_NODEGROUPS)
        .fetch(groupMapper)
        .forEach(registry::put);
    return registry;
  }

  @Override
  public void updateNodeGroup(NodeGroup group) {
    create
        .insertInto(PATHFINDER_NODEGROUPS)
        .values(
            group.getKey().toString(),
            group.getNameFormat(),
            group.getPermission(),
            group.isNavigable(),
            group.isDiscoverable(),
            group.getFindDistance()
        )
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_NODEGROUPS.KEY, group.getKey())
        .set(PATHFINDER_NODEGROUPS.NAME_FORMAT, group.getNameFormat())
        .set(PATHFINDER_NODEGROUPS.PERMISSION, group.getPermission())
        .set(PATHFINDER_NODEGROUPS.NAVIGABLE, group.isNavigable())
        .set(PATHFINDER_NODEGROUPS.DISCOVERABLE, group.isDiscoverable())
        .set(PATHFINDER_NODEGROUPS.FIND_DISTANCE, group.getFindDistance() * 1.)
        .execute();
  }

  @Override
  public void deleteNodeGroup(NamespacedKey key) {
    create
        .deleteFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(key))
        .execute();
  }

  @Override
  public Map<NamespacedKey, Collection<String>> loadSearchTerms() {
    Map<NamespacedKey, Collection<String>> registry = new HashMap<>();
    create
        .selectFrom(PATHFINDER_SEARCH_TERMS)
        .fetch()
        .forEach(record -> {
          registry.computeIfAbsent(record.getGroupKey(), k -> new HashSet<>()).add(record.getSearchTerm());
        });
    return registry;
  }

  @Override
  public void addSearchTerms(NodeGroup group, Collection<String> searchTerms) {
    create.batched(configuration -> {
      for (String searchTerm : searchTerms) {
        DSL.using(configuration)
            .insertInto(PATHFINDER_SEARCH_TERMS)
            .columns(PATHFINDER_SEARCH_TERMS.GROUP_KEY, PATHFINDER_SEARCH_TERMS.SEARCH_TERM)
            .values(group.getKey(), searchTerm)
            .execute();
      }
    });
  }

  @Override
  public void removeSearchTerms(NodeGroup group, Collection<String> searchTerms) {
    create.batched(configuration -> {
      for (String searchTerm : searchTerms) {
        DSL.using(configuration)
            .deleteFrom(PATHFINDER_SEARCH_TERMS)
            .where(PATHFINDER_SEARCH_TERMS.GROUP_KEY.eq(group.getKey()))
            .and(PATHFINDER_SEARCH_TERMS.SEARCH_TERM.eq(searchTerm))
            .execute();
      }
    });
  }

  @Override
  public DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, LocalDateTime foundDate) {
    create
        .insertInto(PATHFINDER_DISCOVERINGS)
        .values(discoverable.getKey().toString(), player, foundDate)
        .onDuplicateKeyIgnore()
        .execute();
    return new DiscoverInfo(player, discoverable.getKey(), foundDate);
  }

  @Override
  public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
    Map<NamespacedKey, DiscoverInfo> registry = new HashMap<>();
    create
        .selectFrom(PATHFINDER_DISCOVERINGS)
        .where(PATHFINDER_DISCOVERINGS.PLAYER_ID.eq(playerId.toString()))
        .forEach(record -> {
          NamespacedKey key = record.getDiscoverKey();
          LocalDateTime date = record.getDate();

          DiscoverInfo info = new DiscoverInfo(playerId, key, date);
          registry.put(info.discoverable(), info);
        });
    return registry;
  }

  @Override
  public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
    create
        .deleteFrom(PATHFINDER_DISCOVERINGS)
        .where(PATHFINDER_DISCOVERINGS.PLAYER_ID.eq(playerId.toString()))
        .and(PATHFINDER_DISCOVERINGS.DISCOVER_KEY.eq(discoverKey))
        .execute();
  }

  @Override
  public <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(
      VisualizerType<T> type) {
    HashedRegistry<T> registry = new HashedRegistry<>();
    create
        .selectFrom(PATHFINDER_PATH_VISUALIZER)
        .where(PATHFINDER_PATH_VISUALIZER.TYPE.eq(type.getKey()))
        .fetch(record -> {

          // create visualizer object
          T visualizer = type.create(record.getKey(),
              record.getNameFormat());
          visualizer.setPermission(record.getPermission());
          Integer interval = record.getInterval();
          visualizer.setInterval(interval == null ? 20 : interval);

          // inject data from map
          YamlConfiguration cfg = new YamlConfiguration();
          try {
            cfg.loadFromString(record.getData());
          } catch (InvalidConfigurationException e) {
            e.printStackTrace();
          }
          type.deserialize(visualizer, cfg.getValues(false));

          return visualizer;
        })
        .forEach(registry::put);
    return registry;
  }

  @Override
  public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {

    Map<String, Object> data = visualizer.getType().serialize(visualizer);
    if (data == null) {
      return;
    }
    YamlConfiguration cfg = new YamlConfiguration();
    data.forEach(cfg::set);
    String dataString = cfg.saveToString();

    create
        .insertInto(PATHFINDER_PATH_VISUALIZER)
        .columns(
            PATHFINDER_PATH_VISUALIZER.KEY,
            PATHFINDER_PATH_VISUALIZER.TYPE,
            PATHFINDER_PATH_VISUALIZER.NAME_FORMAT,
            PATHFINDER_PATH_VISUALIZER.PERMISSION,
            PATHFINDER_PATH_VISUALIZER.INTERVAL,
            PATHFINDER_PATH_VISUALIZER.DATA
        )
        .values(
            visualizer.getKey(), visualizer.getType().getKey(),
            visualizer.getNameFormat(), visualizer.getPermission(),
            visualizer.getInterval(), dataString
        )
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_PATH_VISUALIZER.KEY, visualizer.getKey())
        .set(PATHFINDER_PATH_VISUALIZER.TYPE, visualizer.getType().getKey())
        .set(PATHFINDER_PATH_VISUALIZER.NAME_FORMAT, visualizer.getNameFormat())
        .set(PATHFINDER_PATH_VISUALIZER.PERMISSION, visualizer.getPermission())
        .set(PATHFINDER_PATH_VISUALIZER.INTERVAL, visualizer.getInterval())
        .set(PATHFINDER_PATH_VISUALIZER.DATA, dataString)
        .execute();
  }

  @Override
  public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
    create
        .deleteFrom(PATHFINDER_PATH_VISUALIZER)
        .where(PATHFINDER_PATH_VISUALIZER.KEY.eq(visualizer.getKey()))
        .execute();
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
  public void newVisualizerStyle(ParticleVisualizer visualizer, @Nullable String permission,
                                 @Nullable Material iconType, @Nullable String miniDisplayName) {

  }

  @Override
  public void updateVisualizerStyle(ParticleVisualizer visualizer) {

  }

  @Override
  public void deleteStyleVisualizer(int visualizerId) {

  }

  @Override
  public Map<Integer, Collection<ParticleVisualizer>> loadStyleRoadmapMap(
      Collection<ParticleVisualizer> visualizers) {
    return null;
  }

  @Override
  public void addStyleToRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer) {

  }

  @Override
  public void removeStyleFromRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer) {

  }
}
