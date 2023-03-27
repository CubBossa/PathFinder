package de.cubbossa.pathfinder.data;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.core.node.WaypointType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    MockBukkit.mock();
    world = new WorldMock(Material.DIRT, 1);
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
    storage.connect();
  }

  private Waypoint waypoint() {
    CompletableFuture<Waypoint> future = storage.createNode(waypointNodeType, new Location(world, 1, 2, 3));
    Waypoint waypoint = future.join();

    Assertions.assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(waypoint);

    return waypoint;
  }

  @Test
  void createNode() throws ExecutionException, InterruptedException, TimeoutException {

    CompletableFuture<Waypoint> future = storage.createNode(waypointNodeType, new Location(world, 1, 2, 3));
    Waypoint waypoint = future.get(1, TimeUnit.SECONDS);

    Assertions.assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(waypoint);

    Collection<Node<?>> nodesAfter = storage.getNodes().join();
    Assertions.assertEquals(1, nodesAfter.size());
  }

  @Test
  void getNodes() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Collection<Node<?>>> future = storage.getNodes();
    Collection<Node<?>> nodes = future.join();

    Assertions.assertFalse(future.isCompletedExceptionally());
    Assertions.assertEquals(0, nodes.size());

    waypoint();

    Collection<Node<?>> nodesAfter = storage.getNodes().get(1, TimeUnit.SECONDS);
    Assertions.assertEquals(1, nodesAfter.size());
  }

  @Test
  void getNodeType() throws ExecutionException, InterruptedException, TimeoutException {

    CompletableFuture<NodeType<?>> future1 = storage.getNodeType(waypoint().getNodeId());
    NodeType<?> type = future1.get(1, TimeUnit.SECONDS);

    Assertions.assertFalse(future1.isCompletedExceptionally());
    Assertions.assertNotNull(type);
    Assertions.assertEquals(waypointNodeType, type);
  }

  @Test
  void updateNode() throws ExecutionException, InterruptedException, TimeoutException {
    Waypoint waypoint = waypoint();
    storage.updateNode(waypoint.getNodeId(), node -> {
      node.setLocation(waypoint.getLocation().clone().add(0, 2, 0));
    }).get(1, TimeUnit.SECONDS);

    Node<?> after = storage.getNode(waypoint.getNodeId()).get(1, TimeUnit.SECONDS);

    Assertions.assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
  }
}
