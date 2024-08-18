package de.cubbossa.pathfinder;

import de.cubbossa.disposables.Disposer;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.group.ModifierRegistry;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.Task;
import de.cubbossa.pathfinder.misc.World;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.EdgeImpl;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.GroupedNodeImpl;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.NodeSelectionImpl;
import de.cubbossa.pathfinder.node.NodeType;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifierImpl;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.StorageAdapter;
import de.cubbossa.pathfinder.storage.StorageAdapterImpl;
import de.cubbossa.pathfinder.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.storage.WorldLoader;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import de.cubbossa.pathfinder.storage.implementation.VisualizerStorageImplementationWrapper;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Assertions;

public abstract class PathFinderTest {

  public static final WorldLoader WORLD_LOADER = uuid -> new World() {
    @Override
    public UUID getUniqueId() {
      return uuid;
    }

    @Override
    public String getName() {
      return getUniqueId().toString();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof World w && uuid.equals(w.getUniqueId());
    }
  };
  protected static World world;
  protected static Logger logger = Logger.getLogger("TESTS");
  protected static MiniMessage miniMessage;

  protected PathFinder pathFinder;

  protected Disposer disposer;
  protected StorageAdapterImpl storage;
  protected NodeTypeRegistryImpl nodeTypeRegistry;
  protected VisualizerTypeRegistry visualizerTypeRegistry;
  protected ModifierRegistry modifierRegistry;
  protected NodeType<Waypoint> waypointNodeType;
  protected TestVisualizerType visualizerType;
  protected NodeGroup globalGroup;

  private Map<PathVisualizer<?, ?>, NodeGroup> groupMap;

  public void setupPathFinder() {
    setupPathFinder(true, this::inMemoryStorage);
  }

  public void setupPathFinder(boolean cached, Supplier<StorageImplementation> factory) {
    groupMap = new HashMap<>();
    pathFinder = new PathFinder() {
      @Override
      public Logger getLogger() {
        return logger;
      }

      @Override
      public ApplicationState getState() {
        return ApplicationState.RUNNING;
      }

      @Override
      public void load() {

      }

      @Override
      public void shutdown() {

      }

      @Override
      public void shutdownExceptionally(Throwable t) {

      }

      @Override
      public Disposer getDisposer() {
        return disposer;
      }

      @Override
      public StorageAdapter getStorage() {
        return storage;
      }

      @Override
      public ExtensionsRegistry getExtensionRegistry() {
        return null;
      }

      @Override
      public EventDispatcher<?> getEventDispatcher() {
        return null;
      }

      @Override
      public ModifierRegistry getModifierRegistry() {
        return modifierRegistry;
      }

      @Override
      public NodeTypeRegistry getNodeTypeRegistry() {
        return null;
      }

      @Override
      public VisualizerTypeRegistry getVisualizerTypeRegistry() {
        return visualizerTypeRegistry;
      }

      @Override
      public PathFinderConfig getConfiguration() {
        return new PathFinderConfigImpl();
      }

      @Override
      public String getVersion() {
        return "1.0.0";
      }

      @Override
      public File getDataFolder() {
        return new File("src/test/resources/");
      }

      @Override
      public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
      }

      @Override
      public AudienceProvider getAudiences() {
        return null;
      }

      @Override
      public Task repeatingTask(Runnable runnable, long delay, long interval) {
        return null;
      }

      @Override
      public void cancelTask(Task task) {

      }

      @Override
      public void reloadLocale() {

      }

      @Override
      public void reloadConfigs() {

      }
    };
    disposer = Disposer.disposer();

    PathFinderProvider.setPathFinder(pathFinder);
    pathFinder.load();
    setupStorage(cached, factory);
    setupWorldMock();
    setupMiniMessage();
  }

  public void shutdownPathFinder() {
    shutdownStorage();
    pathFinder.shutdown();
    disposer.dispose(pathFinder);
    disposer = null;
    pathFinder = null;
    PathFinderProvider.setPathFinder(null);
  }

  public void setupWorldMock() {
    UUID uuid = UUID.randomUUID();
    world = WORLD_LOADER.loadWorld(uuid);
  }

  public void setupMiniMessage() {
    miniMessage = MiniMessage.miniMessage();
  }

  @SneakyThrows
  public void setupStorage(boolean cached, Supplier<StorageImplementation> factory) {
    nodeTypeRegistry = new NodeTypeRegistryImpl(pathFinder);
    modifierRegistry = new ModifierRegistryImpl(pathFinder);
    visualizerTypeRegistry = new VisualizerTypeRegistryImpl(pathFinder);

    storage = new StorageAdapterImpl(nodeTypeRegistry);
    StorageImplementation implementation = factory.get();
    // implementation = new DebugStorage(implementation, logger);
    implementation.setLogger(logger);
    implementation.setWorldLoader(WORLD_LOADER);

    storage.setImplementation(implementation);
    storage.setLogger(logger);
    storage.setCache(cached ? new CacheLayerImpl() : CacheLayerImpl.empty());

    waypointNodeType = nodeTypeRegistry.getType(AbstractPathFinder.pathfinder("waypoint"));

    visualizerType = new TestVisualizerType();
    InternalVisualizerStorageImplementation visualizerDataStorage = (InternalVisualizerStorageImplementation) implementation;
    visualizerType.setStorage(new VisualizerStorageImplementationWrapper<>(visualizerType, visualizerDataStorage));

    storage.init();
    globalGroup = storage.createGlobalNodeGroup(visualizerType).join();

    StorageUtil.storage = storage;
  }

  public StorageImplementation inMemoryStorage() {
    SqlStorage implementation = new SqlStorage(SQLDialect.H2, nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry) {
      @Override
      public DataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        return dataSource;
      }

      @Override
      @SneakyThrows
      public void shutdown() {
        getDataSource().getConnection().prepareStatement("DROP ALL OBJECTS").execute();
      }
    };
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }

  public void setupInMemoryStorage() {
    setupStorage(true, this::inMemoryStorage);
  }

  public void shutdownStorage() {
    if (storage != null) {
      storage.shutdown();
      storage = null;
    }
    groupMap = null;
    globalGroup = null;
    StorageUtil.storage = null;
  }

  protected <T> T assertResult(Supplier<CompletableFuture<T>> supplier) {
    CompletableFuture<T> future = supplier.get();
    T element = future.join();

    Assertions.assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(element);
    return element;
  }

  protected void assertFuture(Supplier<CompletableFuture<Void>> supplier) {
    CompletableFuture<Void> future = supplier.get();
    future.join();
    Assertions.assertFalse(future.isCompletedExceptionally());
  }

  protected Waypoint makeWaypoint() {
    return makeWaypoint(new Location(1, 2, 3, world));
  }

  protected Waypoint makeWaypoint(Location location) {
    return assertResult(() -> storage
        .createAndLoadNode(waypointNodeType, location)
        .thenCompose(storage::insertGlobalGroupAndSave));
  }

  protected GroupedNode makeGroupedWaypoint(Location location, PathVisualizer<?, ?>... visualizers) {
    Waypoint waypoint = makeWaypoint(location);
    Collection<NodeGroup> groups = new HashSet<>();
    for (PathVisualizer<?, ?> vis : visualizers) {
      NodeGroup group = groupMap.computeIfAbsent(vis, v -> {
        NodeGroup g = makeGroup(v.getKey());
        g.addModifier(new VisualizerModifierImpl(v.getKey()));
        g.add(waypoint.getNodeId());
        storage.saveGroup(g).join();
        return g;
      });
      groups.add(group);
    }
    groups.add(globalGroup);
    return new GroupedNodeImpl(waypoint, groups);
  }

  protected Collection<NodeGroup> getGroups(Node node) {
    return StorageUtil.getGroups(node);
  }

  protected Collection<NodeGroup> getGroups(UUID node) {
    return StorageUtil.getGroups(node);
  }

  protected void deleteWaypoint(Waypoint waypoint) {
    assertFuture(() -> storage.deleteNodes(new NodeSelectionImpl(waypoint).getIds()));
  }

  protected <N extends Node> N assertNodeExists(UUID node) {
    return (N) storage.loadNode(node).join().orElseThrow();
  }

  protected void assertNodeNotExists(UUID node) {
    Assertions.assertThrows(Exception.class,
        () -> storage.loadNode(node).join().orElseThrow());
  }

  protected void assertNodeCount(int count) {
    Collection<Node> nodesAfter = storage.loadNodes().join();
    Assertions.assertEquals(count, nodesAfter.size());
  }

  protected Edge makeEdge(Waypoint start, Waypoint end) {
    Edge edge = new EdgeImpl(start.getNodeId(), end.getNodeId(), 1.23f);
    assertFuture(() -> storage.modifyNode(start.getNodeId(), node -> {
      node.getEdges().add(edge);
    }));
    assertEdge(start.getNodeId(), end.getNodeId());
    return edge;
  }

  protected void assertEdge(UUID start, UUID end) {
    Node s = storage.loadNode(start).join().orElseThrow();
    Assertions.assertTrue(s.hasConnection(end));
  }

  protected void assertNoEdge(UUID start, UUID end) {
    Optional<Node> node = storage.loadNode(start).join();
    if (node.isEmpty()) {
      return;
    }
    Assertions.assertFalse(node.get().hasConnection(end));
  }

  protected NodeGroup makeGroup(NamespacedKey key) {
    return assertResult(() -> storage.createAndLoadGroup(key));
  }

  protected void deleteGroup(NamespacedKey key) {
    assertFuture(() -> storage.loadGroup(key)
        .thenCompose(group -> storage.deleteGroup(group.orElseThrow())));
  }

  protected NodeGroup assertGroupExists(NamespacedKey key) {
    return assertOptResult(() -> storage.loadGroup(key));
  }

  protected void assertGroupNotExists(NamespacedKey key) {
    Assertions.assertThrows(Exception.class,
        () -> storage.loadGroup(key).join().orElseThrow());
  }

  protected TestVisualizer makeVisualizer(NamespacedKey key) {
    return assertResult(() -> storage.createAndLoadVisualizer(visualizerType, key));
  }

  protected <T> T assertOptResult(Supplier<CompletableFuture<Optional<T>>> supplier) {
    return assertResult(supplier).orElseThrow();
  }
}
