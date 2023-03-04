package de.cubbossa.pathfinder.data;

import static org.jooq.impl.SQLDataType.BOOLEAN;
import static org.jooq.impl.SQLDataType.DOUBLE;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
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
import org.jooq.Converter;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.conf.ParamCastMode;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class SqlDataStorage implements DataStorage {

  abstract ConnectionProvider getConnectionProvider();

  private static final DataType<NamespacedKey> NAMESPACEDKEY1 = VARCHAR(64).asConvertedDataType(
      NamespacedKey.class, NamespacedKey::fromString, NamespacedKey::toString
  );

  private static final String PREFIX = "pathfinder_";

  private DSLContext create;
  private static final DataType<String> NAMESPACEDKEY = VARCHAR(64).notNull();

  // +-----------------------------------------+
  // |  Roadmap Table                          |
  // +-----------------------------------------+
  private static final Name RM = DSL.name(PREFIX + "roadmaps");

  private Table<Record> roadmapTable;
  private final Field<NamespacedKey> roadmapFieldKey = DSL.field(DSL.name("key"), NAMESPACEDKEY1);
  private final Field<String> roadmapFieldNameFormat =
      DSL.field(DSL.name("name_format"), VARCHAR.notNull());
  private final Field<String> roadmapFieldVisualizer =
      DSL.field(DSL.name("path_visualizer"), NAMESPACEDKEY.nullable(true));
  private final Field<Double> roadmapFieldCurveLength =
      DSL.field(DSL.name("path_curve_length"), DOUBLE.notNull().defaultValue(3.));

  private final RecordMapper<? super Record, RoadMap> roadmapMapper = record -> {
    NamespacedKey key = record.getValue(roadmapFieldKey.getQualifiedName(), Converter.from(String.class, NamespacedKey.class, NamespacedKey::fromString));
    String nameFormat = record.get(roadmapFieldNameFormat);
    String visKeyString = record.get(roadmapFieldVisualizer);
    NamespacedKey visKey = visKeyString == null ? null : NamespacedKey.fromString(visKeyString);
    double pathCurveLength = record.get(roadmapFieldCurveLength);

    return new RoadMap(
        key,
        nameFormat,
        visKey == null ? null : VisualizerHandler.getInstance().getPathVisualizer(visKey),
        pathCurveLength);
  };

  // +-----------------------------------------+
  // |  Node Table                             |
  // +-----------------------------------------+
  private static final String NODE = PREFIX + "nodes";

  private Table<Record> nodeTable;
  private final Field<Integer> nodeFieldId = DSL.field(DSL.name("id"), INTEGER.notNull());
  private final Field<String> nodeFieldType = DSL.field(DSL.name("type"), NAMESPACEDKEY.notNull());
  private final Field<String> nodeFieldRoadMap =
      DSL.field(DSL.name("roadmap_key"), NAMESPACEDKEY.notNull());
  private final Field<Double> nodeFieldX = DSL.field(DSL.name("x"), DOUBLE.notNull());
  private final Field<Double> nodeFieldY = DSL.field(DSL.name("y"), DOUBLE.notNull());
  private final Field<Double> nodeFieldZ = DSL.field(DSL.name("z"), DOUBLE.notNull());
  private final Field<UUID> nodeFieldWorld =
      DSL.field(DSL.name("world"), SQLDataType.VARCHAR(36).asConvertedDataType(
          UUID.class,
          UUID::fromString,
          UUID::toString
      ));

  private final Field<Double> nodeFieldCurveLength =
      DSL.field(DSL.name("path_curve_length"), DOUBLE.nullable(true));


  private final Function<RoadMap, RecordMapper<? super Record, Waypoint>> nodeMapper = roadmap -> {
    final RoadMap rm = roadmap;
    return record -> {
      int id = record.get(nodeFieldId);
      double x = record.get(nodeFieldX);
      double y = record.get(nodeFieldY);
      double z = record.get(nodeFieldZ);
      UUID worldUid = record.get(nodeFieldWorld);
      Double curveLength = record.get(nodeFieldCurveLength);

      Waypoint node = new Waypoint(id, rm, true);
      node.setLocation(new Location(Bukkit.getWorld(worldUid), x, y, z));
      node.setCurveLength(curveLength);
      return node;
    };
  };

  // +-----------------------------------------+
  // |  Edge Table                             |
  // +-----------------------------------------+
  private static final String EDGE = PREFIX + "edges";

  private Table<Record> edgeTable;
  private final Field<Integer> edgeFieldStart = DSL.field(DSL.name("start_id"), INTEGER.notNull());
  private final Field<Integer> edgeFieldEnd = DSL.field(DSL.name("end_id"), INTEGER.notNull());
  private final Field<Double> edgeFieldWeight =
      DSL.field(DSL.name("weight_modifier"), DOUBLE.notNull().defaultValue(1.));

  private final Function<Map<Integer, Node<?>>, RecordMapper<? super Record, Edge>> edgeMapper =
      map -> record -> {
        int startId = record.get(edgeFieldStart);
        int endId = record.get(edgeFieldEnd);
        double weight = record.get(edgeFieldWeight);

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
  private static final String GROUP = PREFIX + "nodegroups";

  private Table<Record> groupTable;
  private final Field<String> groupFieldKey = DSL.field(DSL.name("key"), NAMESPACEDKEY);
  private final Field<String> groupFieldNameFormat =
      DSL.field(DSL.name("name_format"), VARCHAR.notNull());
  private final Field<String> groupFieldPermission =
      DSL.field(DSL.name("permission"), VARCHAR.nullable(true));
  private final Field<Boolean> groupFieldNavigable =
      DSL.field(DSL.name("navigable"), BOOLEAN.notNull());
  private final Field<Boolean> groupFieldDiscoverable =
      DSL.field(DSL.name("discoverable"), BOOLEAN.notNull());
  private final Field<Double> groupFieldFindDistance =
      DSL.field(DSL.name("find_distance"), DOUBLE.notNull());

  private final RecordMapper<? super Record, NodeGroup> groupMapper = record -> {
    NamespacedKey key = NamespacedKey.fromString(record.get(groupFieldKey).toString());
    String nameFormat = record.get(groupFieldNameFormat);
    String permission = record.get(groupFieldPermission);
    boolean navigable = record.get(groupFieldNavigable);
    boolean discoverable = record.get(groupFieldDiscoverable);
    double findDistance = record.get(groupFieldFindDistance);

    NodeGroup group = new NodeGroup(key, nameFormat);
    group.setPermission(permission);
    group.setNavigable(navigable);
    group.setDiscoverable(discoverable);
    group.setFindDistance((float) findDistance);
    return group;
  };

  // +-----------------------------------------+
  // |  Nodegroup-Node Relation                |
  // +-----------------------------------------+
  private static final String GROUP_NODES = PREFIX + "nodegroup_nodes";
  private Table<Record> groupNodesRelation;
  private final Field<String> fieldGroupNodesGroupKey =
      DSL.field(DSL.name("group_key"), NAMESPACEDKEY.notNull());
  private final Field<Integer> fieldGroupNodesNodeId =
      DSL.field(DSL.name("node_id"), INTEGER.notNull());

  // +-----------------------------------------+
  // |  Discoverings                           |
  // +-----------------------------------------+
  private static final String DISCOVERINGS = PREFIX + "discoverings";

  private Table<Record> discoveringsTable;
  private final Field<String> discoveringsFieldDiscoverKey =
      DSL.field(DSL.name("discover_key"), NAMESPACEDKEY.notNull());
  private final Field<UUID> discoveringsFieldPlayerId =
      DSL.field(DSL.name("player_id"), SQLDataType.UUID.notNull());
  private final Field<Timestamp> discoveringsFieldDate =
      DSL.field(DSL.name("date"), SQLDataType.TIMESTAMP.notNull());

  // +-----------------------------------------+
  // |  Searchterms                            |
  // +-----------------------------------------+
  private static final String TERMS = PREFIX + "search_terms";

  private Table<Record> termsTable;
  private final Field<String> termsFieldGroup =
      DSL.field(DSL.name("group_key"), NAMESPACEDKEY.notNull());
  private final Field<String> termsFieldTerm =
      DSL.field(DSL.name("search_term"), VARCHAR(64).notNull());


  // +-----------------------------------------+
  // |  Visualizers                            |
  // +-----------------------------------------+
  private static final String VIS = PREFIX + "path_visualizer";

  private Table<Record> visualizerTable;
  private final Field<String> visFieldKey = DSL.field(DSL.name("key"), NAMESPACEDKEY.notNull());
  private final Field<String> visFieldType = DSL.field(DSL.name("type"), NAMESPACEDKEY.notNull());
  private final Field<String> visFieldName = DSL.field(DSL.name("name_format"), VARCHAR.notNull());
  private final Field<String> visFieldPerm =
      DSL.field(DSL.name("permission"), VARCHAR(64).nullable(true));
  private final Field<Integer> visFieldInterval =
      DSL.field(DSL.name("interval"), INTEGER.notNull());
  private final Field<String> visFieldData = DSL.field(DSL.name("data"), VARCHAR.nullable(true));

  private final SQLDialect dialect;

  public SqlDataStorage(SQLDialect dialect) {
    this.dialect = dialect;
  }

  @Override
  public void connect(Runnable initial) throws IOException {
    create = DSL.using(getConnectionProvider(), dialect, new Settings()
        .withParamCastMode(ParamCastMode.NEVER)
        .withRenderQuotedNames(RenderQuotedNames.ALWAYS));

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
    create.createTableIfNotExists(RM)
        .column(roadmapFieldKey, NAMESPACEDKEY1)
        .column(roadmapFieldNameFormat)
        .column(roadmapFieldVisualizer)
        .column(roadmapFieldCurveLength)
        .primaryKey(roadmapFieldKey)
        .execute();
    roadmapTable = DSL.table(RM);
    roadmapTable.fields(roadmapFieldKey, roadmapFieldNameFormat, roadmapFieldVisualizer,
        roadmapFieldCurveLength);
  }

  private void createNodeTable() {

    create.createTableIfNotExists(NODE)
        .column(nodeFieldId)
        .column(nodeFieldType)
        .column(nodeFieldRoadMap)
        .column(nodeFieldX)
        .column(nodeFieldY)
        .column(nodeFieldZ)
        .column(nodeFieldWorld)
        .column(nodeFieldCurveLength)
        .primaryKey(nodeFieldId)
        .execute();
    nodeTable = DSL.table(NODE);
  }

  private void createEdgeTable() {
    create.createTableIfNotExists(EDGE)
        .column(edgeFieldStart)
        .column(edgeFieldEnd)
        .column(edgeFieldWeight)
        .primaryKey(edgeFieldStart, edgeFieldEnd)
        .execute();
    edgeTable = DSL.table(EDGE);
  }

  private void createNodeGroupTable() {
    create.createTableIfNotExists(GROUP)
        .column(groupFieldKey)
        .column(groupFieldNameFormat)
        .column(groupFieldPermission)
        .column(groupFieldNavigable)
        .column(groupFieldDiscoverable)
        .column(groupFieldFindDistance)
        .primaryKey(groupFieldKey)
        .execute();
    groupTable = DSL.table(GROUP);
  }

  private void createNodeGroupSearchTermsTable() {
    create.createTableIfNotExists(TERMS)
        .column(termsFieldGroup)
        .column(termsFieldTerm)
        .primaryKey(termsFieldGroup, termsFieldTerm)
        .constraint(DSL.foreignKey(termsFieldGroup)
            .references(groupTable, groupFieldKey)
            .onDeleteCascade()
        )
        .execute();
    termsTable = DSL.table(TERMS);
  }

  private void createNodeGroupNodesTable() {
    create.createTableIfNotExists(GROUP_NODES)
        .column(fieldGroupNodesGroupKey)
        .column(fieldGroupNodesNodeId)
        .primaryKey(fieldGroupNodesGroupKey, fieldGroupNodesNodeId)
        .constraint(DSL.foreignKey(fieldGroupNodesGroupKey).references(groupTable, groupFieldKey))
        .execute();
    groupNodesRelation = DSL.table(GROUP_NODES);
  }

  private void createPathVisualizerTable() {
    create.createTableIfNotExists(VIS)
        .column(visFieldKey)
        .column(visFieldType)
        .column(visFieldName)
        .column(visFieldPerm)
        .column(visFieldInterval)
        .column(visFieldData)
        .primaryKey(visFieldKey)
        .execute();
    visualizerTable = DSL.table(VIS);
  }

  private void createDiscoverInfoTable() {
    create.createTableIfNotExists(DISCOVERINGS)
        .column(discoveringsFieldDiscoverKey)
        .column(discoveringsFieldPlayerId)
        .column(discoveringsFieldDate)
        .primaryKey(discoveringsFieldDiscoverKey, discoveringsFieldPlayerId)
        .execute();
    discoveringsTable = DSL.table(DISCOVERINGS);
  }


  @Override
  public Map<NamespacedKey, RoadMap> loadRoadMaps() {
    HashedRegistry<RoadMap> registry = new HashedRegistry<>();
    create
        .select(roadmapTable.asterisk())
        .from(roadmapTable)
        .fetch(roadmapMapper)
        .forEach(registry::put);
    return registry;
  }

  @Override
  public void updateRoadMap(RoadMap roadMap) {
    System.out.println("Called");
    create
        .insertInto(DSL.table(RM), roadmapFieldKey, roadmapFieldNameFormat, roadmapFieldVisualizer, roadmapFieldCurveLength)
        .values(
            roadMap.getKey(),
            roadMap.getNameFormat(),
            roadMap.getVisualizer() == null ? null : roadMap.getVisualizer()
                .getKey().toString(),
            roadMap.getDefaultCurveLength())
        .onDuplicateKeyUpdate()
        .set(roadmapFieldNameFormat, roadMap.getNameFormat())
        .set(roadmapFieldVisualizer, roadMap.getVisualizer() == null ? null : roadMap
            .getVisualizer().getKey().toString())
        .set(roadmapFieldCurveLength, roadMap.getDefaultCurveLength())
        .execute();
  }

  @Override
  public void deleteRoadMap(NamespacedKey key) {
    create
        .deleteFrom(roadmapTable)
        .where(roadmapFieldKey.eq(key))
        .execute();
  }

  @Override
  public void saveEdges(Collection<Edge> edges) {
    BatchBindStep step = create.batch(create
        .insertInto(edgeTable)
        .columns(edgeFieldStart, edgeFieldEnd, edgeFieldWeight)
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
        .select()
        .from(edgeTable)
        .where(edgeFieldStart.in(ids))
        .or(edgeFieldEnd.in(ids))
        .fetch(edgeMapper.apply(scope)));
  }

  @Override
  public void deleteEdgesFrom(Node<?> start) {
    create.deleteFrom(edgeTable)
        .where(edgeFieldStart.eq(start.getNodeId()))
        .execute();
  }

  @Override
  public void deleteEdgesTo(Node<?> end) {
    create.deleteFrom(edgeTable)
        .where(edgeFieldEnd.eq(end.getNodeId()))
        .execute();
  }

  public void deleteEdge(Node<?> start, Node<?> end) {
    deleteEdge(start.getNodeId(), end.getNodeId());
  }

  public void deleteEdge(int start, int end) {
    create.deleteFrom(edgeTable)
        .where(edgeFieldStart.eq(start))
        .and(edgeFieldEnd.eq(end))
        .execute();
  }

  @Override
  public void deleteEdges(Collection<Edge> edges) {
    create.batched(configuration -> {
      for (Edge edge : edges) {
        DSL.using(configuration)
            .deleteFrom(edgeTable)
            .where(edgeFieldStart.eq(edge.getStart().getNodeId()))
            .and(edgeFieldEnd.eq(edge.getEnd().getNodeId()))
            .execute();
      }
    });
  }

  @Override
  public Map<Integer, Waypoint> loadNodes(RoadMap roadMap) {

    Map<Integer, Waypoint> map = new TreeMap<>();
    create
        .select()
        .from(nodeTable)
        .where(nodeFieldRoadMap.eq(roadMap.getKey().toString()))
        .fetch(nodeMapper.apply(roadMap))
        .forEach(node -> map.put(node.getNodeId(), node));
    return map;
  }

  @Override
  public void updateNode(Waypoint node) {
    create
        .insertInto(nodeTable)
        .values(
            node.getNodeId(),
            node.getType().getKey().toString(),
            node.getRoadMapKey().toString(),
            node.getLocation().getX(),
            node.getLocation().getY(),
            node.getLocation().getZ(),
            node.getLocation().getWorld().getUID(),
            node.getCurveLength()
        )
        .onDuplicateKeyUpdate()
        .set(nodeFieldType, node.getType().getKey().toString())
        .set(nodeFieldRoadMap, node.getRoadMapKey().toString())
        .set(nodeFieldX, node.getLocation().getX())
        .set(nodeFieldY, node.getLocation().getY())
        .set(nodeFieldZ, node.getLocation().getZ())
        .set(nodeFieldWorld, node.getLocation().getWorld().getUID())
        .execute();
  }

  @Override
  public void deleteNodes(Collection<Integer> nodeIds) {

    create.deleteFrom(nodeTable)
        .where(nodeFieldId.in(nodeIds))
        .execute();
  }

  @Override
  public void assignNodesToGroup(NodeGroup group, NodeSelection selection) {
    create.batched(configuration -> {
      for (Node<?> node : selection) {
        DSL.using(configuration)
            .insertInto(groupNodesRelation)
            .values(group.getKey().toString(), node.getNodeId())
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
        .deleteFrom(groupNodesRelation)
        .where(fieldGroupNodesGroupKey.eq(group.getKey().toString()))
        .and(fieldGroupNodesNodeId.in(ids))
        .execute();
  }

  @Override
  public Map<Integer, ? extends Collection<NamespacedKey>> loadNodeGroupNodes() {
    Map<Integer, HashSet<NamespacedKey>> result = new LinkedHashMap<>();
    create
        .select()
        .from(groupNodesRelation)
        .fetch()
        .forEach(record -> {
          NamespacedKey key = NamespacedKey.fromString(record.get(fieldGroupNodesGroupKey));
          int nodeId = record.get(fieldGroupNodesNodeId);

          result.computeIfAbsent(nodeId, id -> new HashSet<>()).add(key);
        });
    return result;
  }

  @Override
  public HashedRegistry<NodeGroup> loadNodeGroups() {
    HashedRegistry<NodeGroup> registry = new HashedRegistry<>();
    create
        .select()
        .from(groupTable)
        .fetch(groupMapper)
        .forEach(registry::put);
    return registry;
  }

  @Override
  public void updateNodeGroup(NodeGroup group) {
    create
        .insertInto(groupTable)
        .values(
            group.getKey().toString(),
            group.getNameFormat(),
            group.getPermission(),
            group.isNavigable(),
            group.isDiscoverable(),
            group.getFindDistance()
        )
        .onDuplicateKeyUpdate()
        .set(groupFieldKey, group.getKey().toString())
        .set(groupFieldNameFormat, group.getNameFormat())
        .set(groupFieldPermission, group.getPermission())
        .set(groupFieldNavigable, group.isNavigable())
        .set(groupFieldDiscoverable, group.isDiscoverable())
        .set(groupFieldFindDistance, group.getFindDistance() * 1.)
        .execute();
  }

  @Override
  public void deleteNodeGroup(NamespacedKey key) {
    create
        .deleteFrom(groupTable)
        .where(groupFieldKey.eq(key.toString()))
        .execute();
  }

  @Override
  public Map<NamespacedKey, Collection<String>> loadSearchTerms() {
    Map<NamespacedKey, Collection<String>> registry = new HashMap<>();
    create
        .select()
        .from(termsTable)
        .fetch()
        .forEach(record -> {
          NamespacedKey key = NamespacedKey.fromString(record.get(termsFieldGroup));
          String searchTerm = record.get(termsFieldTerm);
          registry.computeIfAbsent(key, k -> new HashSet<>()).add(searchTerm);
        });
    return registry;
  }

  @Override
  public void addSearchTerms(NodeGroup group, Collection<String> searchTerms) {
    create.batched(configuration -> {
      for (String searchTerm : searchTerms) {
        DSL.using(configuration)
            .insertInto(termsTable)
            .values(group.getKey().toString(), searchTerm)
            .execute();
      }
    });
  }

  @Override
  public void removeSearchTerms(NodeGroup group, Collection<String> searchTerms) {
    create.batched(configuration -> {
      for (String searchTerm : searchTerms) {
        DSL.using(configuration)
            .deleteFrom(termsTable)
            .where(termsFieldGroup.eq(group.getKey().toString()))
            .and(termsFieldTerm.eq(searchTerm))
            .execute();
      }
    });
  }

  @Override
  public DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, Date foundDate) {
    create
        .insertInto(discoveringsTable)
        .values(discoverable.getKey().toString(), player, foundDate)
        .onDuplicateKeyIgnore()
        .execute();
    return new DiscoverInfo(player, discoverable.getKey(), foundDate);
  }

  @Override
  public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
    Map<NamespacedKey, DiscoverInfo> registry = new HashMap<>();
    create
        .select()
        .from(discoveringsTable)
        .where(discoveringsFieldPlayerId.eq(playerId))
        .fetch()
        .forEach(record -> {
          NamespacedKey key = NamespacedKey.fromString(record.get(discoveringsFieldDiscoverKey));
          Date date = record.get(discoveringsFieldDate);

          DiscoverInfo info = new DiscoverInfo(playerId, key, date);
          registry.put(info.discoverable(), info);
        });
    return registry;
  }

  @Override
  public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
    create
        .deleteFrom(discoveringsTable)
        .where(discoveringsFieldPlayerId.eq(playerId))
        .and(discoveringsFieldDiscoverKey.eq(discoverKey.toString()))
        .execute();
  }

  @Override
  public <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(
      VisualizerType<T> type) {
    HashedRegistry<T> registry = new HashedRegistry<>();
    create
        .selectFrom(visualizerTable)
        .where(visFieldType.eq(type.getKey().toString()))
        .fetch(record -> {

          // create visualizer object
          T visualizer = type.create(NamespacedKey.fromString(visFieldKey.get(record)),
              visFieldName.get(record));
          visualizer.setPermission(visFieldPerm.get(record));
          Integer interval = visFieldInterval.get(record);
          visualizer.setInterval(interval == null ? 20 : interval);

          // inject data from map
          YamlConfiguration cfg = new YamlConfiguration();
          try {
            cfg.loadFromString(visFieldData.get(record));
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
        .insertInto(visualizerTable)
        .columns(visFieldKey, visFieldType, visFieldName, visFieldPerm, visFieldInterval,
            visFieldData)
        .values(
            visualizer.getKey().toString(), visualizer.getType().getKey().toString(),
            visualizer.getNameFormat(), visualizer.getPermission(),
            visualizer.getInterval(), dataString
        )
        .onDuplicateKeyUpdate()
        .set(visFieldKey, visualizer.getKey().toString())
        .set(visFieldType, visualizer.getType().getKey().toString())
        .set(visFieldName, visualizer.getNameFormat())
        .set(visFieldPerm, visualizer.getPermission())
        .set(visFieldInterval, visualizer.getInterval())
        .set(visFieldData, dataString)
        .execute();
  }

  @Override
  public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
    create
        .delete(visualizerTable)
        .where(visFieldKey.eq(visualizer.getKey().toString()))
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
