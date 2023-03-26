package de.cubbossa.pathfinder.data;

import static de.cubbossa.pathfinder.jooq.Tables.PATHFINDER_GROUP_MODIFIER_RELATION;
import static de.cubbossa.pathfinder.jooq.Tables.PATHFINDER_NODE_TYPE_RELATION;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderEdges.PATHFINDER_EDGES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroups.PATHFINDER_NODEGROUPS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS;
import static de.cubbossa.pathfinder.jooq.tables.PathfinderWaypoints.PATHFINDER_WAYPOINTS;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderEdgesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupNodesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderWaypointsRecord;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
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

public abstract class SqlDataStorage implements DataStorage {

  abstract ConnectionProvider getConnectionProvider();

  private DSLContext create;

  // +-----------------------------------------+
  // |  Node Table                             |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderWaypointsRecord, Waypoint> nodeMapper = record -> {
    UUID id = record.getId();
    double x = record.getX();
    double y = record.getY();
    double z = record.getZ();
    UUID worldUid = record.getWorld();

    Waypoint node = new Waypoint(id);
    node.setLocation(new Location(Bukkit.getWorld(worldUid), x, y, z));
    return node;
  };

  // +-----------------------------------------+
  // |  Edge Table                             |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderEdgesRecord, Edge> edgeMapper =
      record -> {
        UUID startId = record.getStartId();
        UUID endId = record.getEndId();
        double weight = record.getWeight();

        return new Edge(startId, endId, (float) weight);
      };

  // +-----------------------------------------+
  // |  Nodegroup Table                        |
  // +-----------------------------------------+

  private final RecordMapper<? super PathfinderNodegroupsRecord, NodeGroup> groupMapper =
      record -> {
        NamespacedKey key = record.getKey();
        NodeGroup group = new NodeGroup(key);
        group.setWeight(record.getWeight());
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


  private void createNodeTypeRelation() {
    create
        .createTableIfNotExists(PATHFINDER_NODE_TYPE_RELATION)
        .columns(PATHFINDER_NODE_TYPE_RELATION.fields())
        .execute();
  }

  @Override
  public CompletableFuture<NodeType<?>> getNodeType(UUID nodeId) {
    NodeType<?> type = create
        .selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.eq(nodeId))
        .fetch(t -> NodeHandler.getInstance().getNodeType(t.getNodeType()))
        .get(0);
    return CompletableFuture.completedFuture(type);
  }

  @Override
  public CompletableFuture<Void> setNodeType(UUID nodeId, NamespacedKey nodeType) {
    create
        .insertInto(PATHFINDER_NODE_TYPE_RELATION)
        .values(nodeId, nodeType)
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Waypoint> createNodeInStorage(NodeType.NodeCreationContext context) {
    UUID uuid = UUID.randomUUID();
    create
        .insertInto(PATHFINDER_WAYPOINTS)
        .values(
            uuid,
            context.location().getWorld() == null ? null : context.location().getWorld().getUID(),
            context.location().getX(),
            context.location().getY(),
            context.location().getZ()
        )
        .execute();
    Waypoint waypoint = new Waypoint(uuid);
    waypoint.setLocation(context.location());
    return CompletableFuture.completedFuture(waypoint);
  }

  private CompletableFuture<Collection<Waypoint>> insertGroups(Collection<Waypoint> waypoints) {
    Collection<CompletableFuture<?>> futures = new ArrayList<>();
    for (Waypoint waypoint : waypoints) {
      futures.add(getNodeGroups(waypoint.getNodeId()).thenAccept(namespacedKeys -> {
        namespacedKeys.forEach(waypoint::addGroup);
      }));
    }
    return CompletableFuture
        .allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(u -> waypoints);
  }

  @Override
  public CompletableFuture<Waypoint> getNodeFromStorage(UUID id) {
    Waypoint waypoint = create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.eq(id))
        .fetch(nodeMapper)
        .get(0);
    return insertGroups(List.of(waypoint)).thenApply(waypoints -> waypoints.toArray(Waypoint[]::new)[0]);
  }

  @Override
  public CompletableFuture<Collection<Waypoint>> getNodesFromStorage() {
    Collection<Waypoint> waypoints = create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .fetch(nodeMapper);
    return insertGroups(waypoints);
  }

  @Override
  public CompletableFuture<Collection<Waypoint>> getNodesFromStorage(NodeSelection ids) {
    Collection<Waypoint> waypoints = create
        .selectFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.in(ids))
        .fetch(nodeMapper);
    return insertGroups(waypoints);
  }

  @Override
  public CompletableFuture<Void> updateNodesInStorage(NodeSelection nodeIds,
                                                      Consumer<Waypoint> nodeConsumer) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    create.batched(configuration -> {
      NodeHandler.WAYPOINT_TYPE.getNodesFromStorage(nodeIds).thenAccept(nodes -> {
        DSLContext c = DSL.using(configuration);
        for (Node<?> node : nodes) {
          if (!(node instanceof Waypoint waypoint)) {
            continue;
          }
          nodeConsumer.accept(waypoint);
          c.update(PATHFINDER_WAYPOINTS)
              .set(PATHFINDER_WAYPOINTS.X, waypoint.getLocation().getX())
              .set(PATHFINDER_WAYPOINTS.Y, waypoint.getLocation().getY())
              .set(PATHFINDER_WAYPOINTS.Z, waypoint.getLocation().getZ())
              .set(PATHFINDER_WAYPOINTS.WORLD, waypoint.getLocation().getWorld().getUID())
              .execute();
        }
        future.complete(null);
      });
    });
    return future;
  }

  @Override
  public CompletableFuture<Void> deleteNodesFromStorage(NodeSelection nodes) {
    create
        .deleteFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.in(nodes))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> deleteNodes(NodeSelection nodes) {
    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(nodes))
        .execute();
    create
        .deleteFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.in(nodes))
        .execute();
    create
        .deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(nodes))
        .or(PATHFINDER_EDGES.END_ID.in(nodes))
        .execute();
    return DataStorage.super.deleteNodes(nodes);
  }

  @Override
  public CompletableFuture<Collection<Node<?>>> getNodes() {
    Collection<CompletableFuture<?>> futures = new ArrayList<>();
    Collection<Node<?>> nodes = new HashSet<>();
    // for each future add nodes to hashset
    NodeHandler.getInstance().getTypes().values().stream()
        .map(NodeDataStorage::getNodesFromStorage)
        .peek(f -> f.thenAccept(nodes::addAll))
        .forEach(futures::add);
    // when all have added nodes to hashset, return hashset as future
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(unused -> nodes);
  }

  @Override
  public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
    create
        .insertInto(PATHFINDER_EDGES)
        .values(start, end, weight)
        .execute();
    return CompletableFuture.completedFuture(new Edge(start, end, (float) weight));
  }

  @Override
  public CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end) {
    CompletableFuture<Collection<Edge>> future = new CompletableFuture<>();
    create.batched(configuration -> {
      Collection<Edge> edges = new HashSet<>();
      for (UUID startNode : start) {
        for (UUID endNode : end) {
          DSL.using(configuration)
              .insertInto(PATHFINDER_EDGES)
              .values(startNode, endNode, 1)
              .execute();
          edges.add(new Edge(startNode, endNode, 1));
        }
      }
      future.complete(edges);
    });
    return future;
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(UUID start, UUID end) {
    create
        .deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start))
        .and(PATHFINDER_EDGES.END_ID.eq(end))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    create.batched(configuration -> {
      for (UUID startNode : start) {
        for (UUID endNode : end) {
          DSL.using(configuration)
              .deleteFrom(PATHFINDER_EDGES)
              .where(PATHFINDER_EDGES.START_ID.eq(startNode))
              .and(PATHFINDER_EDGES.END_ID.eq(endNode))
              .execute();
        }
      }
      future.complete(null);
    });
    return future;
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start) {
    create
        .deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(start))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnections(UUID start) {
    Collection<Edge> edges = create
        .selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start))
        .fetch(edgeMapper);
    return CompletableFuture.completedFuture(edges);
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnectionsTo(UUID end) {
    Collection<Edge> edges = create
        .selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end))
        .fetch(edgeMapper);
    return CompletableFuture.completedFuture(edges);
  }

  @Override
  public CompletableFuture<Collection<Edge>> getConnectionsTo(NodeSelection end) {
    Collection<Edge> edges = create
        .selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.in(end))
        .fetch(edgeMapper);
    return CompletableFuture.completedFuture(edges);
  }

  @Override
  public CompletableFuture<Void> updateNodeInStorage(UUID id, Consumer<Waypoint> consumer) {
    return NodeHandler.WAYPOINT_TYPE.getNodeFromStorage(id).thenAccept(node -> {
      consumer.accept(node);
      create
          .update(PATHFINDER_WAYPOINTS)
          .set(PATHFINDER_WAYPOINTS.X, node.getLocation().getX())
          .set(PATHFINDER_WAYPOINTS.Y, node.getLocation().getY())
          .set(PATHFINDER_WAYPOINTS.Z, node.getLocation().getZ())
          .set(PATHFINDER_WAYPOINTS.WORLD, node.getLocation().getWorld().getUID())
          .execute();
    });
  }

  @Override
  public CompletableFuture<Collection<NamespacedKey>> getNodeGroups(UUID node) {
    return CompletableFuture.completedFuture(create
        .selectFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.eq(node))
        .fetch(PathfinderNodegroupNodesRecord::getGroupKey));
  }

  @Override
  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> getNodeGroups(
      Class<M> modifier) {
    Collection<NodeGroup> groups = create
        .select()
        .from(PATHFINDER_NODEGROUPS)
        .join(PATHFINDER_GROUP_MODIFIER_RELATION)
        .on(PATHFINDER_NODEGROUPS.KEY.eq(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY))
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_CLASS.eq(modifier.getName()))
        .fetch(r -> {
          NodeGroup group = new NodeGroup(
              r.getValue(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY)
          );
          group.addAll(getNodeGroupNodes(group.getKey()).join());
          loadModifiers(group.getKey()).join().forEach(group::addModifier);
          return group;
        });
    return CompletableFuture.completedFuture(groups);
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroup(NamespacedKey group, NodeSelection selection) {
    create.batched(configuration -> {
      for (UUID nodeId : selection) {
        DSL.using(configuration)
            .insertInto(PATHFINDER_NODEGROUP_NODES)
            .values(group, nodeId)
            .onDuplicateKeyIgnore()
            .execute();
      }
    });
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> removeNodesFromGroup(NamespacedKey key, NodeSelection selection) {
    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(key))
        .and(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(selection))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> clearNodeGroups(NodeSelection selection) {
    return null;
  }

  @Override
  public CompletableFuture<Collection<UUID>> getNodeGroupNodes(NamespacedKey group) {
    Collection<UUID> nodes = new HashSet<>();
    create
        .selectFrom(PATHFINDER_NODEGROUP_NODES)
        .fetch()
        .stream()
        .map(PathfinderNodegroupNodesRecord::getNodeId)
        .forEach(nodes::add);
    return CompletableFuture.completedFuture(nodes);
  }

  @Override
  public CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet() {
    return CompletableFuture.completedFuture(
        create
            .select(PATHFINDER_NODEGROUPS.KEY)
            .from(PATHFINDER_NODEGROUPS)
            .stream()
            .map(Record1::value1)
            .collect(Collectors.toList())
    );
  }

  @Override
  public CompletableFuture<NodeGroup> getNodeGroup(NamespacedKey key) {
    NodeGroup group = create
        .selectFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(key))
        .fetch(groupMapper)
        .get(0);
    return getNodeGroupNodes(key).thenAccept(group::addAll).thenApply(unused -> group);
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> getNodeGroups() {
    return CompletableFuture.completedFuture(
        create
            .selectFrom(PATHFINDER_NODEGROUPS)
            .fetch(groupMapper)
    );
  }

  @Override
  public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
    return CompletableFuture.completedFuture(
        create
            .selectFrom(PATHFINDER_NODEGROUPS)
            .offset(pagination.offset())
            .limit(pagination.limit())
            .fetch(groupMapper)
    );
  }

  @Override
  public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
    create
        .insertInto(PATHFINDER_NODEGROUPS)
        .values(key, 1)
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> updateNodeGroup(NamespacedKey group,
                                                 Consumer<NodeGroup> modifier) {
    return getNodeGroup(group).thenAccept(g -> {
      modifier.accept(g);
      create
          .update(PATHFINDER_NODEGROUPS)
          .set(PATHFINDER_NODEGROUPS.WEIGHT, g.getWeight())
          .where(PATHFINDER_NODEGROUPS.KEY.eq(group))
          .execute();
    });
  }

  @Override
  public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
    create
        .deleteFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(key))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  public CompletableFuture<Collection<Modifier>> loadModifiers(NamespacedKey group) {
    return CompletableFuture.completedFuture(new HashSet<>());
  }

  @Override
  public CompletableFuture<Void> assignNodeGroupModifier(NamespacedKey group, Modifier modifier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> unassignNodeGroupModifier(NamespacedKey group,
                                                           Class<? extends Modifier> modifier) {
    return null;
  }

  @Override
  public DiscoverInfo createDiscoverInfo(UUID player, NodeGroup discoverable,
                                         LocalDateTime foundDate) {
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
        .where(PATHFINDER_DISCOVERINGS.PLAYER_ID.eq(playerId))
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
        .where(PATHFINDER_DISCOVERINGS.PLAYER_ID.eq(playerId))
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
}
