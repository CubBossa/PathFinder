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
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.CombinedVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class SqlDatabase implements DataStorage {

  abstract Connection getConnection();

  private static final String PREFIX = "pathfinder_";

  private final DSLContext create;

  // +-----------------------------------------+
  // |  Roadmap Table                          |
  // +-----------------------------------------+
  private static final String RM = PREFIX + "roadmap";

  private Table<Record> roadmapTable;
  private final Field<String> roadmapFieldKey = DSL.field("key", VARCHAR(64).notNull());
  private final Field<String> roadmapFieldNameFormat = DSL.field("name_format", VARCHAR.notNull());
  private final Field<String> roadmapFieldVisualizer =
      DSL.field("path_visualizer", VARCHAR(64).nullable(true));
  private final Field<Double> roadmapFieldCurveLength =
      DSL.field("path_curve_length", DOUBLE.notNull().defaultValue(3.));

  private final RecordMapper<? super Record, RoadMap> roadmapMapper = record -> {
    String keyString = record.get(roadmapFieldKey);
    String nameFormat = record.get(roadmapFieldNameFormat);
    String pathVisualizerKeyString = record.get(roadmapFieldVisualizer);
    double pathCurveLength = record.get(roadmapFieldCurveLength);

    return new RoadMap(
        NamespacedKey.fromString(keyString),
        nameFormat,
        pathVisualizerKeyString == null ? null : VisualizerHandler.getInstance()
            .getPathVisualizer(NamespacedKey.fromString(pathVisualizerKeyString)),
        pathCurveLength);
  };

  // +-----------------------------------------+
  // |  Node Table                             |
  // +-----------------------------------------+
  private static final String NODE = PREFIX + "nodes";

  private Table<Record> nodeTable;
  private final Field<Integer> nodeFieldId = DSL.field("id", INTEGER.notNull());
  private final Field<String> nodeFieldType = DSL.field("type", VARCHAR(64).notNull());
  private final Field<String> nodeFieldRoadMap = DSL.field("roadmap_key", VARCHAR(64).notNull());
  private final Field<Double> nodeFieldX = DSL.field("x", DOUBLE.notNull());
  private final Field<Double> nodeFieldY = DSL.field("y", DOUBLE.notNull());
  private final Field<Double> nodeFieldZ = DSL.field("z", DOUBLE.notNull());
  private final Field<UUID> nodeFieldWorld = DSL.field("world", SQLDataType.UUID.notNull());
  private final Field<Double> nodeFieldCurveLength =
      DSL.field("path_curve_length", DOUBLE.nullable(true));


  private final Function<RoadMap, RecordMapper<? super Record, Node>> nodeMapper = roadmap -> {
    final RoadMap rm = roadmap;
    return record -> {
      int id = record.get(nodeFieldId);
      String type = record.get(nodeFieldType);
      double x = record.get(nodeFieldX);
      double y = record.get(nodeFieldY);
      double z = record.get(nodeFieldZ);
      UUID worldUid = record.get(nodeFieldWorld);
      Double curveLength = record.get(nodeFieldCurveLength);

      NodeType<?> nodeType =
          NodeTypeHandler.getInstance().getNodeType(NamespacedKey.fromString(type));
      Node node = nodeType.getFactory().apply(new NodeType.NodeCreationContext(
          rm,
          id,
          new Location(Bukkit.getWorld(worldUid), x, y, z),
          true
      ));
      node.setCurveLength(curveLength);
      return node;
    };
  };

  // +-----------------------------------------+
  // |  Edge Table                             |
  // +-----------------------------------------+
  private static final String EDGE = PREFIX + "edges";

  private Table<Record> edgeTable;
  private final Field<Integer> edgeFieldStart = DSL.field("start_id", INTEGER.notNull());
  private final Field<Integer> edgeFieldEnd = DSL.field("end_id", INTEGER.notNull());
  private final Field<Double> edgeFieldWeight =
      DSL.field("weight_modifier", DOUBLE.notNull().defaultValue(1.));

  private final Function<Map<Integer, Node>, RecordMapper<? super Record, Edge>> edgeMapper =
      map -> record -> {
        int startId = record.get(edgeFieldStart);
        int endId = record.get(edgeFieldEnd);
        double weight = record.get(edgeFieldWeight);

        Node start = map.get(startId);
        Node end = map.get(endId);

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
  private final Field<String> groupFieldKey = DSL.field("key", VARCHAR(64).notNull());
  private final Field<String> groupFieldNameFormat = DSL.field("key", VARCHAR.notNull());
  private final Field<String> groupFieldPermission =
      DSL.field("permission", VARCHAR.nullable(true));
  private final Field<Boolean> groupFieldNavigable = DSL.field("navigable", BOOLEAN.notNull());
  private final Field<Boolean> groupFieldDiscoverable =
      DSL.field("discoverable", BOOLEAN.notNull());
  private final Field<Double> groupFieldFindDistance = DSL.field("find_distance", DOUBLE.notNull());

  private final RecordMapper<? super Record, NodeGroup> groupMapper = record -> {
    String keyString = record.get(groupFieldKey);
    String nameFormat = record.get(groupFieldNameFormat);
    String permission = record.get(groupFieldPermission);
    boolean navigable = record.get(groupFieldNavigable);
    boolean discoverable = record.get(groupFieldDiscoverable);
    double findDistance = record.get(groupFieldFindDistance);

    NodeGroup group = new NodeGroup(NamespacedKey.fromString(keyString), nameFormat);
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
      DSL.field("group_key", VARCHAR(64).notNull());
  private final Field<Integer> fieldGroupNodesNodeId = DSL.field("node_id", INTEGER.notNull());

  // +-----------------------------------------+
  // |  Discoverings                           |
  // +-----------------------------------------+
  private static final String DISCOVERINGS = PREFIX + "discoverings";

  private Table<Record> discoveringsTable;
  private final Field<String> discoveringsFieldDiscoverKey =
      DSL.field("discover_key", VARCHAR(64).notNull());
  private final Field<UUID> discoveringsFieldPlayerId =
      DSL.field("player_id", SQLDataType.UUID.notNull());
  private final Field<Timestamp> discoveringsFieldDate =
      DSL.field("date", SQLDataType.TIMESTAMP.notNull());

  // +-----------------------------------------+
  // |  Searchterms                            |
  // +-----------------------------------------+
  private static final String TERMS = PREFIX + "search_terms";

  private Table<Record> termsTable;
  private final Field<String> termsFieldGroup =
      DSL.field("group_key", VARCHAR(64).notNull());
  private final Field<String> termsFieldTerm =
      DSL.field("search_term", VARCHAR(64).notNull());

  public SqlDatabase() {
    create = DSL.using(SQLDialect.SQLITE);
  }

  @Override
  public void connect(Runnable initial) throws IOException {
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
    DSL.createTableIfNotExists(RM)
        .column(roadmapFieldKey)
        .column(roadmapFieldNameFormat)
        .column(roadmapFieldVisualizer)
        .column(roadmapFieldCurveLength)
        .primaryKey(roadmapFieldKey)
        .execute();
    roadmapTable = DSL.table(RM);
  }

  private void createNodeTable() {

    DSL.createTableIfNotExists(RM)
        .column(nodeFieldId)
        .column(nodeFieldType)
        .column(nodeFieldRoadMap)
        .column(nodeFieldX)
        .column(nodeFieldY)
        .column(nodeFieldZ)
        .column(nodeFieldWorld)
        .column(nodeFieldCurveLength)
        .primaryKey(nodeFieldId)
        .constraint(DSL.foreignKey(nodeFieldRoadMap).references(roadmapTable))
        .execute();
    nodeTable = DSL.table(NODE);
  }

  private void createEdgeTable() {
    DSL.createTableIfNotExists(EDGE)
        .column(edgeFieldStart)
        .column(edgeFieldEnd)
        .column(edgeFieldWeight)
        .primaryKey(edgeFieldStart, edgeFieldEnd)
        .execute();
    edgeTable = DSL.table(EDGE);
  }

  private void createNodeGroupTable() {
    DSL.createTableIfNotExists(GROUP)
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
    DSL.createTableIfNotExists(TERMS)
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
    DSL.createTableIfNotExists(GROUP_NODES)
        .column(fieldGroupNodesGroupKey)
        .column(fieldGroupNodesNodeId)
        .primaryKey(fieldGroupNodesGroupKey, fieldGroupNodesNodeId)
        .constraint(DSL.foreignKey(fieldGroupNodesGroupKey).references(groupTable, groupFieldKey))
        .execute();
    groupNodesRelation = DSL.table(GROUP_NODES);
  }

  private void createPathVisualizerTable() {
    try (Connection con = getConnection()) {
      try (PreparedStatement stmt = con.prepareStatement(
          "CREATE TABLE IF NOT EXISTS `pathfinder_path_visualizer` (" +
              "`key` VARCHAR(64) NOT NULL PRIMARY KEY ," +
              "`type` VARCHAR(64) NOT NULL ," +
              "`name_format` TEXT NOT NULL ," +
              "`permission` VARCHAR(64) NULL ," +
              "`interval` INT NOT NULL ," +
              "`data` TEXT NULL )")) {
        stmt.executeUpdate();
      }
    } catch (Exception e) {
      throw new DataStorageException("Could not create path visualizer table.", e);
    }
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
    create
        .insertInto(roadmapTable)
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
        .where(roadmapFieldKey.eq(key.toString()))
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
  public Collection<Edge> loadEdges(RoadMap roadMap, Map<Integer, Node> scope) {
    Collection<Integer> ids = scope.keySet();
    return new HashSet<>(create
        .select(edgeTable.asterisk())
        .from(edgeTable)
        .where(edgeFieldStart.in(ids))
        .or(edgeFieldEnd.in(ids))
        .fetch(edgeMapper.apply(scope)));
  }

  @Override
  public void deleteEdgesFrom(Node start) {
    create.deleteFrom(edgeTable)
        .where(edgeFieldStart.eq(start.getNodeId()))
        .execute();
  }

  @Override
  public void deleteEdgesTo(Node end) {
    create.deleteFrom(edgeTable)
        .where(edgeFieldEnd.eq(end.getNodeId()))
        .execute();
  }

  public void deleteEdge(Node start, Node end) {
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
  public Map<Integer, Node> loadNodes(RoadMap roadMap) {

    Map<Integer, Node> map = new TreeMap<>();
    create
        .select(nodeTable.asterisk())
        .from(nodeTable)
        .where(roadmapFieldKey.eq(roadMap.getKey().toString()))
        .fetch(nodeMapper.apply(roadMap))
        .forEach(node -> map.put(node.getNodeId(), node));
    return map;
  }

  @Override
  public void updateNode(Node node) {
    try (Connection con = getConnection()) {
      try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_nodes` " +
          "(`id`, `type`, `roadmap_key`, `x`, `y`, `z`, `world`, `path_curve_length`) VALUES " +
          "(?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(`id`) DO UPDATE SET " +
          "`x` = ?, " +
          "`y` = ?, " +
          "`z` = ?, " +
          "`world` = ?, " +
          "`path_curve_length` = ?")) {
        stmt.setInt(1, node.getNodeId());
        stmt.setString(2, node.getType().getKey().toString());
        stmt.setString(3, node.getRoadMapKey().toString());
        stmt.setDouble(4, node.getLocation().getX());
        stmt.setDouble(5, node.getLocation().getY());
        stmt.setDouble(6, node.getLocation().getZ());
        stmt.setString(7, node.getLocation().getWorld().getUID().toString());
        if (node.getCurveLength() == null) {
          stmt.setNull(8, Types.DOUBLE);
        } else {
          stmt.setDouble(8, node.getCurveLength());
        }
        stmt.setDouble(9, node.getLocation().getX());
        stmt.setDouble(10, node.getLocation().getY());
        stmt.setDouble(11, node.getLocation().getZ());
        stmt.setString(12, node.getLocation().getWorld().getUID().toString());
        if (node.getCurveLength() == null) {
          stmt.setNull(13, Types.DOUBLE);
        } else {
          stmt.setDouble(13, node.getCurveLength());
        }
        stmt.executeUpdate();
      }
    } catch (Exception e) {
      throw new DataStorageException("Could not update node.", e);
    }
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
      for (Node node : selection) {
        DSL.using(configuration)
            .insertInto(groupNodesRelation)
            .values(group.getKey(), node.getNodeId())
            .onDuplicateKeyIgnore()
            .execute();
      }
    });
  }

  @Override
  public void removeNodesFromGroup(NodeGroup group, Iterable<Groupable> selection) {
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
        .select(groupNodesRelation.asterisk())
        .from(groupNodesRelation)
        .fetch()
        .forEach(record -> {
          String keyString = record.get(fieldGroupNodesGroupKey);
          int nodeId = record.get(fieldGroupNodesNodeId);

          HashSet<NamespacedKey> l = result.computeIfAbsent(nodeId, id -> new HashSet<>());
          l.add(NamespacedKey.fromString(keyString));
        });
    return result;
  }

  @Override
  public HashedRegistry<NodeGroup> loadNodeGroups() {
    HashedRegistry<NodeGroup> registry = new HashedRegistry<>();
    create
        .select(groupTable.asterisk())
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
            group.getKey(),
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
    try (Connection con = getConnection()) {
      try (PreparedStatement stmt = con.prepareStatement(
          "SELECT * FROM `pathfinder_search_terms`")) {
        try (ResultSet resultSet = stmt.executeQuery()) {
          Map<NamespacedKey, Collection<String>> registry = new HashMap<>();
          while (resultSet.next()) {
            String keyString = resultSet.getString("group_key");
            String searchTerm = resultSet.getString("search_term");

            Collection<String> l = registry.computeIfAbsent(NamespacedKey.fromString(keyString),
                key -> new HashSet<>());
            l.add(searchTerm);
          }
          return registry;
        }
      }
    } catch (Exception e) {
      throw new DataStorageException("Could not load search terms.", e);
    }
  }

  @Override
  public void addSearchTerms(NodeGroup group, Collection<String> searchTerms) {
    try (Connection con = getConnection()) {
      boolean wasAuto = con.getAutoCommit();
      con.setAutoCommit(false);

      try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_search_terms` " +
          "(`group_key`, `search_term`) VALUES (?, ?)")) {
        for (String term : searchTerms) {
          stmt.setString(1, group.getKey().toString());
          stmt.setString(2, term);
          stmt.addBatch();
        }
        stmt.executeBatch();
      }
      con.commit();
      con.setAutoCommit(wasAuto);
    } catch (Exception e) {
      throw new DataStorageException("Could not add search terms.", e);
    }
  }

  @Override
  public void removeSearchTerms(NodeGroup group, Collection<String> searchTerms) {
    try (Connection con = getConnection()) {
      boolean wasAuto = con.getAutoCommit();
      con.setAutoCommit(false);
      try (PreparedStatement stmt = con.prepareStatement(
          "DELETE FROM `pathfinder_search_terms` WHERE `group_key` = ? AND `search_term` = ?")) {

        for (String term : searchTerms) {
          stmt.setString(1, group.getKey().toString());
          stmt.setString(2, term);
          stmt.addBatch();
        }
        stmt.executeBatch();
      }
      con.commit();
      con.setAutoCommit(wasAuto);
    } catch (Exception e) {
      throw new DataStorageException("Could not remove search terms.", e);
    }
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
        .select(discoveringsTable.asterisk())
        .from(discoveringsTable)
        .where(discoveringsFieldPlayerId.eq(playerId))
        .fetch()
        .forEach(record -> {
          String keyString = record.get(discoveringsFieldDiscoverKey);
          Date date = record.get(discoveringsFieldDate);

          DiscoverInfo info = new DiscoverInfo(playerId, NamespacedKey.fromString(keyString), date);
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
  public Map<NamespacedKey, PathVisualizer<?, ?>> loadPathVisualizer() {
    try (Connection con = getConnection()) {
      try (PreparedStatement stmt = con.prepareStatement(
          "SELECT * FROM `pathfinder_path_visualizer`")) {
        try (ResultSet resultSet = stmt.executeQuery()) {
          Map<PathVisualizer, String> dataMap = new HashMap<>();
          while (resultSet.next()) {
            String keyString = resultSet.getString("key");
            String typeString = resultSet.getString("type");
            String nameFormat = resultSet.getString("name_format");
            int interval = resultSet.getInt("interval");
            String permission = resultSet.getString("permission");
            String data = resultSet.getString("data");

            VisualizerType<?> type = VisualizerHandler.getInstance()
                .getVisualizerType(NamespacedKey.fromString(typeString));
            PathVisualizer<?, ?> visualizer =
                type.create(NamespacedKey.fromString(keyString), nameFormat);
            visualizer.setInterval(interval);
            visualizer.setPermission(permission);
            dataMap.put(visualizer, data);
          }
          HashedRegistry<PathVisualizer<?, ?>> registry = new HashedRegistry<>();
          dataMap.forEach((visualizer, s) -> {
            YamlConfiguration cfg = new YamlConfiguration();
            try {
              cfg.loadFromString(s);
            } catch (InvalidConfigurationException e) {
              e.printStackTrace();
            }
            visualizer.getType().deserialize(visualizer, cfg.getValues(false));
            registry.put(visualizer);
          });
          registry.values().forEach(visualizer -> {
            if (visualizer instanceof CombinedVisualizer combined) {
              combined.resolveReferences(registry.values());
            }
          });
          return registry;
        }
      }
    } catch (Exception e) {
      throw new DataStorageException("Could not load visualizers.", e);
    }
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

    try (Connection connection = getConnection()) {
      try (PreparedStatement stmt = connection.prepareStatement(
          "INSERT INTO `pathfinder_path_visualizer` " +
              "( `key`, `type`, `name_format`, `interval`, `permission`, `data`) VALUES (?, ?, ?, ?, ?, ?) "
              +
              "ON CONFLICT(`key`) DO UPDATE SET " +
              "`name_format` = ?, " +
              "`interval` = ?, " +
              "`permission` = ?, " +
              "`data` = ? ")) {
        stmt.setString(1, visualizer.getKey().toString());
        stmt.setString(2, visualizer.getType().getKey().toString());
        stmt.setString(3, visualizer.getNameFormat());
        stmt.setInt(4, visualizer.getInterval());
        stmt.setString(5, visualizer.getPermission());
        stmt.setString(6, dataString);
        stmt.setString(7, visualizer.getNameFormat());
        stmt.setInt(8, visualizer.getInterval());
        stmt.setString(9, visualizer.getPermission());
        stmt.setString(10, dataString);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataStorageException("Could not update pathvisualizer.", e);
    }
  }

  @Override
  public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
    try (Connection con = getConnection()) {
      try (PreparedStatement stmt = con.prepareStatement(
          "DELETE FROM `pathfinder_path_visualizer` WHERE `key` = ?")) {
        stmt.setString(1, visualizer.getKey().toString());
        stmt.executeUpdate();
      }
    } catch (Exception e) {
      throw new DataStorageException("Could not delete path visualizer.", e);
    }
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
