package de.cubbossa.pathfinder.data;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.util.NodeSelection;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class DataStorageTest {


  abstract DataStorage storage();

  protected static MiniMessage miniMessage;
  protected static World world;
  protected DataStorage storage;
  protected NodeTypeRegistry nodeTypeRegistry;
  protected NodeType<Waypoint> waypointNodeType;

  @BeforeAll
  static void beforeAll() {
    miniMessage = MiniMessage.miniMessage();
    ServerMock mock = MockBukkit.mock();
    world = mock.addSimpleWorld("test");
  }

  @AfterEach
  void afterEach() {
    storage.disconnect();
  }

  @BeforeEach
  void beforeEach() throws IOException {
    nodeTypeRegistry = new NodeTypeRegistry();
    storage = storage();
    waypointNodeType = new WaypointType(storage, miniMessage);
    nodeTypeRegistry.registerNodeType(waypointNodeType);
    nodeTypeRegistry.setWaypointNodeType(waypointNodeType);
    storage.connect();
  }

  private Waypoint waypoint() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Waypoint> future = storage.createNode(waypointNodeType, new Location(world, 1, 2, 3));
    Waypoint waypoint = future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(waypoint);

    return waypoint;
  }

  @Test @Order(1)
  void createNode() throws ExecutionException, InterruptedException, TimeoutException {

    CompletableFuture<Waypoint> future = storage.createNode(waypointNodeType, new Location(world, 1, 2, 3));
    Waypoint waypoint = future.get(1, TimeUnit.SECONDS);

    assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(waypoint);

    Collection<Node<?>> nodesAfter = storage.getNodes().join();
    assertEquals(1, nodesAfter.size());
  }

  @Test @Order(2)
  void getNodes() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Collection<Node<?>>> future = storage.getNodes();
    Collection<Node<?>> nodes = future.join();

    assertFalse(future.isCompletedExceptionally());
    assertEquals(0, nodes.size());

    waypoint();

    Collection<Node<?>> nodesAfter = storage.getNodes().get(1, TimeUnit.SECONDS);
    assertEquals(1, nodesAfter.size());
  }

  @Test @Order(3)
  void getNodeType() throws ExecutionException, InterruptedException, TimeoutException {

    CompletableFuture<NodeType<?>> future1 = storage.getNodeType(waypoint().getNodeId());
    NodeType<?> type = future1.get(1, TimeUnit.SECONDS);

    assertFalse(future1.isCompletedExceptionally());
    Assertions.assertNotNull(type);
    assertEquals(waypointNodeType, type);
  }

  @Test @Order(4)
  void updateNode() throws ExecutionException, InterruptedException, TimeoutException {
    Waypoint waypoint = waypoint();
		System.out.println(waypoint.getLocation().getWorld().getUID());
    storage.updateNode(waypoint.getNodeId(), node -> {
	    System.out.println(node.getLocation().getWorld().getUID());
      node.setLocation(waypoint.getLocation().clone().add(0, 2, 0));
    }).get(1, TimeUnit.SECONDS);

    Node<?> after = storage.getNode(waypoint.getNodeId()).get(1, TimeUnit.SECONDS);

    assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
  }

	@Test @Order(5)
	void teleportNode() throws ExecutionException, InterruptedException, TimeoutException {
		Waypoint waypoint = waypoint();
		storage.teleportNode(
				waypoint.getNodeId(),
				waypoint.getLocation().clone().add(0, 2, 0)
		).get(1, TimeUnit.SECONDS);

		Node<?> after = storage.getNode(waypoint.getNodeId()).get(1, TimeUnit.SECONDS);

		assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
	}

	Edge connectNodes(Waypoint start, Waypoint end) throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Edge> future = storage.connectNodes(start.getNodeId(), end.getNodeId(), 1.23);
		Edge edge = future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());
		Assertions.assertNotNull(edge);
		assertEquals(start.getNodeId(), edge.getStart());
		assertEquals(end.getNodeId(), edge.getEnd());
		assertEquals(1.23, edge.getWeightModifier(), .00001);
		return edge;
	}

	Edge connectNodes() throws ExecutionException, InterruptedException, TimeoutException {
		Waypoint start = waypoint();
		Waypoint end = waypoint();

		return connectNodes(start, end);
	}

	@Test @Order(6)
	void testConnectNodes() throws ExecutionException, InterruptedException, TimeoutException {
		connectNodes();
	}

	@Test @Order(7)
	void disconnectNodes() throws ExecutionException, InterruptedException, TimeoutException {
		Edge edge = connectNodes();

		CompletableFuture<?> future = storage.disconnectNodes(edge.getStart(), edge.getEnd());
		future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());

		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(edge.getStart());
		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);

		assertFalse(future1.isCompletedExceptionally());
		assertEquals(0, edges.size());
	}

	@Test @Order(8)
	void disconnectNodes3() throws ExecutionException, InterruptedException, TimeoutException {
		Edge edge = connectNodes();

		CompletableFuture<?> future = storage.disconnectNodes(new NodeSelection(edge.getStart()), new NodeSelection(edge.getEnd()));
		future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());

		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(edge.getStart());
		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);

		assertFalse(future1.isCompletedExceptionally());
		assertEquals(0, edges.size());
	}

	@Test @Order(9)
	void disconnectNodes1() throws ExecutionException, InterruptedException, TimeoutException {
		Waypoint a = waypoint();
		Waypoint b = waypoint();
		Waypoint c = waypoint();

		Edge ab = connectNodes(a, b);
		Edge ac = connectNodes(a, c);
		Edge bc = connectNodes(b, c);

		CompletableFuture<?> future = storage.disconnectNodes(a.getNodeId());
		future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());

		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(a.getNodeId());
		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);

		assertFalse(future1.isCompletedExceptionally());
		assertEquals(0, edges.size());

		CompletableFuture<Collection<Edge>> future2 = storage.getConnectionsTo(c.getNodeId());
		Collection<Edge> edges1 = future2.get(1, TimeUnit.SECONDS);

		assertFalse(future2.isCompletedExceptionally());
		assertEquals(List.of(bc), edges1);
	}

	@Test @Order(10)
	void disconnectNodes2() throws ExecutionException, InterruptedException, TimeoutException {
		Waypoint a = waypoint();
		Waypoint b = waypoint();
		Waypoint c = waypoint();

		Edge ab = connectNodes(a, b);
		Edge ac = connectNodes(a, c);
		Edge bc = connectNodes(b, c);

		CompletableFuture<?> future = storage.disconnectNodes(new NodeSelection(a.getNodeId()));
		future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());

		CompletableFuture<Collection<Edge>> future1 = storage.getConnections(a.getNodeId());
		Collection<Edge> edges = future1.get(1, TimeUnit.SECONDS);

		assertFalse(future1.isCompletedExceptionally());
		assertEquals(0, edges.size());

		CompletableFuture<Collection<Edge>> future2 = storage.getConnectionsTo(c.getNodeId());
		Collection<Edge> edges1 = future2.get(1, TimeUnit.SECONDS);

		assertFalse(future2.isCompletedExceptionally());
		assertEquals(List.of(bc), edges1);
	}

	NodeGroup nodeGroup(NamespacedKey key) throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<NodeGroup> future = storage.createNodeGroup(key);
		NodeGroup group = future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());
		Assertions.assertNotNull(group);

		return group;
	}

	@Test @Order(11)
	void createGroup() throws ExecutionException, InterruptedException, TimeoutException {
		nodeGroup(NamespacedKey.fromString("pathfinder:abc"));
	}

	@Test @Order(12)
	void deleteGroup() throws ExecutionException, InterruptedException, TimeoutException {
		NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
		nodeGroup(key);

		CompletableFuture<Void> future = storage.deleteNodeGroup(key);
		future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());

		CompletableFuture<NodeGroup> future1 = storage.getNodeGroup(key);
		NodeGroup group = future1.get(1, TimeUnit.SECONDS);

		assertFalse(future1.isCompletedExceptionally());
		Assertions.assertNull(group);
	}

	@Test @Order(13)
	void getNodeGroupKeySet() throws ExecutionException, InterruptedException, TimeoutException {
		NamespacedKey a = NamespacedKey.fromString("pathfinder:a");
		NamespacedKey b = NamespacedKey.fromString("pathfinder:b");

		nodeGroup(a);
		nodeGroup(b);

		CompletableFuture<Collection<NamespacedKey>> future = storage.getNodeGroupKeySet();
		Collection<NamespacedKey> keys = future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());
		assertEquals(List.of(a, b), keys);
	}

	@Test @Order(14)
	void deleteNodes() throws ExecutionException, InterruptedException, TimeoutException {
		Waypoint a = waypoint();
		waypoint();
		waypoint();

		Collection<Node<?>> nodes = storage.getNodes().get(1, TimeUnit.SECONDS);
		assertEquals(3, nodes.size());

		CompletableFuture<Void> future = storage.deleteNodes(new NodeSelection(a.getNodeId()));
		future.get(1, TimeUnit.SECONDS);

		assertFalse(future.isCompletedExceptionally());

		Collection<Node<?>> nodes1 = storage.getNodes().get(1, TimeUnit.SECONDS);
		assertEquals(2, nodes1.size());

		CompletableFuture<Void> future1 = storage.deleteNodes(new NodeSelection(UUID.randomUUID()));
		future1.get(1, TimeUnit.SECONDS);

		assertFalse(future1.isCompletedExceptionally());

		Collection<Node<?>> nodes2 = storage.getNodes().get(1, TimeUnit.SECONDS);
		assertEquals(2, nodes2.size());
	}

	@Test @Order(15)
	void assignNodesToGroup() {
	}

	@Test @Order(16)
	void deleteNodesWithGroups() throws ExecutionException, InterruptedException, TimeoutException {
		NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
		Waypoint a = waypoint();
		NodeGroup g = nodeGroup(gk);

		storage.assignNodesToGroup(gk, new NodeSelection(a.getNodeId())).get(1, TimeUnit.SECONDS);
		storage.deleteNodes(new NodeSelection(a.getNodeId())).get(1, TimeUnit.SECONDS);

		CompletableFuture<Collection<UUID>> future = storage.getNodeGroupNodes(gk);
		Collection<UUID> nodes = future.get(1, TimeUnit.SECONDS);

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
	void assignNodesToGroups() {
	}

	@Test
	void removeNodesFromGroup() {
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
