package de.cubbossa.pathfinder.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.WorldImpl;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public abstract class StorageTest {

  abstract StorageImplementation storage(NodeTypeRegistry registry);

  protected static MiniMessage miniMessage;
  protected static World world;
  protected static Logger logger = Logger.getLogger("TESTS");
  protected Storage storage;
  protected NodeTypeRegistry nodeTypeRegistry;
  protected NodeType<Waypoint> waypointNodeType;

  @BeforeAll
  static void beforeAll() {
    miniMessage = MiniMessage.miniMessage();
    ServerMock mock = MockBukkit.mock();
    org.bukkit.World bukkitWorld = mock.addSimpleWorld("test");
    world = new WorldImpl(bukkitWorld.getUID());
  }

  @AfterEach
  void afterEach() {
    storage.shutdown();
  }

  @BeforeEach
  void beforeEach() throws Exception {
    nodeTypeRegistry = new NodeTypeRegistry();
    storage = new Storage();
    storage.setLogger(logger);
    storage.setImplementation(storage(nodeTypeRegistry));
    waypointNodeType = new WaypointType(new WaypointStorage(storage), miniMessage);
    nodeTypeRegistry.register(waypointNodeType);
    nodeTypeRegistry.setWaypointNodeType(waypointNodeType);
    storage.init();
  }

  private Waypoint waypoint() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Waypoint> future =
        storage.createAndLoadNode(waypointNodeType, new Location(1, 2, 3, world));
    Waypoint waypoint = future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(waypoint);

    return waypoint;
  }

  @Test
  @Order(1)
  void createNode() throws ExecutionException, InterruptedException, TimeoutException {

    CompletableFuture<Waypoint> future =
        storage.createAndLoadNode(waypointNodeType, new Location(1, 2, 3, world));
    Waypoint waypoint = future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(waypoint);

    Collection<Node<?>> nodesAfter = storage.loadNodes().join();
    assertEquals(1, nodesAfter.size());
  }

  @Test
  @Order(2)
  void getNodes() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Collection<Node<?>>> future = storage.loadNodes();
    Collection<Node<?>> nodes = future.join();

    assertFalse(future.isCompletedExceptionally());
    assertEquals(0, nodes.size());

    waypoint();

    Collection<Node<?>> nodesAfter = storage.loadNodes().get(1, TimeUnit.SECONDS);
    assertEquals(1, nodesAfter.size());
  }

  @Test
  @Order(3)
  <N extends Node<N>> void getNodeType()
      throws ExecutionException, InterruptedException, TimeoutException {

    CompletableFuture<Optional<NodeType<N>>> future1 = storage.loadNodeType(waypoint().getNodeId());
    NodeType<N> type = future1.get(1, TimeUnit.SECONDS).orElseThrow();

    assertFalse(future1.isCompletedExceptionally());
    Assertions.assertNotNull(type);
    assertEquals(waypointNodeType, type);
  }

  @Test
  @Order(4)
  void updateNode() throws ExecutionException, InterruptedException, TimeoutException {
    Waypoint waypoint = waypoint();
    storage.modifyNode(waypoint.getNodeId(), node -> {
      node.setLocation(waypoint.getLocation().clone().add(0, 2, 0));
    }).get(1, TimeUnit.SECONDS);

    Node<?> after = storage.loadNode(waypoint.getNodeId()).get(1, TimeUnit.SECONDS).orElseThrow();

    assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
  }

  void connectNodes(Waypoint start, Waypoint end)
      throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Void> future = storage.modifyNode(start.getNodeId(), node -> {
      node.getEdges().add(new SimpleEdge(start.getNodeId(), end.getNodeId(), 1.23f));
    });
    future.get(1, TimeUnit.SECONDS);
    assertFalse(future.isCompletedExceptionally());

    assertTrue(storage.loadNode(start.getNodeId()).get(1, TimeUnit.SECONDS).orElseThrow().getEdges()
        .stream().anyMatch(edge -> edge.getEnd().equals(end.getNodeId())));
  }

  @Test
  @Order(6)
  void testConnectNodes() throws ExecutionException, InterruptedException, TimeoutException {
    Waypoint a = waypoint();
    Waypoint b = waypoint();
    connectNodes(a, b);
  }

//  @Test
//  @Order(7)
//  void disconnectNodes() throws ExecutionException, InterruptedException, TimeoutException {
//    Waypoint a = waypoint();
//    Waypoint b = waypoint();
//    connectNodes(a, b);
//
//    CompletableFuture<?> future = storage.disconnectNodes(edge.getStart(), edge.getEnd());
//    future.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future.isCompletedExceptionally());
//
//    CompletableFuture<Collection<Edge>> future1 = storage.getConnections(edge.getStart());
//    Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future1.isCompletedExceptionally());
//    assertEquals(0, edges.size());
//  }
//
//  @Test
//  @Order(8)
//  void disconnectNodes3() throws ExecutionException, InterruptedException, TimeoutException {
//    Edge edge = connectNodes();
//
//    CompletableFuture<?> future = storage.disconnectNodes(new NodeSelection(edge.getStart()),
//        new NodeSelection(edge.getEnd()));
//    future.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future.isCompletedExceptionally());
//
//    CompletableFuture<Collection<Edge>> future1 = storage.getConnections(edge.getStart());
//    Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future1.isCompletedExceptionally());
//    assertEquals(0, edges.size());
//  }
//
//  @Test
//  @Order(9)
//  void disconnectNodes1() throws ExecutionException, InterruptedException, TimeoutException {
//    Waypoint a = waypoint();
//    Waypoint b = waypoint();
//    Waypoint c = waypoint();
//
//    Edge ab = connectNodes(a, b);
//    Edge ac = connectNodes(a, c);
//    Edge bc = connectNodes(b, c);
//
//    CompletableFuture<?> future = storage.disconnectNodes(a.getNodeId());
//    future.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future.isCompletedExceptionally());
//
//    CompletableFuture<Collection<Edge>> future1 = storage.getConnections(a.getNodeId());
//    Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future1.isCompletedExceptionally());
//    assertEquals(0, edges.size());
//
//    CompletableFuture<Collection<Edge>> future2 = storage.getConnectionsTo(c.getNodeId());
//    Collection<Edge> edges1 = future2.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future2.isCompletedExceptionally());
//    assertEquals(List.of(bc), edges1);
//  }
//
//  @Test
//  @Order(10)
//  void disconnectNodes2() throws ExecutionException, InterruptedException, TimeoutException {
//    Waypoint a = waypoint();
//    Waypoint b = waypoint();
//    Waypoint c = waypoint();
//
//    Edge ab = connectNodes(a, b);
//    Edge ac = connectNodes(a, c);
//    Edge bc = connectNodes(b, c);
//
//    CompletableFuture<?> future = storage.disconnectNodes(new NodeSelection(a.getNodeId()));
//    future.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future.isCompletedExceptionally());
//
//    CompletableFuture<Collection<Edge>> future1 = storage.getConnections(a.getNodeId());
//    Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future1.isCompletedExceptionally());
//    assertEquals(0, edges.size());
//
//    CompletableFuture<Collection<Edge>> future2 = storage.getConnectionsTo(c.getNodeId());
//    Collection<Edge> edges1 = future2.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future2.isCompletedExceptionally());
//    assertEquals(List.of(bc), edges1);
//  }

  NodeGroup nodeGroup(NamespacedKey key)
      throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<NodeGroup> future = storage.createAndLoadGroup(key);
    NodeGroup group = future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(group);

    return group;
  }

  @Test
  @Order(11)
  void createGroup() throws ExecutionException, InterruptedException, TimeoutException {
    nodeGroup(NamespacedKey.fromString("pathfinder:abc"));
  }

  @Test
  @Order(12)
  void deleteGroup() throws ExecutionException, InterruptedException, TimeoutException {
    NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
    NodeGroup g = nodeGroup(key);

    CompletableFuture<Void> future = storage.deleteGroup(g);
    future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());

    CompletableFuture<Optional<NodeGroup>> future1 = storage.loadGroup(key);
    Optional<NodeGroup> group = future1.get(1, TimeUnit.SECONDS);

    assertFalse(future1.isCompletedExceptionally());
    Assertions.assertFalse(group.isPresent());
  }

  @Test
  @Order(13)
  void getNodeGroupKeySet() throws ExecutionException, InterruptedException, TimeoutException {
    NamespacedKey a = NamespacedKey.fromString("pathfinder:a");
    NamespacedKey b = NamespacedKey.fromString("pathfinder:b");

    nodeGroup(a);
    nodeGroup(b);

    CompletableFuture<Collection<NamespacedKey>> future =
        storage.loadAllGroups().thenApply(nodeGroups -> nodeGroups.stream()
            .map(NodeGroup::getKey).toList());
    Collection<NamespacedKey> keys = future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());
    assertEquals(List.of(a, b), keys);
  }

  @Test
  @Order(14)
  void deleteNodes() throws ExecutionException, InterruptedException, TimeoutException {
    Waypoint a = waypoint();
    waypoint();
    waypoint();

    Collection<Node<?>> nodes = storage.loadNodes().get(1, TimeUnit.SECONDS);
    assertEquals(3, nodes.size());

    CompletableFuture<Void> future = storage.deleteNodes(List.of(a));
    future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());

    Collection<Node<?>> nodes1 = storage.loadNodes().get(1, TimeUnit.SECONDS);
    assertEquals(2, nodes1.size());

    CompletableFuture<Void> future1 = storage.deleteNodesById(List.of(UUID.randomUUID()));
    future1.get(1, TimeUnit.SECONDS);

    assertFalse(future1.isCompletedExceptionally());

    Collection<Node<?>> nodes2 = storage.loadNodes().get(1, TimeUnit.SECONDS);
    assertEquals(2, nodes2.size());
  }

  @Test
  @Order(15)
  void assignNodesToGroup() throws ExecutionException, InterruptedException, TimeoutException {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = waypoint();
    NodeGroup g = nodeGroup(gk);

    CompletableFuture<Void> future = storage.modifyNode(a.getNodeId(), n -> {
      if (n instanceof Groupable<?> groupable) {
        groupable.addGroup(g);
      }
    });
    future.get(1, TimeUnit.SECONDS);
    assertFalse(future.isCompletedExceptionally());

    CompletableFuture<Optional<Waypoint>> future1 = storage.loadNode(a.getNodeId());
    Waypoint waypoint = future1.get(1, TimeUnit.SECONDS).orElseThrow();
    assertFalse(future1.isCompletedExceptionally());
    assertTrue(waypoint.getGroups().contains(g));
  }

  @Test
  @Order(16)
  void unassignNodesToGroup() throws ExecutionException, InterruptedException, TimeoutException {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = waypoint();
    Waypoint b = waypoint();
    NodeGroup g = nodeGroup(gk);

    storage.modifyNode(a.getNodeId(), node -> {
      if (node instanceof Groupable<?> groupable) {
        groupable.addGroup(g);
      }
    }).get(1, TimeUnit.SECONDS);
    assertTrue(storage.loadNode(a.getNodeId()).get(1, TimeUnit.SECONDS)
        .orElseThrow() instanceof Groupable<?> groupable && groupable.getGroups().contains(g));
    assertTrue(
        storage.loadGroup(gk).get(1, TimeUnit.SECONDS).orElseThrow().contains(a.getNodeId()));
    assertFalse(
        storage.loadGroup(gk).get(1, TimeUnit.SECONDS).orElseThrow().contains(b.getNodeId()));

    CompletableFuture<Void> future = storage.modifyNode(a.getNodeId(), node -> {
      if (node instanceof Groupable<?> groupable) {
        groupable.removeGroup(gk);
      }
    });
    future.get(1, TimeUnit.SECONDS);
    assertFalse(future.isCompletedExceptionally());

    CompletableFuture<Optional<NodeGroup>> future1 = storage.loadGroup(gk);
    Collection<UUID> uuids = future1.get(1, TimeUnit.SECONDS).orElseThrow();
    assertFalse(future1.isCompletedExceptionally());
    assertFalse(uuids.contains(a.getNodeId()));
    assertFalse(uuids.contains(b.getNodeId()));
  }

  @Test
  @Order(16)
  void deleteNodesWithGroups() throws ExecutionException, InterruptedException, TimeoutException {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = waypoint();
    NodeGroup g = nodeGroup(gk);

    storage.modifyNode(a.getNodeId(), node -> {
      if (node instanceof Groupable<?> groupable) {
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
  void testGetNodes() {
  }

  @Test
  void testGetNodes1() {
  }

  @Test
  void getNode() {
  }

  @Test
  void getNodeGroups() {
  }

  @Test
  void removeNodesFromGroups() {
  }

  @Test
  void assignNodeGroupModifier() {
  }

  @Test
  void unassignNodeGroupModifier() {
  }
}
