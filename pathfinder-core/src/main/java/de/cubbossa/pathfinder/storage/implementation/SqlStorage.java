package de.cubbossa.pathfinder.storage.implementation;

import static de.cubbossa.pathfinder.jooq.tables.PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderEdges.PATHFINDER_EDGES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderGroupModifierRelation.PATHFINDER_GROUP_MODIFIER_RELATION;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodeTypeRelation.PATHFINDER_NODE_TYPE_RELATION;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroups.PATHFINDER_NODEGROUPS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderWaypoints.PATHFINDER_WAYPOINTS;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.NodeDataStorage;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderEdgesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderPathVisualizerRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderWaypointsRecord;
import de.cubbossa.pathfinder.storage.Storage;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.WorldImpl;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;

public abstract class SqlStorage implements StorageImplementation, WaypointDataStorage {

  @Getter
  @Setter
  private @Nullable Logger logger;
  public abstract ConnectionProvider getConnectionProvider();

  private DSLContext create;

  private void debug(String message) {
    if (logger != null) logger.log(Level.INFO, message);
  }

  // +-----------------------------------------+
  // |  Node Table                             |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderWaypointsRecord, Waypoint> nodeMapper;

  // +-----------------------------------------+
  // |  Edge Table                             |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderEdgesRecord, Edge> edgeMapper =
      record -> {
        UUID startId = record.getStartId();
        UUID endId = record.getEndId();
        double weight = record.getWeight();

        return new SimpleEdge(startId, endId, (float) weight);
      };

  // +-----------------------------------------+
  // |  Nodegroup Table                        |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderNodegroupsRecord, NodeGroup> groupMapper =
      record -> {
        NamespacedKey key = record.getKey();
        SimpleNodeGroup group = new SimpleNodeGroup(key);
        group.addAll(loadGroupNodes(group));
        group.setWeight(record.getWeight().floatValue());
        return group;
      };

  //

  private <T extends PathVisualizer<T, ?, ?>> RecordMapper<PathfinderPathVisualizerRecord, T> visualizerMapper(
      VisualizerType<T> type) {
    return record -> {
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
    };
  }

  private final SQLDialect dialect;
  @Getter
  private final NodeTypeRegistry nodeTypeRegistry;

  public SqlStorage(SQLDialect dialect, NodeTypeRegistry nodeTypeRegistry) {
    this.dialect = dialect;
    this.nodeTypeRegistry = nodeTypeRegistry;

    nodeMapper = record -> {
      UUID id = record.getId();
      double x = record.getX();
      double y = record.getY();
      double z = record.getZ();
      UUID worldUid = record.getWorld();

      Waypoint node = new Waypoint(nodeTypeRegistry.getWaypointNodeType(), id);
      node.setLocation(new Location(x, y, z, new WorldImpl(worldUid)));
      return node;
    };
  }

  @Override
  public void init() throws Exception {

    System.setProperty("org.jooq.no-logo", "true");
    System.setProperty("org.jooq.no-tips", "true");

    create = DSL
        .using(getConnectionProvider(), dialect, new Settings()
            .withRenderQuotedNames(RenderQuotedNames.ALWAYS)
            .withRenderSchema(dialect != SQLDialect.SQLITE));

    createPathVisualizerTable();
    createNodeTable();
    createNodeGroupTable();
    createNodeGroupSearchTermsTable();
    createNodeGroupNodesTable();
    createEdgeTable();
    createDiscoverInfoTable();
    createNodeTypeRelation();
  }

  private void createNodeTable() {
    create
        .createTableIfNotExists(PATHFINDER_WAYPOINTS)
        .columns(PATHFINDER_WAYPOINTS.fields())
        .execute();
    debug("Table created: 'pathfinder_waypoints'");
  }

  private void createEdgeTable() {
    create
        .createTableIfNotExists(PATHFINDER_EDGES)
        .columns(PATHFINDER_EDGES.fields())
        .execute();
    debug("Table created: 'pathfinder_edges'");
  }

  private void createNodeGroupTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUPS)
        .columns(PATHFINDER_NODEGROUPS.fields())
        .execute();
    debug("Table created: 'pathfinder_nodegroups'");
  }

  private void createNodeGroupSearchTermsTable() {
    create
        .createTableIfNotExists(PATHFINDER_SEARCH_TERMS)
        .columns(PATHFINDER_SEARCH_TERMS.fields())
        .execute();
    debug("Table created: 'pathfinder_search_terms'");
  }

  private void createNodeGroupNodesTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUP_NODES)
        .columns(PATHFINDER_NODEGROUP_NODES.fields())
        .execute();
    debug("Table created: 'pathfinder_nodegroup_nodes'");
  }

  private void createPathVisualizerTable() {
    create
        .createTableIfNotExists(PATHFINDER_PATH_VISUALIZER)
        .columns(PATHFINDER_PATH_VISUALIZER.fields())
        .execute();
    debug("Table created: 'pathfinder_path_visualizer'");
  }

  private void createDiscoverInfoTable() {
    create
        .createTableIfNotExists(PATHFINDER_DISCOVERINGS)
        .columns(PATHFINDER_DISCOVERINGS.fields())
        .execute();
    debug("Table created: 'pathfinder_discoverings'");
  }


  private void createNodeTypeRelation() {
    create
        .createTableIfNotExists(PATHFINDER_NODE_TYPE_RELATION)
        .columns(PATHFINDER_NODE_TYPE_RELATION.fields())
        .execute();
    debug("Table created: 'pathfinder_node_type_relation'");
  }

  @Override
  public <N extends Node<N>> Optional<NodeType<N>> loadNodeType(UUID node) {
    List<NodeType<N>> resultSet = create
        .selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.eq(node))
        .fetch(t -> nodeTypeRegistry.getType(t.getNodeType()));
    debug(" > Storage Implementation: 'loadNodeType(UUID): Optional<NodeType<N>>'");
    return resultSet.stream().findFirst();
  }

  @Override
  public Map<UUID, NodeType<? extends Node<?>>> loadNodeTypes(Collection<UUID> nodes) {
    Map<UUID, NodeType<? extends Node<?>>> result = new HashMap<>();
    create.selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.in(nodes))
        .fetch(t -> result.put(t.getNodeId(), nodeTypeRegistry.getType(t.getNodeType())));
    debug(" > Storage Implementation: 'loadNodeTypes(Collection<UUID>): Map<UUID, NodeType<N>>'");
    return result;
  }

  @Override
  public <N extends Node<N>> N createAndLoadNode(NodeType<N> type, Location location) {
    debug(" > Storage Implementation: 'createAndLoadNode(" + type.getKey() + ", " + location + ")'");
    N node = type.createAndLoadNode(new NodeDataStorage.Context(location));
    saveNodeType(node.getNodeId(), type);
    return node;
  }

  @Override
  public <N extends Node<N>> Optional<N> loadNode(UUID id) {
    debug(" > Storage Implementation: 'loadNode(" + id + ")'");
    Optional<NodeType<N>> type = loadNodeType(id);
    if (type.isPresent()) {
      return type.get().loadNode(id);
    }
    throw new IllegalStateException("No type found for node with UUID '" + id + "'.");
  }

  @Override
  public Collection<Node<?>> loadNodes() {
    debug(" > Storage Implementation: 'loadNodes()'");
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadAllNodes().stream())
        .collect(Collectors.toList());
  }

  @Override
  public Collection<Node<?>> loadNodes(Collection<UUID> ids) {
    debug(" > Storage Implementation: 'loadNodes(" + ids.stream()
        .map(UUID::toString).collect(Collectors.joining(", ")) + ")'");
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadNodes(ids).stream())
        .collect(Collectors.toList());
  }

  @Override
  public void saveNode(Node<?> node) {
    debug(" > Storage Implementation: 'saveNode(" + node.getNodeId() + ")'");
    saveNodeTyped(node);
  }

  private <N extends Node<N>> void saveNodeTyped(Node<?> node) {
    NodeType<N> type = (NodeType<N>) node.getType();
    N before = type.loadNode(node.getNodeId()).orElseThrow();
    type.saveNode((N) node);

    if (before instanceof Groupable<?> gBefore && node instanceof Groupable<?> gAfter) {
      Storage.ComparisonResult<NodeGroup> cmp = Storage.ComparisonResult.compare(gBefore.getGroups(), gAfter.getGroups());
      cmp.toInsertIfPresent(nodeGroups -> assignToGroups(nodeGroups, List.of(node.getNodeId())));
      cmp.toDeleteIfPresent(nodeGroups -> unassignFromGroups(nodeGroups, List.of(node.getNodeId())));
    }
    Storage.ComparisonResult<Edge> cmp = Storage.ComparisonResult.compare(before.getEdges(), node.getEdges());
    cmp.toInsertIfPresent(edges -> {
      for (Edge edge : edges) {
        createAndLoadEdge(edge.getStart(), edge.getEnd(), edge.getWeight());
      }
    });
    cmp.toDeleteIfPresent(edges -> edges.forEach(this::deleteEdge));
  }

  @Override
  public void saveNodeType(UUID node, NodeType<? extends Node<?>> type) {
    debug(" > Storage Implementation: 'saveNodeType(" + node + ", " + type.getKey() + ")'");
    create
        .insertInto(PATHFINDER_NODE_TYPE_RELATION)
        .values(node, type.getKey())
        .execute();
  }

  @Override
  public void saveNodeTypes(Map<UUID, NodeType<? extends Node<?>>> typeMapping) {
    debug(" > Storage Implementation: 'saveNodeTypes(" + typeMapping.entrySet().stream()
            .map(e -> "{" + e.getKey() + "; " + e.getValue().getKey() + "}").collect(Collectors.joining(", ")) + ")'");
    create.batched(configuration -> {
      typeMapping.forEach((uuid, nodeType) -> {
        create.insertInto(PATHFINDER_NODE_TYPE_RELATION)
            .values(uuid, nodeType)
            .execute();
      });
    });
  }

  @Override
  public void deleteNodes(Collection<Node<?>> nodes) {
    debug(" > Storage Implementation: 'deleteNodes(" + nodes.stream()
        .map(Node::getNodeId).map(UUID::toString).collect(Collectors.joining(",")) + ")'");
    Collection<UUID> ids = nodes.stream().map(Node::getNodeId).toList();
    Map<UUID, NodeType<? extends Node<?>>> types = loadNodeTypes(ids);
    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(ids))
        .execute();
    create
        .deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(ids))
        .or(PATHFINDER_EDGES.END_ID.in(ids))
        .execute();
    for (Node<?> node : nodes) {
      deleteNode(node, types.get(node.getNodeId()));
    }
    create
        .deleteFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.in(ids))
        .execute();
  }

  @Override
  public Edge createAndLoadEdge(UUID start, UUID end, double weight) {
    create.insertInto(PATHFINDER_EDGES)
        .values(start, end, weight)
        .execute();
    debug(" > Storage Implementation: 'createAndLoadEdge(" + start + ", " + end + ")'");
    return new SimpleEdge(start, end, (float) weight);
  }

  @Override
  public Collection<Edge> loadEdgesFrom(UUID start) {
    debug(" > Storage Implementation: 'loadEdgesFrom(" + start + ")'");
    return create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start))
        .fetch(edgeMapper);
  }

  @Override
  public Collection<Edge> loadEdgesTo(UUID end) {
    debug(" > Storage Implementation: 'loadEdgesTo(" + end + ")'");
    return create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end))
        .fetch(edgeMapper);
  }

  @Override
  public Optional<Edge> loadEdge(UUID start, UUID end) {
    debug(" > Storage Implementation: 'loadEdge(" + start + ", " + end + ")'");
    return create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end))
        .and(PATHFINDER_EDGES.START_ID.eq(start))
        .fetch(edgeMapper).stream().findAny();
  }

  @Override
  public void saveEdge(Edge edge) {
    create.update(PATHFINDER_EDGES)
        .set(PATHFINDER_EDGES.WEIGHT, (double) edge.getWeight())
        .where(PATHFINDER_EDGES.END_ID.eq(edge.getEnd()))
        .and(PATHFINDER_EDGES.START_ID.eq(edge.getStart()))
        .execute();
    debug(" > Storage Implementation: 'saveEdge(" + edge + ")'");
  }

  @Override
  public void deleteEdge(Edge edge) {
    create.deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(edge.getEnd()))
        .and(PATHFINDER_EDGES.START_ID.eq(edge.getStart()))
        .execute();
    debug(" > Storage Implementation: 'deleteEdge(" + edge + ")'");
  }

  private void deleteNode(Node node, NodeType type) {
    type.deleteNode(node);
  }

  @Override
  public Waypoint createAndLoadWaypoint(Location l) {
    UUID uuid = UUID.randomUUID();
    create
        .insertInto(PATHFINDER_WAYPOINTS)
        .values(uuid, l.getWorld() == null ? null : l.getWorld().getUniqueId(), l.getX(), l.getY(),
            l.getZ())
        .execute();
    Waypoint waypoint = new Waypoint(nodeTypeRegistry.getWaypointNodeType(), uuid);
    waypoint.setLocation(l);
    return waypoint;
  }

  @Override
  public Optional<Waypoint> loadWaypoint(UUID uuid) {
    return create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.eq(uuid))
        .fetch(nodeMapper).stream()
        .map(this::insertGroups)
        .map(this::insertEdges).findFirst();
  }

  @Override
  public Collection<Waypoint> loadAllWaypoints() {
    return create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .fetch(nodeMapper).stream()
        .map(this::insertGroups)
        .map(this::insertEdges)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<Waypoint> loadWaypoints(Collection<UUID> uuids) {
    return create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.in(uuids))
        .fetch(nodeMapper).stream()
        .map(this::insertGroups)
        .map(this::insertEdges)
        .collect(Collectors.toList());
  }

  @Override
  public void saveWaypoint(Waypoint waypoint) {
    create.update(PATHFINDER_WAYPOINTS)
        .set(PATHFINDER_WAYPOINTS.X, waypoint.getLocation().getX())
        .set(PATHFINDER_WAYPOINTS.Y, waypoint.getLocation().getY())
        .set(PATHFINDER_WAYPOINTS.Z, waypoint.getLocation().getZ())
        .set(PATHFINDER_WAYPOINTS.WORLD, waypoint.getLocation().getWorld().getUniqueId())
        .where(PATHFINDER_WAYPOINTS.ID.eq(waypoint.getNodeId()))
        .execute();
  }

  @Override
  public void deleteWaypoints(Collection<Waypoint> waypoints) {
    Collection<UUID> ids = waypoints.stream().map(Waypoint::getNodeId).toList();
    create.deleteFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.in(ids))
        .execute();
  }

  @Override
  public SimpleNodeGroup createAndLoadGroup(NamespacedKey key) {
    create
        .insertInto(PATHFINDER_NODEGROUPS)
        .values(key, 1)
        .execute();
    debug(" > Storage Implementation: 'createAndLoadGroup(" + key + ")'");
    return new SimpleNodeGroup(key);
  }

  @Override
  public Optional<NodeGroup> loadGroup(NamespacedKey key) {
    debug(" > Storage Implementation: 'loadGroup(" + key + ")'");
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(key))
        .fetch(groupMapper).stream().findAny();
  }

  @Override
  public Collection<UUID> loadGroupNodes(NodeGroup group) {
    debug(" > Storage Implementation: 'loadGroupNodes(" + group.getKey() + ")'");
    return create.select(PATHFINDER_NODEGROUP_NODES.NODE_ID)
        .from(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(group.getKey()))
        .fetch(Record1::value1);
  }

  @Override
  public Collection<NodeGroup> loadGroups(Collection<NamespacedKey> key) {
    debug(" > Storage Implementation: 'loadGroups(" + key.stream().map(NamespacedKey::toString).collect(Collectors.joining(",")) + ")'");
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.in(key))
        .fetch(groupMapper);
  }

  @Override
  public List<NodeGroup> loadGroups(Pagination pagination) {
    debug(" > Storage Implementation: 'loadGroups(" + pagination + ")'");
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .offset(pagination.getOffset())
        .limit(pagination.getLimit())
        .fetch(groupMapper);
  }

  @Override
  public Collection<NodeGroup> loadGroups(UUID node) {
    debug(" > Storage Implementation: 'loadGroups(" + node + ")'");
    return create.select().from(PATHFINDER_NODEGROUPS)
        .join(PATHFINDER_NODEGROUP_NODES)
        .on(PATHFINDER_NODEGROUPS.KEY.eq(PATHFINDER_NODEGROUP_NODES.GROUP_KEY))
        .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.eq(node))
        .fetch(record -> {
          SimpleNodeGroup group = new SimpleNodeGroup(record.get(PATHFINDER_NODEGROUPS.KEY));
          group.setWeight(record.get(PATHFINDER_NODEGROUPS.WEIGHT).floatValue());
          group.addAll(loadGroupNodes(group));
          return group;
        });
  }

  private Waypoint insertGroups(Waypoint waypoint) {
    debug(" > Storage Implementation: 'insertGroups(" + waypoint.getNodeId() + ")'");
    loadGroups(waypoint.getNodeId()).forEach(waypoint::addGroup);
    return waypoint;
  }

  private Waypoint insertEdges(Waypoint waypoint) {
    debug(" > Storage Implementation: 'insertEdges(" + waypoint.getNodeId() + ")'");
    waypoint.getEdges().addAll(loadEdgesFrom(waypoint.getNodeId()));
    return waypoint;
  }

  @Override
  public <M extends Modifier> Collection<NodeGroup> loadGroups(Class<M> modifier) {
    debug(" > Storage Implementation: 'loadGroups(" + modifier.getSimpleName() + ")'");
    return create
        .select()
        .from(PATHFINDER_NODEGROUPS)
        .join(PATHFINDER_GROUP_MODIFIER_RELATION)
        .on(PATHFINDER_NODEGROUPS.KEY.eq(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY))
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_CLASS.eq(modifier.getName()))
        .fetch(r -> {
          SimpleNodeGroup group = new SimpleNodeGroup(
              r.getValue(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY)
          );
          group.addAll(loadGroupNodes(group));
          loadModifiers(group.getKey()).join().forEach(group::addModifier);
          return group;
        });
  }

  @Override
  public Collection<NodeGroup> loadAllGroups() {
    debug(" > Storage Implementation: 'loadAllGroups()'");
    return create.selectFrom(PATHFINDER_NODEGROUPS).fetch(groupMapper);
  }

  @Override
  public void saveGroup(NodeGroup group) {
    NodeGroup before = loadGroup(group.getKey()).orElseThrow();
    create
        .update(PATHFINDER_NODEGROUPS)
        .set(PATHFINDER_NODEGROUPS.WEIGHT, (double) group.getWeight())
        .where(PATHFINDER_NODEGROUPS.KEY.eq(group.getKey()))
        .execute();
    Storage.ComparisonResult<UUID> cmp = Storage.ComparisonResult.compare(before, group);
    cmp.toInsertIfPresent(uuids -> assignToGroups(List.of(group), uuids));
    cmp.toDeleteIfPresent(uuids -> unassignFromGroups(List.of(group), uuids));
    debug(" > Storage Implementation: 'saveGroup(" + group.getKey() + ")'");
  }

  public void assignToGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {
    if (groups.isEmpty() || nodes.isEmpty()) {
      return;
    }
    create.batched(configuration -> {
      for (UUID nodeId : nodes) {
        for (NodeGroup group : groups) {
          DSL.using(configuration)
              .insertInto(PATHFINDER_NODEGROUP_NODES)
              .columns(PATHFINDER_NODEGROUP_NODES.GROUP_KEY, PATHFINDER_NODEGROUP_NODES.NODE_ID)
              .values(group.getKey(), nodeId)
              .onDuplicateKeyIgnore()
              .execute();
        }
      }
    });
    debug(" > Storage Implementation: 'assignToGroups("
        + "[" + groups.stream().map(NodeGroup::getKey).map(NamespacedKey::toString).collect(Collectors.joining(",")) + "]"
        + ", [" + nodes.stream().map(UUID::toString).collect(Collectors.joining(",")) + "])'");
  }

  public void unassignFromGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {
    if (groups.isEmpty() || nodes.isEmpty()) {
      return;
    }
    Collection<NamespacedKey> keys = groups.stream().map(NodeGroup::getKey).toList();
    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.in(keys))
        .and(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(nodes))
        .execute();
    debug(" > Storage Implementation: 'unassignFromGroups("
        + "[" + groups.stream().map(NodeGroup::getKey).map(NamespacedKey::toString).collect(Collectors.joining(",")) + "]"
        + ", [" + nodes.stream().map(UUID::toString).collect(Collectors.joining(",")) + "])'");
  }

  @Override
  public void deleteGroup(NodeGroup group) {
    create
        .deleteFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(group.getKey()))
        .execute();
    debug(" > Storage Implementation: 'deleteGroup(" + group.getKey() + ")'");
  }

  @Override
  public DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                LocalDateTime time) {
    create
        .insertInto(PATHFINDER_DISCOVERINGS)
        .values(key, player, time)
        .onDuplicateKeyIgnore()
        .execute();
    return new DiscoverInfo(player, key, time);
  }

  @Override
  public Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key) {
    return create
        .selectFrom(PATHFINDER_DISCOVERINGS)
        .where(PATHFINDER_DISCOVERINGS.PLAYER_ID.eq(player))
        .and(PATHFINDER_DISCOVERINGS.DISCOVER_KEY.eq(key))
        .fetch(record -> {
          NamespacedKey k = record.getDiscoverKey();
          LocalDateTime date = record.getDate();
          return new DiscoverInfo(player, k, date);
        }).stream().findAny();
  }

  @Override
  public void deleteDiscoverInfo(DiscoverInfo info) {
    create
        .deleteFrom(PATHFINDER_DISCOVERINGS)
        .where(PATHFINDER_DISCOVERINGS.PLAYER_ID.eq(info.playerId()))
        .and(PATHFINDER_DISCOVERINGS.DISCOVER_KEY.eq(info.discoverable()))
        .execute();
  }

  // ###############################################################################################

  public CompletableFuture<SimpleEdge> connectNodes(UUID start, UUID end, double weight) {
    create
        .insertInto(PATHFINDER_EDGES)
        .values(start, end, weight)
        .execute();
    return CompletableFuture.completedFuture(new SimpleEdge(start, end, (float) weight));
  }

  public CompletableFuture<Collection<SimpleEdge>> connectNodes(NodeSelection start,
                                                                NodeSelection end) {
    CompletableFuture<Collection<SimpleEdge>> future = new CompletableFuture<>();
    create.batched(configuration -> {
      Collection<SimpleEdge> edges = new HashSet<>();
      for (Node<?> startNode : start) {
        for (Node<?> endNode : end) {
          DSL.using(configuration)
              .insertInto(PATHFINDER_EDGES)
              .values(startNode.getNodeId(), endNode.getNodeId(), 1)
              .execute();
          edges.add(new SimpleEdge(startNode.getNodeId(), endNode.getNodeId(), 1));
        }
      }
      future.complete(edges);
    });
    return future;
  }

  public CompletableFuture<Void> disconnectNodes(UUID start, UUID end) {
    create
        .deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start))
        .and(PATHFINDER_EDGES.END_ID.eq(end))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    create.batched(configuration -> {
      for (Node<?> startNode : start) {
        for (Node<?> endNode : end) {
          DSL.using(configuration)
              .deleteFrom(PATHFINDER_EDGES)
              .where(PATHFINDER_EDGES.START_ID.eq(startNode.getNodeId()))
              .and(PATHFINDER_EDGES.END_ID.eq(endNode.getNodeId()))
              .execute();
        }
      }
      future.complete(null);
    });
    return future;
  }

  public CompletableFuture<Void> disconnectNodes(NodeSelection start) {
    create
        .deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(start))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  public CompletableFuture<Collection<Edge>> getConnections(UUID start) {
    Collection<Edge> edges = create
        .selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start))
        .fetch(edgeMapper);
    return CompletableFuture.completedFuture(edges);
  }

  public CompletableFuture<Collection<Edge>> getConnectionsTo(UUID end) {
    Collection<Edge> edges = create
        .selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end))
        .fetch(edgeMapper);
    return CompletableFuture.completedFuture(edges);
  }

  public CompletableFuture<Collection<Edge>> getConnectionsTo(NodeSelection end) {
    Collection<Edge> edges = create
        .selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.in(end))
        .fetch(edgeMapper);
    return CompletableFuture.completedFuture(edges);
  }

  public CompletableFuture<Collection<Modifier>> loadModifiers(NamespacedKey group) {
    return CompletableFuture.completedFuture(new HashSet<>());
  }

  public CompletableFuture<Void> assignNodeGroupModifier(NamespacedKey group, Modifier modifier) {
    return null;
  }

  public CompletableFuture<Void> unassignNodeGroupModifier(NamespacedKey group,
                                                           Class<? extends Modifier> modifier) {
    return null;
  }

  @Override
  public <T extends PathVisualizer<T, ?, ?>> T createAndLoadVisualizer(VisualizerType<T> type,
                                                                       NamespacedKey key) {
    return null;
  }

  @Override
  public <T extends PathVisualizer<T, ?, ?>> Optional<T> loadVisualizer(VisualizerType<T> type,
                                                                        NamespacedKey key) {
    return create
        .selectFrom(PATHFINDER_PATH_VISUALIZER)
        .where(PATHFINDER_PATH_VISUALIZER.KEY.eq(key))
        .fetch(visualizerMapper(type))
        .stream().findFirst();
  }

  @Override
  public <T extends PathVisualizer<T, ?, ?>> Map<NamespacedKey, T> loadVisualizers(
      VisualizerType<T> type) {
    HashedRegistry<T> registry = new HashedRegistry<>();
    create
        .selectFrom(PATHFINDER_PATH_VISUALIZER)
        .where(PATHFINDER_PATH_VISUALIZER.TYPE.eq(type.getKey()))
        .fetch(visualizerMapper(type))
        .forEach(registry::put);
    return registry;
  }


  private <T extends PathVisualizer<T, ?, ?>> Map<String, Object> serialize(
      PathVisualizer<?, ?, ?> pathVisualizer) {
    VisualizerType<T> type = ((T) pathVisualizer).getType();
    return type.serialize((T) pathVisualizer);
  }

  @Override
  public void saveVisualizer(PathVisualizer<?, ?, ?> visualizer) {
    Map<String, Object> data = serialize(visualizer);
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
  public void deleteVisualizer(PathVisualizer<?, ?, ?> visualizer) {
    create
        .deleteFrom(PATHFINDER_PATH_VISUALIZER)
        .where(PATHFINDER_PATH_VISUALIZER.KEY.eq(visualizer.getKey()))
        .execute();
  }
}
