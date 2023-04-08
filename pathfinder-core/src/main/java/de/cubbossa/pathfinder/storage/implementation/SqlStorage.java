package de.cubbossa.pathfinder.storage.implementation;

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
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderEdgesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderWaypointsRecord;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.storage.DiscoverInfo;
import de.cubbossa.pathfinder.storage.NodeDataStorage;
import de.cubbossa.pathfinder.storage.StorageImplementation;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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

public abstract class SqlStorage implements StorageImplementation {

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
    World world = Bukkit.getWorld(worldUid);
    node.setLocation(new Location(world, x, y, z));
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
  @Getter
  private final NodeTypeRegistry nodeTypeRegistry;

  public SqlStorage(SQLDialect dialect, NodeTypeRegistry nodeTypeRegistry) {
    this.dialect = dialect;
    this.nodeTypeRegistry = nodeTypeRegistry;
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
  public <N extends Node<N>> Optional<NodeType<N>> loadNodeType(UUID node) {
    List<NodeType<N>> resultSet = create
        .selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.eq(node))
        .fetch(t -> nodeTypeRegistry.getNodeType(t.getNodeType()));
    return resultSet.stream().findFirst();
  }

  @Override
  public Map<UUID, NodeType<?>> loadNodeTypes(Collection<UUID> nodes) {
    Map<UUID, NodeType<?>> result = new HashMap<>();
    create.selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.in(nodes))
        .fetch(t -> result.put(t.getNodeId(), nodeTypeRegistry.getNodeType(t.getNodeType())));
    return result;
  }

  @Override
  public <N extends Node<N>> N createAndLoadNode(NodeType<N> type, Location location) {
    return type.createAndLoadNode(new NodeDataStorage.Context(location));
  }

  @Override
  public <N extends Node<N>> Optional<N> loadNode(UUID id) {
    Optional<NodeType<N>> type = loadNodeType(id);
    if (type.isPresent()) {
      return type.get().loadNode(id);
    }
    throw new IllegalStateException("No type found for node with UUID '" + id + "'.");
  }

  @Override
  public <N extends Node<N>> Optional<N> loadNode(NodeType<N> type, UUID id) {
    return type.loadNode(id);
  }

  @Override
  public Collection<Node<?>> loadNodes() {
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadAllNodes().stream())
        .collect(Collectors.toList());
  }

  @Override
  public Collection<Node<?>> loadNodes(Collection<UUID> ids) {
    return nodeTypeRegistry.getTypes().stream()
        .flatMap(nodeType -> nodeType.loadNodes(ids).stream())
        .collect(Collectors.toList());
  }

  @Override
  public <N extends Node<N>> void saveNode(N node) {
    node.getType().saveNode(node);
  }

  @Override
  public void saveNodeType(UUID node, NodeType<?> type) {
    create
        .insertInto(PATHFINDER_NODE_TYPE_RELATION)
        .values(node, type)
        .execute();
  }

  @Override
  public void saveNodeTypes(Map<UUID, NodeType<?>> typeMapping) {
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
    return new Edge(start, end, (float) weight);
  }

  @Override
  public Collection<Edge> loadEdgesFrom(UUID start) {
    return create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.eq(start))
        .fetch(edgeMapper);
  }

  @Override
  public Collection<Edge> loadEdgesTo(UUID end) {
    return create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end))
        .fetch(edgeMapper);
  }

  @Override
  public Optional<Edge> loadEdge(UUID start, UUID end) {
    return create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end))
        .and(PATHFINDER_EDGES.START_ID.eq(start))
        .fetch(edgeMapper).stream().findAny();
  }

  @Override
  public void saveEdge(Edge edge) {
    create.update(PATHFINDER_EDGES)
        .set(PATHFINDER_EDGES.WEIGHT, (double) edge.getWeightModifier())
        .where(PATHFINDER_EDGES.END_ID.eq(edge.getEnd()))
        .and(PATHFINDER_EDGES.START_ID.eq(edge.getStart()))
        .execute();
  }

  @Override
  public void deleteEdge(Edge edge) {
    create.deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(edge.getEnd()))
        .and(PATHFINDER_EDGES.START_ID.eq(edge.getStart()))
        .execute();
  }

  private void deleteNode(Node node, NodeType type) {
    type.deleteNode(node);
  }

  @Override
  public Waypoint createAndLoadWaypoint(Location l) {
    UUID uuid = UUID.randomUUID();
    create
        .insertInto(PATHFINDER_WAYPOINTS)
        .values(uuid, l.getWorld() == null ? null : l.getWorld().getUID(), l.getX(), l.getY(), l.getZ())
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
        .set(PATHFINDER_WAYPOINTS.WORLD, waypoint.getLocation().getWorld().getUID())
        .execute();
    //TODO save edges
    //TODO save groups
  }

  @Override
  public void deleteWaypoints(Collection<Waypoint> waypoints) {
    Collection<UUID> ids = waypoints.stream().map(Waypoint::getNodeId).toList();
    create.deleteFrom(PATHFINDER_WAYPOINTS)
        .where(PATHFINDER_WAYPOINTS.ID.in(ids))
        .execute();
  }

  @Override
  public NodeGroup createAndLoadGroup(NamespacedKey key) {
    create
        .insertInto(PATHFINDER_NODEGROUPS)
        .values(key, 1)
        .execute();
    return new NodeGroup(key);
  }

  @Override
  public Optional<NodeGroup> loadGroup(NamespacedKey key) {
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(key))
        .fetch(groupMapper).stream()
        .peek(group -> group.addAll(loadGroupNodes(group)))
        .findAny();
  }

  @Override
  public Collection<Node<?>> loadGroupNodes(NodeGroup group) {
    Collection<UUID> ids = create.select(PATHFINDER_NODEGROUP_NODES.NODE_ID)
        .from(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(group.getKey()))
        .fetch(Record1::value1);
    return loadNodes(ids);
  }

  @Override
  public Collection<NodeGroup> loadGroups(Collection<NamespacedKey> key) {
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.in(key))
        .fetch(groupMapper).stream()
        .peek(group -> group.addAll(loadGroupNodes(group)))
        .collect(Collectors.toList());
  }

  @Override
  public Collection<NodeGroup> loadGroups(UUID node) {
    return create.select().from(PATHFINDER_NODEGROUPS)
        .join(PATHFINDER_NODEGROUP_NODES)
        .on(PATHFINDER_NODEGROUPS.KEY.eq(PATHFINDER_NODEGROUP_NODES.GROUP_KEY))
        .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.eq(node))
        .fetch(record -> {
          NodeGroup group = new NodeGroup(record.get(PATHFINDER_NODEGROUPS.KEY));
          group.setWeight(record.get(PATHFINDER_NODEGROUPS.WEIGHT));
          group.addAll(loadGroupNodes(group));
          return group;
        });
  }

  private Waypoint insertGroups(Waypoint waypoint) {
    loadGroups(waypoint.getNodeId()).forEach(waypoint::addGroup);
    return waypoint;
  }

  private Waypoint insertEdges(Waypoint waypoint) {
    waypoint.getEdges().addAll(loadEdgesFrom(waypoint.getNodeId()));
    return waypoint;
  }

  @Override
  public <M extends Modifier> Collection<NodeGroup> loadGroups(Class<M> modifier) {
    return create
        .select()
        .from(PATHFINDER_NODEGROUPS)
        .join(PATHFINDER_GROUP_MODIFIER_RELATION)
        .on(PATHFINDER_NODEGROUPS.KEY.eq(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY))
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_CLASS.eq(modifier.getName()))
        .fetch(r -> {
          NodeGroup group = new NodeGroup(
              r.getValue(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY)
          );
          group.addAll(loadGroupNodes(group));
          loadModifiers(group.getKey()).join().forEach(group::addModifier);
          return group;
        });
  }

  @Override
  public Collection<NodeGroup> loadAllGroups() {
    Map<NamespacedKey, NodeGroup> groups = new HashMap<>();
    Collection<UUID> idSet = new HashSet<>();
    Map<NamespacedKey, Collection<UUID>> ids = new HashMap<>();

    create.selectFrom(PATHFINDER_NODEGROUPS)
        .fetch(groupMapper).forEach(group -> groups.put(group.getKey(), group));
    create.selectFrom(PATHFINDER_NODEGROUP_NODES)
        .forEach(record -> {
          idSet.add(record.getNodeId());
          ids.computeIfAbsent(record.getGroupKey(), key -> new HashSet<>()).add(record.getNodeId());
        });
    Map<UUID, Node<?>> nodes = new HashMap<>();
    loadNodes(idSet).forEach(node -> nodes.put(node.getNodeId(), node));

    ids.forEach((key, uuids) -> {
      Collection<Node<?>> n = new HashSet<>();
      uuids.forEach(uuid -> n.add(nodes.get(uuid)));
      groups.get(key).addAll(n);
    });
    return groups.values();
  }

  @Override
  public void saveGroup(NodeGroup group) {
    create
        .update(PATHFINDER_NODEGROUPS)
        .set(PATHFINDER_NODEGROUPS.WEIGHT, group.getWeight())
        .where(PATHFINDER_NODEGROUPS.KEY.eq(group.getKey()))
        .execute();
  }

  @Override
  public void deleteGroup(NodeGroup group) {
    create
        .deleteFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(group.getKey()))
        .execute();
  }

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

  public CompletableFuture<Void> removeNodesFromGroup(NamespacedKey key, NodeSelection selection) {
    create
        .deleteFrom(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(key))
        .and(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(selection))
        .execute();
    return CompletableFuture.completedFuture(null);
  }

  // ###############################################################################################

  public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
    create
        .insertInto(PATHFINDER_EDGES)
        .values(start, end, weight)
        .execute();
    return CompletableFuture.completedFuture(new Edge(start, end, (float) weight));
  }

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

  public DiscoverInfo createDiscoverInfo(UUID player, NodeGroup discoverable,
                                         LocalDateTime foundDate) {
    create
        .insertInto(PATHFINDER_DISCOVERINGS)
        .values(discoverable.getKey().toString(), player, foundDate)
        .onDuplicateKeyIgnore()
        .execute();
    return new DiscoverInfo(player, discoverable.getKey(), foundDate);
  }

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

  public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
    create
        .deleteFrom(PATHFINDER_DISCOVERINGS)
        .where(PATHFINDER_DISCOVERINGS.PLAYER_ID.eq(playerId))
        .and(PATHFINDER_DISCOVERINGS.DISCOVER_KEY.eq(discoverKey))
        .execute();
  }

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

  public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
    create
        .deleteFrom(PATHFINDER_PATH_VISUALIZER)
        .where(PATHFINDER_PATH_VISUALIZER.KEY.eq(visualizer.getKey()))
        .execute();
  }
}
