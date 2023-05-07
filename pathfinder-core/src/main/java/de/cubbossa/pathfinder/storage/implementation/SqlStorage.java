package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.ModifierType;
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
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.jooq.tables.records.*;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.StringUtils;
import de.cubbossa.pathfinder.util.WorldImpl;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jooq.*;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static de.cubbossa.pathfinder.jooq.Tables.PATHFINDER_VISUALIZER;
import static de.cubbossa.pathfinder.jooq.Tables.PATHFINDER_VISUALIZER_TYPE_RELATION;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderEdges.PATHFINDER_EDGES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderGroupModifierRelation.PATHFINDER_GROUP_MODIFIER_RELATION;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodeTypeRelation.PATHFINDER_NODE_TYPE_RELATION;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroups.PATHFINDER_NODEGROUPS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderWaypoints.PATHFINDER_WAYPOINTS;

public abstract class SqlStorage extends CommonStorage {

  private final RecordMapper<? super PathfinderWaypointsRecord, Waypoint> nodeMapper;
  private final RecordMapper<? super PathfinderEdgesRecord, Edge> edgeMapper =
      record -> {
        UUID startId = record.getStartId();
        UUID endId = record.getEndId();
        double weight = record.getWeight();

        return new SimpleEdge(startId, endId, (float) weight);
      };

  private final SQLDialect dialect;

  private DSLContext create;

  private final RecordMapper<? super PathfinderNodegroupsRecord, NodeGroup> groupMapper =
      record -> {
        NamespacedKey key = record.getKey();
        SimpleNodeGroup group = new SimpleNodeGroup(key);
        group.addAll(loadGroupNodes(group));
        loadModifiers(group.getKey()).forEach(group::addModifier);
        group.setWeight(record.getWeight().floatValue());
        return group;
      };

  public SqlStorage(SQLDialect dialect, NodeTypeRegistry nodeTypeRegistry,
                    ModifierRegistry modifierRegistry,
                    VisualizerTypeRegistry visualizerTypeRegistry) {
    super(nodeTypeRegistry, visualizerTypeRegistry, modifierRegistry);
    this.dialect = dialect;

    nodeMapper = record -> {
      UUID id = record.getId();
      double x = record.getX();
      double y = record.getY();
      double z = record.getZ();
      UUID worldUid = record.getWorld();

      Waypoint node = new Waypoint(id);
      node.setLocation(new Location(x, y, z, new WorldImpl(worldUid)));
      return node;
    };
  }

  public abstract ConnectionProvider getConnectionProvider();

  private <T extends PathVisualizer<?, ?>> RecordMapper<PathfinderVisualizerRecord, T> visualizerMapper(
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

  @Override
  public void init() throws Exception {

    System.setProperty("org.jooq.no-logo", "true");
    System.setProperty("org.jooq.no-tips", "true");

    create = DSL
        .using(getConnectionProvider(), dialect, new Settings()
            .withRenderQuotedNames(RenderQuotedNames.ALWAYS)
            .withRenderSchema(dialect != SQLDialect.SQLITE));

    createPathVisualizerTable();
    createPathVisualizerTypeTable();
    createNodeTable();
    createNodeGroupTable();
    createNodeGroupNodesTable();
    createEdgeTable();
    createDiscoverInfoTable();
    createNodeTypeRelation();
    createModifierGroupRelation();
  }

  private void createNodeTable() {
    create
        .createTableIfNotExists(PATHFINDER_WAYPOINTS)
        .columns(PATHFINDER_WAYPOINTS.fields())
        .primaryKey(PATHFINDER_WAYPOINTS.ID)
        .execute();
    debug("Table created: 'pathfinder_waypoints'");
  }

  private void createEdgeTable() {
    create
        .createTableIfNotExists(PATHFINDER_EDGES)
        .columns(PATHFINDER_EDGES.fields())
        .primaryKey(PATHFINDER_EDGES.START_ID, PATHFINDER_EDGES.END_ID)
        .execute();
    debug("Table created: 'pathfinder_edges'");
  }

  private void createNodeGroupTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUPS)
        .columns(PATHFINDER_NODEGROUPS.fields())
        .primaryKey(PATHFINDER_NODEGROUPS.KEY)
        .execute();
    debug("Table created: 'pathfinder_nodegroups'");
  }

  private void createNodeGroupNodesTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUP_NODES)
        .columns(PATHFINDER_NODEGROUP_NODES.fields())
        .primaryKey(PATHFINDER_NODEGROUP_NODES.fields())
        .execute();
    debug("Table created: 'pathfinder_nodegroup_nodes'");
  }

  private void createPathVisualizerTable() {
    create
        .createTableIfNotExists(PATHFINDER_VISUALIZER)
        .columns(PATHFINDER_VISUALIZER.fields())
        .primaryKey(PATHFINDER_VISUALIZER.KEY)
        .execute();
    debug("Table created: 'pathfinder_path_visualizer'");
  }

  private void createPathVisualizerTypeTable() {
    create
        .createTableIfNotExists(PATHFINDER_VISUALIZER_TYPE_RELATION)
        .columns(PATHFINDER_VISUALIZER_TYPE_RELATION.fields())
        .primaryKey(PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY, PATHFINDER_VISUALIZER_TYPE_RELATION.TYPE_KEY)
        .execute();
    debug("Table created: 'pathfinder_visualizer_type_relation'");
  }

  private void createDiscoverInfoTable() {
    create
        .createTableIfNotExists(PATHFINDER_DISCOVERINGS)
        .columns(PATHFINDER_DISCOVERINGS.fields())
        .primaryKey(PATHFINDER_DISCOVERINGS.PLAYER_ID, PATHFINDER_DISCOVERINGS.DISCOVER_KEY)
        .execute();
    debug("Table created: 'pathfinder_discoverings'");
  }


  private void createNodeTypeRelation() {
    create
        .createTableIfNotExists(PATHFINDER_NODE_TYPE_RELATION)
        .columns(PATHFINDER_NODE_TYPE_RELATION.fields())
        .primaryKey(PATHFINDER_NODE_TYPE_RELATION.NODE_ID, PATHFINDER_NODE_TYPE_RELATION.NODE_TYPE)
        .execute();
    debug("Table created: 'pathfinder_node_type_relation'");
  }

  private void createModifierGroupRelation() {
    create
        .createTableIfNotExists(PATHFINDER_GROUP_MODIFIER_RELATION)
        .columns(PATHFINDER_GROUP_MODIFIER_RELATION.fields())
        .primaryKey(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY, PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_CLASS)
        .execute();
    debug("Table created: 'pathfinder_group_modifier_relation'");
  }

  @Override
  public <N extends Node> Optional<NodeType<N>> loadNodeType(UUID node) {
    List<NodeType<N>> resultSet = create
        .selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.eq(node))
        .fetch(t -> nodeTypeRegistry.getType(t.getNodeType()));
    debug(" > Storage Implementation: 'loadNodeType(UUID): Optional<NodeType<N>>'");
    return resultSet.stream().findFirst();
  }

  @Override
  public Map<UUID, NodeType<?>> loadNodeTypes(Collection<UUID> nodes) {
    Map<UUID, NodeType<?>> result = new HashMap<>();
    create.selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.in(nodes))
        .fetch(t -> result.put(t.getNodeId(), nodeTypeRegistry.getType(t.getNodeType())));
    debug(" > Storage Implementation: 'loadNodeTypes(Collection<UUID>): Map<UUID, NodeType<N>>'");
    return result;
  }

  @Override
  public void saveNodeType(UUID node, NodeType<?> type) {
    debug(" > Storage Implementation: 'saveNodeType(" + node + ", " + type.getKey() + ")'");
    create
        .insertInto(PATHFINDER_NODE_TYPE_RELATION)
        .values(node, type.getKey())
        .execute();
  }

  @Override
  public void saveNodeTypes(Map<UUID, NodeType<?>> typeMapping) {
    debug(" > Storage Implementation: 'saveNodeTypes(" + typeMapping.entrySet().stream()
        .map(e -> "{" + e.getKey() + "; " + e.getValue().getKey() + "}")
        .collect(Collectors.joining(", ")) + ")'");
    create.batched(configuration -> {
      typeMapping.forEach((uuid, nodeType) -> {
        create.insertInto(PATHFINDER_NODE_TYPE_RELATION)
            .values(uuid, nodeType)
            .execute();
      });
    });
  }

  @Override
  public void deleteNodes(Collection<Node> nodes) {
    debug(" > Storage Implementation: 'deleteNodes(" + nodes.stream()
        .map(Node::getNodeId).map(UUID::toString).collect(Collectors.joining(",")) + ")'");
    Collection<UUID> ids = nodes.stream().map(Node::getNodeId).toList();
    Map<UUID, NodeType<?>> types = loadNodeTypes(ids);
    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(ids))
        .execute();
    create
        .deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(ids))
        .or(PATHFINDER_EDGES.END_ID.in(ids))
        .execute();
    for (Node node : nodes) {
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
        .onDuplicateKeyIgnore()
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

  @Override
  public Waypoint createAndLoadWaypoint(Location l) {
    UUID uuid = UUID.randomUUID();
    create
        .insertInto(PATHFINDER_WAYPOINTS)
        .values(uuid, l.getWorld() == null ? null : l.getWorld().getUniqueId(), l.getX(), l.getY(),
            l.getZ())
        .execute();
    Waypoint waypoint = new Waypoint(uuid);
    waypoint.setLocation(l);
    return waypoint;
  }

  @Override
  public Optional<Waypoint> loadWaypoint(UUID uuid) {
    return create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.eq(uuid))
        .fetch(nodeMapper).stream().findFirst();
  }

  @Override
  public Collection<Waypoint> loadAllWaypoints() {
    return new ArrayList<>(create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .fetch(nodeMapper));
  }

  @Override
  public Collection<Waypoint> loadWaypoints(Collection<UUID> uuids) {
    return new ArrayList<>(create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.in(uuids))
        .fetch(nodeMapper));
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
    debug(" > Storage Implementation: 'loadGroups(" + key.stream().map(NamespacedKey::toString)
        .collect(Collectors.joining(",")) + ")'");
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
          loadModifiers(group.getKey()).forEach(group::addModifier);
          group.addAll(loadGroupNodes(group));
          return group;
        });
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
          loadModifiers(group.getKey()).forEach(group::addModifier);
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
    debug(" > Storage Implementation: 'saveGroup(" + group.getKey() + ")'");
    NodeGroup before = loadGroup(group.getKey()).orElseThrow();
    create
        .update(PATHFINDER_NODEGROUPS)
        .set(PATHFINDER_NODEGROUPS.WEIGHT, (double) group.getWeight())
        .where(PATHFINDER_NODEGROUPS.KEY.eq(group.getKey()))
        .execute();
    StorageImpl.ComparisonResult<UUID> cmp = StorageImpl.ComparisonResult.compare(before, group);
    cmp.toInsertIfPresent(uuids -> assignToGroups(List.of(group), uuids));
    cmp.toDeleteIfPresent(uuids -> unassignFromGroups(List.of(group), uuids));

    StorageImpl.ComparisonResult<Modifier> cmpMod =
        StorageImpl.ComparisonResult.compare(before.getModifiers(), group.getModifiers());
    cmpMod.toInsertIfPresent(mods -> mods.forEach(m -> assignNodeGroupModifier(group.getKey(), m)));
    cmpMod.toDeleteIfPresent(
        mods -> mods.forEach(m -> unassignNodeGroupModifier(group.getKey(), m.getClass())));
  }

  public void assignToGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {
    debug(" > Storage Implementation: 'assignToGroups("
        + "[" + groups.stream().map(NodeGroup::getKey).map(NamespacedKey::toString)
        .collect(Collectors.joining(",")) + "]"
        + ", [" + nodes.stream().map(UUID::toString).collect(Collectors.joining(",")) + "])'");
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
  }

  public void unassignFromGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {
    debug(" > Storage Implementation: 'unassignFromGroups("
        + "[" + groups.stream().map(NodeGroup::getKey).map(NamespacedKey::toString)
        .collect(Collectors.joining(",")) + "]"
        + ", [" + nodes.stream().map(UUID::toString).collect(Collectors.joining(",")) + "])'");
    if (groups.isEmpty() || nodes.isEmpty()) {
      return;
    }
    Collection<NamespacedKey> keys = groups.stream().map(NodeGroup::getKey).toList();
    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.in(keys))
        .and(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(nodes))
        .execute();
  }

  @Override
  public void deleteGroup(NodeGroup group) {
    create.deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(group.getKey()))
        .execute();
    create.deleteFrom(PATHFINDER_GROUP_MODIFIER_RELATION)
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY.eq(group.getKey()))
        .execute();
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
      for (Node startNode : start) {
        for (Node endNode : end) {
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
      for (Node startNode : start) {
        for (Node endNode : end) {
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

  public Collection<Modifier> loadModifiers(NamespacedKey group) {
    debug(" > Storage Implementation: 'loadModifiers(" + group.getKey() + ")'");
    HashSet<Modifier> modifiers = new HashSet<>();
    create.selectFrom(PATHFINDER_GROUP_MODIFIER_RELATION)
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY.eq(group))
        .forEach(record -> {
          try {
            ModifierType<?> type =
                modifierRegistry.getType(record.getModifierClass()).orElseThrow();
            YamlConfiguration cfg =
                YamlConfiguration.loadConfiguration(new StringReader(record.getData()));
            Modifier modifier = type.deserialize(cfg.getValues(false));
            modifiers.add(modifier);
          } catch (Throwable t) {
            if (getLogger() != null) {
              getLogger().log(Level.WARNING,
                  "Could not load modifier with class name '" + record.getModifierClass()
                      + "', skipping.");
            } else {
              throw new RuntimeException(t);
            }
          }
        });
    return modifiers;
  }

  @Override
  public <M extends Modifier> void assignNodeGroupModifier(NamespacedKey group, M modifier) {
    debug(" > Storage Implementation: 'assignNodeGroupModifier(" + group.getKey() + ")'");
    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new StringReader(""));
    ModifierType<M> type = modifierRegistry.getType((Class<M>) modifier.getClass()).orElseThrow();
    type.serialize(modifier).forEach(cfg::set);
    create
        .insertInto(PATHFINDER_GROUP_MODIFIER_RELATION)
        .set(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY, group)
        .set(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_CLASS, type.getModifierClass().getName())
        .set(PATHFINDER_GROUP_MODIFIER_RELATION.DATA, cfg.saveToString())
        .execute();
  }

  @Override
  public <M extends Modifier> void unassignNodeGroupModifier(NamespacedKey group,
                                                             Class<M> modifier) {
    debug(" > Storage Implementation: 'unassignNodeGroupModifier(" + group.getKey() + ")'");
    create.deleteFrom(PATHFINDER_GROUP_MODIFIER_RELATION)
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY.eq(group))
        .and(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_CLASS.eq(modifier.getName()))
        .execute();
  }

  @Override
  public <T extends PathVisualizer<?, ?>> T createAndLoadInternalVisualizer(VisualizerType<T> type, NamespacedKey key) {
    T visualizer = type.create(key, StringUtils.toDisplayNameFormat(key));
    Map<String, Object> data = serialize(visualizer);
    if (data == null) {
      throw new IllegalStateException("Could not serialize internal visualizer '" + key + "', data is null.");
    }
    YamlConfiguration cfg = new YamlConfiguration();
    data.forEach(cfg::set);
    String dataString = cfg.saveToString();

    create
        .insertInto(PATHFINDER_VISUALIZER)
        .columns(
            PATHFINDER_VISUALIZER.KEY,
            PATHFINDER_VISUALIZER.TYPE,
            PATHFINDER_VISUALIZER.NAME_FORMAT,
            PATHFINDER_VISUALIZER.PERMISSION,
            PATHFINDER_VISUALIZER.INTERVAL,
            PATHFINDER_VISUALIZER.DATA
        )
        .values(
            visualizer.getKey(), resolve(visualizer).getKey(),
            visualizer.getNameFormat(), visualizer.getPermission(),
            visualizer.getInterval(), dataString
        )
        .execute();
    return visualizer;
  }

  @Override
  public <T extends PathVisualizer<?, ?>> Optional<T> loadInternalVisualizer(VisualizerType<T> type, NamespacedKey key) {
    return create
        .selectFrom(PATHFINDER_VISUALIZER)
        .where(PATHFINDER_VISUALIZER.KEY.eq(key))
        .fetch(visualizerMapper(type))
        .stream().findFirst();
  }

  @Override
  public <T extends PathVisualizer<?, ?>> Map<NamespacedKey, T> loadInternalVisualizers(VisualizerType<T> type) {
    HashedRegistry<T> registry = new HashedRegistry<>();
    create
        .selectFrom(PATHFINDER_VISUALIZER)
        .where(PATHFINDER_VISUALIZER.TYPE.eq(type.getKey()))
        .fetch(visualizerMapper(type))
        .forEach(registry::put);
    return registry;
  }


  private <VisualizerT extends PathVisualizer<?, ?>> Map<String, Object> serialize(
      VisualizerT pathVisualizer) {
    VisualizerType<VisualizerT> type = resolve(pathVisualizer);
    return type.serialize(pathVisualizer);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void saveInternalVisualizer(VisualizerT visualizer) {
    Map<String, Object> data = serialize(visualizer);
    if (data == null) {
      return;
    }
    YamlConfiguration cfg = new YamlConfiguration();
    data.forEach(cfg::set);
    String dataString = cfg.saveToString();

    create
        .insertInto(PATHFINDER_VISUALIZER)
        .columns(
            PATHFINDER_VISUALIZER.KEY,
            PATHFINDER_VISUALIZER.TYPE,
            PATHFINDER_VISUALIZER.NAME_FORMAT,
            PATHFINDER_VISUALIZER.PERMISSION,
            PATHFINDER_VISUALIZER.INTERVAL,
            PATHFINDER_VISUALIZER.DATA
        )
        .values(
            visualizer.getKey(), resolve(visualizer).getKey(),
            visualizer.getNameFormat(), visualizer.getPermission(),
            visualizer.getInterval(), dataString
        )
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_VISUALIZER.KEY, visualizer.getKey())
        .set(PATHFINDER_VISUALIZER.TYPE, resolve(visualizer).getKey())
        .set(PATHFINDER_VISUALIZER.NAME_FORMAT, visualizer.getNameFormat())
        .set(PATHFINDER_VISUALIZER.PERMISSION, visualizer.getPermission())
        .set(PATHFINDER_VISUALIZER.INTERVAL, visualizer.getInterval())
        .set(PATHFINDER_VISUALIZER.DATA, dataString)
        .execute();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void deleteInternalVisualizer(VisualizerT visualizer) {
    create
        .deleteFrom(PATHFINDER_VISUALIZER)
        .where(PATHFINDER_VISUALIZER.KEY.eq(visualizer.getKey()))
        .execute();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void saveVisualizerType(NamespacedKey key,
                                                                            VisualizerType<VisualizerT> type) {
    create
        .insertInto(PATHFINDER_VISUALIZER_TYPE_RELATION)
        .values(key, type.getKey())
        .execute();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> loadVisualizerType(
      NamespacedKey key) {
    Optional<NamespacedKey> typeKey = create.selectFrom(PATHFINDER_VISUALIZER_TYPE_RELATION)
        .where(PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY.eq(key))
        .fetch(PathfinderVisualizerTypeRelationRecord::getTypeKey)
        .stream().findAny();
    if (typeKey.isEmpty()) {
      return Optional.empty();
    }
    return typeKey
        .map(visualizerTypeRegistry::<VisualizerT>getType)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  @Override
  public Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypes(Collection<NamespacedKey> key) {
    Map<NamespacedKey, VisualizerType<?>> map = new HashMap<>();
    create.selectFrom(PATHFINDER_VISUALIZER_TYPE_RELATION)
        .where(PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY.in(key))
        .forEach(r -> visualizerTypeRegistry.getType(r.getTypeKey()).ifPresent(t -> {
          map.put(r.getVisualizerKey(), t);
        }));
    return map;
  }
}
