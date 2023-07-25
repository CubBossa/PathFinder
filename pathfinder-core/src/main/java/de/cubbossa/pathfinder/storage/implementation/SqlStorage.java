package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.group.NodeGroup;
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
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderEdgesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderVisualizerRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderWaypointsRecord;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.HashedRegistry;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;
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

        group.getContentChanges().flush();
        group.getModifierChanges().flush();

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
      node.setLocation(new Location(x, y, z, worldLoader.loadWorld(worldUid)));
      return node;
    };
  }

  public abstract DataSource getDataSource();

  private <T extends PathVisualizer<?, ?>> RecordMapper<PathfinderVisualizerRecord, T> visualizerMapper(
      VisualizerType<T> type) {
    return record -> {
      // create visualizer object
      T visualizer = type.create(record.getKey());
      visualizer.setPermission(record.getPermission());

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
        .using(getDataSource(), dialect, new Settings()
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
  }

  private void createEdgeTable() {
    create
        .createTableIfNotExists(PATHFINDER_EDGES)
        .columns(PATHFINDER_EDGES.fields())
        .primaryKey(PATHFINDER_EDGES.START_ID, PATHFINDER_EDGES.END_ID)
        .execute();
  }

  private void createNodeGroupTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUPS)
        .columns(PATHFINDER_NODEGROUPS.fields())
        .primaryKey(PATHFINDER_NODEGROUPS.KEY)
        .execute();
  }

  private void createNodeGroupNodesTable() {
    create
        .createTableIfNotExists(PATHFINDER_NODEGROUP_NODES)
        .columns(PATHFINDER_NODEGROUP_NODES.fields())
        .primaryKey(PATHFINDER_NODEGROUP_NODES.fields())
        .execute();
  }

  private void createPathVisualizerTable() {
    create
        .createTableIfNotExists(PATHFINDER_VISUALIZER)
        .columns(PATHFINDER_VISUALIZER.fields())
        .primaryKey(PATHFINDER_VISUALIZER.KEY)
        .execute();
  }

  private void createPathVisualizerTypeTable() {
    create
        .createTableIfNotExists(PATHFINDER_VISUALIZER_TYPE_RELATION)
        .columns(PATHFINDER_VISUALIZER_TYPE_RELATION.fields())
        .primaryKey(PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY, PATHFINDER_VISUALIZER_TYPE_RELATION.TYPE_KEY)
        .execute();
  }

  private void createDiscoverInfoTable() {
    create
        .createTableIfNotExists(PATHFINDER_DISCOVERINGS)
        .columns(PATHFINDER_DISCOVERINGS.fields())
        .primaryKey(PATHFINDER_DISCOVERINGS.PLAYER_ID, PATHFINDER_DISCOVERINGS.DISCOVER_KEY)
        .execute();
  }


  private void createNodeTypeRelation() {
    create
        .createTableIfNotExists(PATHFINDER_NODE_TYPE_RELATION)
        .columns(PATHFINDER_NODE_TYPE_RELATION.fields())
        .primaryKey(PATHFINDER_NODE_TYPE_RELATION.NODE_ID, PATHFINDER_NODE_TYPE_RELATION.NODE_TYPE)
        .execute();
  }

  private void createModifierGroupRelation() {
    create
        .createTableIfNotExists(PATHFINDER_GROUP_MODIFIER_RELATION)
        .columns(PATHFINDER_GROUP_MODIFIER_RELATION.fields())
        .primaryKey(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY, PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_KEY)
        .execute();
  }

  @Override
  public Map<UUID, NodeType<?>> loadNodeTypeMapping(Collection<UUID> nodes) {
    Map<UUID, NodeType<?>> result = new HashMap<>();
    create.selectFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.in(nodes))
        .fetch(t -> result.put(t.getNodeId(), nodeTypeRegistry.getType(t.getNodeType())));
    return result;
  }

  @Override
  public void saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping) {
    create.batched(configuration -> {
      typeMapping.forEach((uuid, nodeType) -> {
        create.insertInto(PATHFINDER_NODE_TYPE_RELATION)
            .values(uuid, nodeType.getKey())
            .execute();
      });
    });
  }

  @Override
  public void deleteNodeTypeMapping(Collection<UUID> nodes) {
    create.deleteFrom(PATHFINDER_NODE_TYPE_RELATION)
        .where(PATHFINDER_NODE_TYPE_RELATION.NODE_ID.in(nodes))
        .execute();
  }

  public Edge createAndLoadEdge(UUID start, UUID end, double weight) {
    create.insertInto(PATHFINDER_EDGES)
        .values(start, end, weight)
        .onDuplicateKeyIgnore()
        .execute();
    return new SimpleEdge(start, end, (float) weight);
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesFrom(Collection<UUID> start) {
    Map<UUID, Collection<Edge>> result = new HashMap<>();
    create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(start))
        .fetch(edgeMapper).forEach(edge -> result.computeIfAbsent(edge.getStart(), u -> new HashSet<>()).add(edge));
    return result;
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesTo(Collection<UUID> end) {
    Map<UUID, Collection<Edge>> result = new HashMap<>();
    create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.START_ID.in(end))
        .fetch(edgeMapper).forEach(edge -> result.computeIfAbsent(edge.getEnd(), u -> new HashSet<>()).add(edge));
    return result;
  }

  public Optional<Edge> loadEdge(UUID start, UUID end) {
    return create.selectFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(end))
        .and(PATHFINDER_EDGES.START_ID.eq(start))
        .fetch(edgeMapper).stream().findAny();
  }

  private void saveEdge(DSLContext ctx, Edge edge) {
    ctx.insertInto(PATHFINDER_EDGES)
        .values(edge.getStart(), edge.getEnd(), edge.getWeight())
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_EDGES.WEIGHT, (double) edge.getWeight())
        .where(PATHFINDER_EDGES.END_ID.eq(edge.getEnd()))
        .and(PATHFINDER_EDGES.START_ID.eq(edge.getStart()))
        .execute();
  }

  private void deleteEdge(DSLContext ctx, Edge edge) {
    ctx.deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.eq(edge.getEnd()))
        .and(PATHFINDER_EDGES.START_ID.eq(edge.getStart()))
        .execute();
  }

  @Override
  public void deleteEdgesTo(Collection<UUID> end) {
    create.deleteFrom(PATHFINDER_EDGES)
        .where(PATHFINDER_EDGES.END_ID.in(end))
        .execute();
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
    create.transaction(configuration -> {
      var ctx = configuration.dsl();
      ctx.update(PATHFINDER_WAYPOINTS)
          .set(PATHFINDER_WAYPOINTS.X, waypoint.getLocation().getX())
          .set(PATHFINDER_WAYPOINTS.Y, waypoint.getLocation().getY())
          .set(PATHFINDER_WAYPOINTS.Z, waypoint.getLocation().getZ())
          .set(PATHFINDER_WAYPOINTS.WORLD, waypoint.getLocation().getWorld().getUniqueId())
          .where(PATHFINDER_WAYPOINTS.ID.eq(waypoint.getNodeId()))
          .execute();

      for (Edge e : waypoint.getEdgeChanges().getAddList()) {
        saveEdge(ctx, e);
      }
      for (Edge e : waypoint.getEdgeChanges().getRemoveList()) {
        deleteEdge(ctx, e);
      }
      waypoint.getEdgeChanges().flush();
    });
  }

  @Override
  public void deleteWaypoints(Collection<Waypoint> waypoints) {
    Collection<UUID> ids = waypoints.stream().map(Waypoint::getNodeId).toList();
    create.transaction(configuration -> {
      var ctx = configuration.dsl();
      ctx.deleteFrom(PATHFINDER_WAYPOINTS)
          .where(PATHFINDER_WAYPOINTS.ID.in(ids))
          .execute();
      ctx.deleteFrom(PATHFINDER_EDGES)
          .where(PATHFINDER_EDGES.START_ID.in(ids))
          .or(PATHFINDER_EDGES.END_ID.in(ids))
          .execute();
      ctx.deleteFrom(PATHFINDER_NODEGROUP_NODES)
          .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(ids))
          .execute();
    });
  }

  @Override
  public SimpleNodeGroup createAndLoadGroup(NamespacedKey key) {
    create
        .insertInto(PATHFINDER_NODEGROUPS)
        .values(key, 1)
        .execute();
    return new SimpleNodeGroup(key);
  }

  @Override
  public Optional<NodeGroup> loadGroup(NamespacedKey key) {
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.eq(key))
        .fetch(groupMapper).stream().findAny();
  }

  @Override
  public Collection<UUID> loadGroupNodes(NodeGroup group) {
    return create.select(PATHFINDER_NODEGROUP_NODES.NODE_ID)
        .from(PATHFINDER_NODEGROUP_NODES)
        .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(group.getKey()))
        .fetch(Record1::value1);
  }

  @Override
  public Map<UUID, Collection<NodeGroup>> loadGroups(Collection<UUID> ids) {
    Map<UUID, Collection<NodeGroup>> result = new HashMap<>();
    create.transaction(cfg -> {
      Map<UUID, Collection<NamespacedKey>> mapping = new HashMap<>();
      cfg.dsl().selectFrom(PATHFINDER_NODEGROUP_NODES)
          .where(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(ids))
          .forEach(record -> mapping.computeIfAbsent(record.getNodeId(), uuid -> new HashSet<>()).add(record.getGroupKey()));

      Map<NamespacedKey, NodeGroup> groups = new HashMap<>();
      cfg.dsl().selectFrom(PATHFINDER_NODEGROUPS)
          .where(PATHFINDER_NODEGROUPS.KEY.in(mapping.values().stream()
              .flatMap(Collection::stream).collect(Collectors.toList())))
          .fetch(groupMapper)
          .forEach(group -> groups.put(group.getKey(), group));

      mapping.forEach((uuid, keys) -> result.put(uuid, keys.stream()
          .map(groups::get).collect(Collectors.toSet())));
    });
    return result;
  }

  @Override
  public Collection<NodeGroup> loadGroupsByMod(Collection<NamespacedKey> key) {
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .where(PATHFINDER_NODEGROUPS.KEY.in(key))
        .fetch(groupMapper);
  }

  @Override
  public List<NodeGroup> loadGroups(Range range) {
    return create.selectFrom(PATHFINDER_NODEGROUPS)
        .offset(range.getOffset())
        .limit(range.getLimit())
        .fetch(groupMapper);
  }

  @Override
  public Collection<NodeGroup> loadGroups(UUID node) {
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
  public <M extends Modifier> Collection<NodeGroup> loadGroups(NamespacedKey modifier) {
    return create
        .select()
        .from(PATHFINDER_NODEGROUPS)
        .join(PATHFINDER_GROUP_MODIFIER_RELATION)
        .on(PATHFINDER_NODEGROUPS.KEY.eq(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY))
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_KEY.eq(modifier))
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
    return create.selectFrom(PATHFINDER_NODEGROUPS).fetch(groupMapper);
  }

  @Override
  public void saveGroup(NodeGroup group) {
    create.transaction(configuration -> {
      var ctx = configuration.dsl();
      ctx
          .update(PATHFINDER_NODEGROUPS)
          .set(PATHFINDER_NODEGROUPS.WEIGHT, (double) group.getWeight())
          .where(PATHFINDER_NODEGROUPS.KEY.eq(group.getKey()))
          .execute();
      for (UUID nodeId : group.getContentChanges().getAddList()) {
        ctx
            .insertInto(PATHFINDER_NODEGROUP_NODES)
            .columns(PATHFINDER_NODEGROUP_NODES.GROUP_KEY, PATHFINDER_NODEGROUP_NODES.NODE_ID)
            .values(group.getKey(), nodeId)
            .onDuplicateKeyIgnore()
            .execute();
      }
      group.getContentChanges().getRemoveList().forEach(uuid -> System.out.println(" Removed " + uuid + " from group " + group.getKey()));
      ctx
          .deleteFrom(PATHFINDER_NODEGROUP_NODES)
          .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(group.getKey()))
          .and(PATHFINDER_NODEGROUP_NODES.NODE_ID.in(group.getContentChanges().getRemoveList()))
          .execute();
      group.getContentChanges().flush();
      for (Modifier mod : group.getModifierChanges().getAddList()) {
        assignNodeGroupModifier(ctx, group, mod);
      }
      for (Modifier mod : group.getModifierChanges().getRemoveList()) {
        removeNodeGroupModifier(ctx, group.getKey(), mod.getKey());
      }
      group.getModifierChanges().flush();
    });
  }

  @Override
  public void deleteGroup(NodeGroup group) {
    create.transaction(configuration -> {
      var dsl = configuration.dsl();
      dsl.deleteFrom(PATHFINDER_NODEGROUP_NODES)
          .where(PATHFINDER_NODEGROUP_NODES.GROUP_KEY.eq(group.getKey()))
          .execute();
      dsl.deleteFrom(PATHFINDER_GROUP_MODIFIER_RELATION)
          .where(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY.eq(group.getKey()))
          .execute();
      dsl
          .deleteFrom(PATHFINDER_NODEGROUPS)
          .where(PATHFINDER_NODEGROUPS.KEY.eq(group.getKey()))
          .execute();
    });
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

  public Collection<Modifier> loadModifiers(NamespacedKey group) {
    HashSet<Modifier> modifiers = new HashSet<>();
    create.selectFrom(PATHFINDER_GROUP_MODIFIER_RELATION)
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY.eq(group))
        .forEach(record -> {
          try {
            ModifierType<?> type = modifierRegistry.getType(record.getModifierKey()).orElseThrow();
            YamlConfiguration cfg =
                YamlConfiguration.loadConfiguration(new StringReader(record.getData()));
            Modifier modifier = type.deserialize(cfg.getValues(false));
            modifiers.add(modifier);
          } catch (Throwable t) {
            if (getLogger() != null) {
              getLogger().log(Level.WARNING,
                  "Could not load modifier with class name '" + record.getModifierKey()
                      + "', skipping.", t);
            } else {
              throw new RuntimeException(t);
            }
          }
        });
    return modifiers;
  }

  private <M extends Modifier> void assignNodeGroupModifier(DSLContext ctx, NodeGroup group, M modifier) {
    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new StringReader(""));
    Optional<ModifierType<M>> opt = modifierRegistry.getType(modifier.getKey());
    if (opt.isEmpty()) {
      getLogger().log(Level.SEVERE, "Tried to apply modifier '" + modifier.getClass()
          + "', but could not find according modifier type in type registry.");
      return;
    }
    ModifierType<M> type = opt.orElseThrow();
    type.serialize(modifier).forEach(cfg::set);
    ctx
        .insertInto(PATHFINDER_GROUP_MODIFIER_RELATION)
        .set(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY, group.getKey())
        .set(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_KEY, type.getKey())
        .set(PATHFINDER_GROUP_MODIFIER_RELATION.DATA, cfg.saveToString())
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_GROUP_MODIFIER_RELATION.DATA, cfg.saveToString())
        .execute();
  }

  private void removeNodeGroupModifier(DSLContext ctx, NamespacedKey group, NamespacedKey modifier) {
    ctx.deleteFrom(PATHFINDER_GROUP_MODIFIER_RELATION)
        .where(PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY.eq(group))
        .and(PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_KEY.eq(modifier))
        .execute();
  }

  @Override
  public <T extends PathVisualizer<?, ?>> T createAndLoadInternalVisualizer(VisualizerType<T> type, NamespacedKey key) {
    T visualizer = type.create(key);
    Map<String, Object> data = type.serialize(visualizer);
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
            PATHFINDER_VISUALIZER.PERMISSION,
            PATHFINDER_VISUALIZER.DATA
        )
        .values(
            visualizer.getKey(), type.getKey(), visualizer.getPermission(), dataString
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


  @Override
  public <VisualizerT extends PathVisualizer<?, ?>>
  void saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer) {
    Map<String, Object> data = type.serialize(visualizer);
    if (data == null) {
      return;
    }
    YamlConfiguration cfg = new YamlConfiguration();
    data.forEach(cfg::set);
    String dataString = cfg.saveToString();

    create
        .insertInto(PATHFINDER_VISUALIZER)
        .set(PATHFINDER_VISUALIZER.KEY, visualizer.getKey())
        .set(PATHFINDER_VISUALIZER.TYPE, type.getKey())
        .set(PATHFINDER_VISUALIZER.PERMISSION, visualizer.getPermission())
        .set(PATHFINDER_VISUALIZER.DATA, dataString)
        .onDuplicateKeyUpdate()
        .set(PATHFINDER_VISUALIZER.PERMISSION, visualizer.getPermission())
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
  public void saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> typeMapping) {
    create.batched(configuration -> {
      for (Map.Entry<NamespacedKey, VisualizerType<?>> e : typeMapping.entrySet()) {
        configuration.dsl()
            .insertInto(PATHFINDER_VISUALIZER_TYPE_RELATION)
            .columns(PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY, PATHFINDER_VISUALIZER_TYPE_RELATION.TYPE_KEY)
            .values(e.getKey(), e.getValue().getKey())
            .execute();
      }
    });
  }

  @Override
  public Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypeMapping(Collection<NamespacedKey> key) {
    Map<NamespacedKey, VisualizerType<?>> map = new HashMap<>();
    create.selectFrom(PATHFINDER_VISUALIZER_TYPE_RELATION)
        .where(PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY.in(key))
        .forEach(r -> visualizerTypeRegistry.getType(r.getTypeKey()).ifPresent(t -> {
          map.put(r.getVisualizerKey(), t);
        }));
    return map;
  }

  @Override
  public void deleteVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    create.deleteFrom(PATHFINDER_VISUALIZER_TYPE_RELATION)
        .where(PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY.in(keys))
        .execute();
  }
}
