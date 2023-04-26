package de.cubbossa.pathfinder.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifierType;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.WorldImpl;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public abstract class StorageTest {

  protected boolean useCaches = false;

  protected static MiniMessage miniMessage;
  protected static World world;
  protected static Logger logger = Logger.getLogger("TESTS");
  protected StorageImpl storage;
  protected NodeTypeRegistryImpl nodeTypeRegistry;
  protected ModifierRegistry modifierRegistry;
  protected NodeType<Waypoint> waypointNodeType;

  @BeforeAll
  static void beforeAll() {
    miniMessage = MiniMessage.miniMessage();
    ServerMock mock = MockBukkit.mock();
    org.bukkit.World bukkitWorld = mock.addSimpleWorld("test");
    world = new WorldImpl(bukkitWorld.getUID());
  }

  @AfterAll
  static void afterAll() {
    MockBukkit.unmock();
  }

  abstract StorageImplementation storage(NodeTypeRegistryImpl registry,
                                         ModifierRegistry modifierRegistry);

  @AfterEach
  void afterEach() {
    storage.shutdown();
  }

  @BeforeEach
  void beforeEach() throws Exception {
    nodeTypeRegistry = new NodeTypeRegistryImpl();
    modifierRegistry = new ModifierRegistryImpl();
    modifierRegistry.registerModifierType(new PermissionModifierType());

    storage = new StorageImpl();
    StorageImplementation implementation = storage(nodeTypeRegistry, modifierRegistry);

    storage.setImplementation(implementation);
    storage.setLogger(logger);
    storage.setCache(useCaches ? new CacheLayerImpl() : CacheLayerImpl.empty());

    waypointNodeType = new WaypointType(new WaypointStorage(storage), miniMessage);
    nodeTypeRegistry.register(waypointNodeType);
    nodeTypeRegistry.setWaypointNodeType(waypointNodeType);

    storage.init();
  }

  protected <T> T assertResult(Supplier<CompletableFuture<T>> supplier) throws Exception {
    CompletableFuture<T> future = supplier.get();
    T element = future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());
    assertNotNull(element);
    return element;
  }

  protected void assertFuture(Supplier<CompletableFuture<Void>> supplier) throws Exception {
    CompletableFuture<Void> future = supplier.get();
    future.get(1, TimeUnit.SECONDS);
    assertFalse(future.isCompletedExceptionally());
  }

  protected <T> T assertOptResult(Supplier<CompletableFuture<Optional<T>>> supplier)
      throws Exception {
    return assertResult(supplier).orElseThrow();
  }

  protected Waypoint makeWaypoint() throws Exception {
    return assertResult(
        () -> storage.createAndLoadNode(waypointNodeType, new Location(1, 2, 3, world)));
  }

  protected <N extends Node> N assertNodeExists(UUID node) throws Exception {
    return (N) storage.loadNode(node).get(1, TimeUnit.SECONDS).orElseThrow();
  }

  protected void assertNodeNotExists(UUID node) {
    assertThrows(Exception.class,
        () -> storage.loadNode(node).get(1, TimeUnit.SECONDS).orElseThrow());
  }

  protected void assertNodeCount(int count) throws Exception {
    Collection<Node> nodesAfter = storage.loadNodes().get(1, TimeUnit.SECONDS);
    assertEquals(count, nodesAfter.size());
  }

  protected Edge makeEdge(Waypoint start, Waypoint end)
      throws Exception {
    Edge edge = new SimpleEdge(start.getNodeId(), end.getNodeId(), 1.23f);
    assertFuture(() -> storage.modifyNode(start.getNodeId(), node -> {
      node.getEdges().add(edge);
    }));
    assertEdge(start.getNodeId(), end.getNodeId());
    return edge;
  }

  protected void assertEdge(UUID start, UUID end) throws Exception {
    assertTrue(storage.loadNode(start).get(1, TimeUnit.SECONDS).orElseThrow().getEdges()
        .stream().anyMatch(e -> e.getEnd().equals(end)));
  }

  protected void assertNoEdge(UUID start, UUID end) throws Exception {
    Optional<Node> node = (Optional<Node>) storage.loadNode(start).get(1, TimeUnit.SECONDS);
    if (node.isEmpty()) {
      return;
    }
    assertFalse(node.get().getEdges().stream().anyMatch(e -> e.getEnd().equals(end)));
  }

  protected NodeGroup makeGroup(NamespacedKey key) throws Exception {
    return assertResult(() -> storage.createAndLoadGroup(key));
  }

  protected void deleteGroup(NamespacedKey key) throws Exception {
    assertFuture(() -> storage.loadGroup(key)
        .thenAccept(group -> storage.deleteGroup(group.orElseThrow()).join()));
  }

  protected NodeGroup assertGroupExists(NamespacedKey key) throws Exception {
    return assertOptResult(() -> storage.loadGroup(key));
  }

  protected void assertGroupNotExists(NamespacedKey key) {
    assertThrows(Exception.class,
        () -> storage.loadGroup(key).get(1, TimeUnit.SECONDS).orElseThrow());
  }

  @Test
  @Order(1)
  void createNode() throws Exception {
    assertNodeCount(0);
    Waypoint waypoint = makeWaypoint();
    assertNodeExists(waypoint.getNodeId());
    assertNodeCount(1);
  }

  @Test
  @Order(2)
  void getNodes() throws Exception {
    assertNodeCount(0);
    makeWaypoint();
    assertNodeCount(1);
  }

  @Test
  @Order(3)
  <N extends Node> void getNodeType()
      throws Exception {
    Waypoint waypoint = makeWaypoint();
    NodeType<N> type = assertOptResult(() -> storage.loadNodeType(waypoint.getNodeId()));
    assertEquals(waypointNodeType, type);
  }

  @Test
  @Order(4)
  void updateNode() throws Exception {
    Waypoint waypoint = makeWaypoint();
    assertFuture(() -> storage.modifyNode(waypoint.getNodeId(), node -> {
      node.setLocation(waypoint.getLocation().clone().add(0, 2, 0));
    }));
    Node after = assertNodeExists(waypoint.getNodeId());
    assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
  }

  @Test
  @Order(6)
  void testConnectNodes() throws Exception {
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();
    assertNoEdge(a.getNodeId(), b.getNodeId());
    makeEdge(a, b);
    assertEdge(a.getNodeId(), b.getNodeId());
  }

  @SneakyThrows
  @Test
  @Order(7)
  void disconnectNodes() throws Exception {
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();
    assertNoEdge(a.getNodeId(), b.getNodeId());
    Edge edge = makeEdge(a, b);
    assertEdge(a.getNodeId(), b.getNodeId());

    assertFuture(() -> storage.modifyNode(edge.getStart(), node -> {
      node.getEdges().removeIf(e -> e.getEnd().equals(edge.getEnd()));
    }));
    assertNoEdge(a.getNodeId(), b.getNodeId());
  }

  @Test
  @Order(11)
  void createGroup() throws Exception {
    NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
    assertGroupNotExists(key);
    makeGroup(key);
    assertGroupExists(key);
  }

  @Test
  @Order(12)
  void deleteGroup() throws Exception {
    NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
    assertGroupNotExists(key);
    makeGroup(key);
    assertGroupExists(key);
    deleteGroup(key);
    assertGroupNotExists(key);
  }

  @Test
  @Order(13)
  void getNodeGroupKeySet() throws Exception {
    NamespacedKey a = NamespacedKey.fromString("pathfinder:a");
    NamespacedKey b = NamespacedKey.fromString("pathfinder:b");

    makeGroup(a);
    makeGroup(b);

    Collection<NamespacedKey> keys = assertResult(() -> storage.loadAllGroups()
        .thenApply(nodeGroups -> nodeGroups.stream()
            .map(NodeGroup::getKey)
            .collect(Collectors.toList())));
    assertEquals(List.of(a, b), keys);
  }

  @Test
  @Order(14)
  void deleteNodes() throws Exception {
    assertNodeCount(0);
    Waypoint a = makeWaypoint();
    makeWaypoint();
    assertNodeCount(2);
    assertFuture(() -> storage.deleteNodes(List.of(a)));
    assertNodeCount(1);
    assertFuture(() -> storage.deleteNodesById(List.of(UUID.randomUUID())));
    assertNodeCount(1);
  }

  @Test
  @Order(15)
  void assignNodesToGroup() throws Exception {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = makeWaypoint();
    NodeGroup g = makeGroup(gk);

    assertFuture(() -> storage.modifyNode(a.getNodeId(), n -> {
      if (n instanceof Groupable groupable) {
        groupable.addGroup(g);
      }
    }));

    Waypoint waypoint = assertNodeExists(a.getNodeId());
    assertTrue(waypoint.getGroups().stream().map(NodeGroup::getKey).anyMatch(k -> k.equals(gk)));
  }

  @Test
  @Order(16)
  void unassignNodesFromGroup() throws Exception {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = makeWaypoint();
    NodeGroup g = makeGroup(gk);

    assertFuture(() -> storage.modifyNode(a.getNodeId(), node -> {
      if (node instanceof Groupable groupable) {
        groupable.addGroup(g);
      }
    }));

    Waypoint a1 = assertNodeExists(a.getNodeId());
    assertTrue(a1.getGroups().stream().map(NodeGroup::getKey).anyMatch(gk::equals));
    NodeGroup g1 = assertGroupExists(gk);
    assertTrue(g1.contains(a.getNodeId()));

    assertFuture(() -> storage.modifyNode(a.getNodeId(), node -> {
      if (node instanceof Groupable groupable) {
        groupable.removeGroup(gk);
      }
    }));

    NodeGroup g2 = assertGroupExists(gk);
    assertFalse(g2.contains(a.getNodeId()));
  }

  @Test
  @Order(16)
  void deleteNodesWithGroups() throws Exception {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = makeWaypoint();
    NodeGroup g = makeGroup(gk);

    storage.modifyNode(a.getNodeId(), node -> {
      if (node instanceof Groupable groupable) {
        groupable.addGroup(g);
      }
    }).get(1, TimeUnit.SECONDS);
    storage.deleteNodes(new NodeSelection(a)).get(1, TimeUnit.SECONDS);

    CompletableFuture<Optional<NodeGroup>> future = storage.loadGroup(gk);
    Collection<UUID> nodes = future.get(1, TimeUnit.SECONDS).orElseThrow();

    assertFalse(future.isCompletedExceptionally());
    assertNotNull(nodes);
    assertEquals(0, nodes.size());
  }

  @Test
  void deleteNodeWithEdges() {

  }

  @Test
  void deleteGroupWithModifier() {

  }

  @Test
  void setModifier() throws Exception {
    NamespacedKey gk = NamespacedKey.fromString("test:abc");
    NodeGroup g = makeGroup(gk);
    Modifier mod = new PermissionModifier("abc");

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.addModifier(mod);
    }));
    NodeGroup g1 = assertGroupExists(gk);
    assertTrue(g1.hasModifier(PermissionModifier.class));
    assertEquals("abc", g1.getModifier(PermissionModifier.class).permission());

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.removeModifier(PermissionModifier.class);
    }));
    NodeGroup g2 = assertGroupExists(gk);
    assertFalse(g2.hasModifier(PermissionModifier.class));
  }

  @Test
  void createVisualizer() {

  }

  @Test
  void deleteVisualizer() {

  }

  @Test
  void loadVisualizer() {

  }
}
