//package de.cubbossa.pathfinder.storage;
//
//import be.seeseemelk.mockbukkit.MockBukkit;
//import be.seeseemelk.mockbukkit.ServerMock;
//import de.cubbossa.pathfinder.core.node.*;
//import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
//import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
//import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
//import de.cubbossa.pathfinder.util.NodeSelection;
//import java.util.Optional;
//import net.kyori.adventure.text.minimessage.MiniMessage;
//import org.bukkit.Location;
//import de.cubbossa.pathfinder.api.misc.NamespacedKey;
//import org.bukkit.World;
//import org.junit.jupiter.api.*;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public abstract class StorageTest {
//
//
//  abstract Storage storage();
//
//  protected static MiniMessage miniMessage;
//  protected static World world;
//  protected Storage storage;
//  protected NodeTypeRegistry nodeTypeRegistry;
//  protected NodeType<Waypoint> waypointNodeType;
//
//  @BeforeAll
//  static void beforeAll() {
//    miniMessage = MiniMessage.miniMessage();
//    ServerMock mock = MockBukkit.mock();
//    world = mock.addSimpleWorld("test");
//  }
//
//  @AfterEach
//  void afterEach() {
//    storage.shutdown();
//  }
//
//  @BeforeEach
//  void beforeEach() throws Exception {
//    nodeTypeRegistry = new NodeTypeRegistry();
//    storage = storage();
//    waypointNodeType = new WaypointType(new WaypointStorage(storage), miniMessage);
//    nodeTypeRegistry.registerNodeType(waypointNodeType);
//    nodeTypeRegistry.setWaypointNodeType(waypointNodeType);
//    storage.init();
//  }
//
//  private Waypoint waypoint() throws ExecutionException, InterruptedException, TimeoutException {
//    CompletableFuture<Waypoint> future = storage.createAndLoadNode(waypointNodeType, new Location(world, 1, 2, 3));
//    Waypoint waypoint = future.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future.isCompletedExceptionally());
//    Assertions.assertNotNull(waypoint);
//
//    return waypoint;
//  }
//
//  @Test @Order(1)
//  void createNode() throws ExecutionException, InterruptedException, TimeoutException {
//
//    CompletableFuture<Waypoint> future = storage.createAndLoadNode(waypointNodeType, new Location(world, 1, 2, 3));
//    Waypoint waypoint = future.get(1, TimeUnit.SECONDS);
//
//    assertFalse(future.isCompletedExceptionally());
//    Assertions.assertNotNull(waypoint);
//
//    Collection<Node<?>> nodesAfter = storage.loadNodes().join();
//    assertEquals(1, nodesAfter.size());
//  }
//
//  @Test @Order(2)
//  void getNodes() throws ExecutionException, InterruptedException, TimeoutException {
//    CompletableFuture<Collection<Node<?>>> future = storage.loadNodes();
//    Collection<Node<?>> nodes = future.join();
//
//    assertFalse(future.isCompletedExceptionally());
//    assertEquals(0, nodes.size());
//
//    waypoint();
//
//    Collection<Node<?>> nodesAfter = storage.loadNodes().get(1, TimeUnit.SECONDS);
//    assertEquals(1, nodesAfter.size());
//  }
//
//  @Test @Order(3)
//  <N extends Node<N>> void getNodeType() throws ExecutionException, InterruptedException, TimeoutException {
//
//    CompletableFuture<Optional<NodeType<N>>> future1 = storage.loadNodeType(waypoint().getNodeId());
//    NodeType<N> type = future1.get(1, TimeUnit.SECONDS).orElseThrow();
//
//    assertFalse(future1.isCompletedExceptionally());
//    Assertions.assertNotNull(type);
//    assertEquals(waypointNodeType, type);
//  }
//
//  @Test @Order(4)
//  void updateNode() throws ExecutionException, InterruptedException, TimeoutException {
//    Waypoint waypoint = waypoint();
//    storage.modifyNode(waypoint.getNodeId(), node -> {
//      node.setLocation(waypoint.getLocation().clone().add(0, 2, 0));
//    }).get(1, TimeUnit.SECONDS);
//
//    Node<?> after = storage.loadNode(waypoint.getNodeId()).get(1, TimeUnit.SECONDS).orElseThrow();
//
//    assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
//  }
//
//	Edge connectNodes(Waypoint start, Waypoint end) throws ExecutionException, InterruptedException, TimeoutException {
//		CompletableFuture<Edge> future = storage.connectNodes(start.getNodeId(), end.getNodeId(), 1.23);
//		Edge edge = future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//		Assertions.assertNotNull(edge);
//		assertEquals(start.getNodeId(), edge.getStart());
//		assertEquals(end.getNodeId(), edge.getEnd());
//		assertEquals(1.23, edge.getWeightModifier(), .00001);
//		return edge;
//	}
//
//	Edge connectNodes() throws ExecutionException, InterruptedException, TimeoutException {
//		Waypoint start = waypoint();
//		Waypoint end = waypoint();
//
//		return connectNodes(start, end);
//	}
//
//	@Test @Order(6)
//	void testConnectNodes() throws ExecutionException, InterruptedException, TimeoutException {
//		connectNodes();
//	}
//
//	@Test @Order(7)
//	void disconnectNodes() throws ExecutionException, InterruptedException, TimeoutException {
//		Edge edge = connectNodes();
//
//		CompletableFuture<?> future = storage.disconnectNodes(edge.getStart(), edge.getEnd());
//		future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//
//		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(edge.getStart());
//		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future1.isCompletedExceptionally());
//		assertEquals(0, edges.size());
//	}
//
//	@Test @Order(8)
//	void disconnectNodes3() throws ExecutionException, InterruptedException, TimeoutException {
//		Edge edge = connectNodes();
//
//		CompletableFuture<?> future = storage.disconnectNodes(new NodeSelection(edge.getStart()), new NodeSelection(edge.getEnd()));
//		future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//
//		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(edge.getStart());
//		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future1.isCompletedExceptionally());
//		assertEquals(0, edges.size());
//	}
//
//	@Test @Order(9)
//	void disconnectNodes1() throws ExecutionException, InterruptedException, TimeoutException {
//		Waypoint a = waypoint();
//		Waypoint b = waypoint();
//		Waypoint c = waypoint();
//
//		Edge ab = connectNodes(a, b);
//		Edge ac = connectNodes(a, c);
//		Edge bc = connectNodes(b, c);
//
//		CompletableFuture<?> future = storage.disconnectNodes(a.getNodeId());
//		future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//
//		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(a.getNodeId());
//		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future1.isCompletedExceptionally());
//		assertEquals(0, edges.size());
//
//		CompletableFuture<Collection<Edge>> future2 = storage.getConnectionsTo(c.getNodeId());
//		Collection<Edge> edges1 = future2.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future2.isCompletedExceptionally());
//		assertEquals(List.of(bc), edges1);
//	}
//
//	@Test @Order(10)
//	void disconnectNodes2() throws ExecutionException, InterruptedException, TimeoutException {
//		Waypoint a = waypoint();
//		Waypoint b = waypoint();
//		Waypoint c = waypoint();
//
//		Edge ab = connectNodes(a, b);
//		Edge ac = connectNodes(a, c);
//		Edge bc = connectNodes(b, c);
//
//		CompletableFuture<?> future = storage.disconnectNodes(new NodeSelection(a.getNodeId()));
//		future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//
//		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(a.getNodeId());
//		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future1.isCompletedExceptionally());
//		assertEquals(0, edges.size());
//
//		CompletableFuture<Collection<Edge>> future2 = storage.getConnectionsTo(c.getNodeId());
//		Collection<Edge> edges1 = future2.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future2.isCompletedExceptionally());
//		assertEquals(List.of(bc), edges1);
//	}
//
//	NodeGroup nodeGroup(NamespacedKey key) throws ExecutionException, InterruptedException, TimeoutException {
//		CompletableFuture<NodeGroup> future = storage.createAndLoadGroup(key);
//		NodeGroup group = future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//		Assertions.assertNotNull(group);
//
//		return group;
//	}
//
//	@Test @Order(11)
//	void createGroup() throws ExecutionException, InterruptedException, TimeoutException {
//		nodeGroup(NamespacedKey.fromString("pathfinder:abc"));
//	}
//
//	@Test @Order(12)
//	void deleteGroup() throws ExecutionException, InterruptedException, TimeoutException {
//		NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
//		NodeGroup g = nodeGroup(key);
//
//		CompletableFuture<Void> future = storage.deleteGroup(g);
//		future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//
//		CompletableFuture<NodeGroup> future1 = storage.loadGroup(key).thenApply(Optional::orElseThrow);
//		NodeGroup group = future1.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future1.isCompletedExceptionally());
//		Assertions.assertNull(group);
//	}
//
//	@Test @Order(13)
//	void getNodeGroupKeySet() throws ExecutionException, InterruptedException, TimeoutException {
//		NamespacedKey a = NamespacedKey.fromString("pathfinder:a");
//		NamespacedKey b = NamespacedKey.fromString("pathfinder:b");
//
//		nodeGroup(a);
//		nodeGroup(b);
//
//		CompletableFuture<Collection<NamespacedKey>> future = storage.loadAllGroups().thenApply(nodeGroups -> nodeGroups.stream()
//				.map(NodeGroup::getKey).toList());
//		Collection<NamespacedKey> keys = future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//		assertEquals(List.of(a, b), keys);
//	}
//
//	@Test @Order(14)
//	void deleteNodes() throws ExecutionException, InterruptedException, TimeoutException {
//		Waypoint a = waypoint();
//		waypoint();
//		waypoint();
//
//		Collection<Node<?>> nodes = storage.loadNodes().get(1, TimeUnit.SECONDS);
//		assertEquals(3, nodes.size());
//
//		CompletableFuture<Void> future = storage.deleteNodes(List.of(a));
//		future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//
//		Collection<Node<?>> nodes1 = storage.loadNodes().get(1, TimeUnit.SECONDS);
//		assertEquals(2, nodes1.size());
//
//		CompletableFuture<Void> future1 = storage.deleteNodesById(List.of(UUID.randomUUID()));
//		future1.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future1.isCompletedExceptionally());
//
//		Collection<Node<?>> nodes2 = storage.loadNodes().get(1, TimeUnit.SECONDS);
//		assertEquals(2, nodes2.size());
//	}
//
//	@Test @Order(15)
//	void assignNodesToGroup() throws ExecutionException, InterruptedException, TimeoutException {
//		NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
//		Waypoint a = waypoint();
//		NodeGroup g = nodeGroup(gk);
//
//		CompletableFuture<Void> future = storage.assignNodesToGroup(gk, new NodeSelection(a.getNodeId()));
//		future.get(1, TimeUnit.SECONDS);
//		assertFalse(future.isCompletedExceptionally());
//
//		CompletableFuture<Collection<UUID>> future1 = storage.getNodeGroupNodes(gk);
//		Collection<UUID> uuids = future1.get(1, TimeUnit.SECONDS);
//		assertFalse(future1.isCompletedExceptionally());
//		assertTrue(uuids.contains(a.getNodeId()));
//	}
//
//	@Test @Order(16)
//	void unassignNodesToGroup() throws ExecutionException, InterruptedException, TimeoutException {
//		NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
//		Waypoint a = waypoint();
//		Waypoint b = waypoint();
//		NodeGroup g = nodeGroup(gk);
//
//		storage.assignNodesToGroup(gk, new NodeSelection(a.getNodeId(), b.getNodeId())).get(1, TimeUnit.SECONDS);
//		CompletableFuture<Void> future = storage.removeNodesFromGroup(gk, new NodeSelection(a.getNodeId()));
//		future.get(1, TimeUnit.SECONDS);
//		assertFalse(future.isCompletedExceptionally());
//
//		CompletableFuture<Collection<UUID>> future1 = storage.getNodeGroupNodes(gk);
//		Collection<UUID> uuids = future1.get(1, TimeUnit.SECONDS);
//		assertFalse(future1.isCompletedExceptionally());
//		assertFalse(uuids.contains(a.getNodeId()));
//		assertTrue(uuids.contains(b.getNodeId()));
//	}
//
//	@Test @Order(16)
//	void deleteNodesWithGroups() throws ExecutionException, InterruptedException, TimeoutException {
//		NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
//		Waypoint a = waypoint();
//		NodeGroup g = nodeGroup(gk);
//
//		storage.assignNodesToGroup(gk, new NodeSelection(a.getNodeId())).get(1, TimeUnit.SECONDS);
//		storage.deleteNodes(new NodeSelection(a.getNodeId())).get(1, TimeUnit.SECONDS);
//
//		CompletableFuture<Collection<UUID>> future = storage.getNodeGroupNodes(gk);
//		Collection<UUID> nodes = future.get(1, TimeUnit.SECONDS);
//
//		assertFalse(future.isCompletedExceptionally());
//		assertNotNull(nodes);
//		assertEquals(0, nodes.size());
//	}
//
//	@Test
//	void testGetNodes() {
//	}
//
//	@Test
//	void testGetNodes1() {
//	}
//
//	@Test
//	void getNode() {
//	}
//
//	@Test
//	void getNodeGroups() {
//	}
//
//	@Test
//	void removeNodesFromGroups() {
//	}
//
//	@Test
//	void assignNodeGroupModifier() {
//	}
//
//	@Test
//	void unassignNodeGroupModifier() {
//	}
//}
