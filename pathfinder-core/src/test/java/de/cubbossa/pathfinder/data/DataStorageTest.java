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
  protected DataStorage storage;
  protected NodeTypeRegistry nodeTypeRegistry;
  protected NodeType<Waypoint> waypointNodeType;

  @BeforeAll
  static void beforeAll() {
    miniMessage = MiniMessage.miniMessage();
    MockBukkit.mock();
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

  @Test
  void createNode() {

    World world = new WorldMock(Material.DIRT, 1);
    CompletableFuture<Waypoint> future = storage.createNode(waypointNodeType, new Location(world, 1, 2, 3));
    Waypoint waypoint = future.join();

    Assertions.assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(waypoint);

    Collection<Node<?>> nodesAfter = storage.getNodes().join();
    Assertions.assertEquals(1, nodesAfter.size());
  }

  @Test
  void getNodes() {
    CompletableFuture<Collection<Node<?>>> future = storage.getNodes();
    Collection<Node<?>> nodes = future.join();

    Assertions.assertFalse(future.isCompletedExceptionally());
    Assertions.assertEquals(0, nodes.size());

    World world = new WorldMock(Material.DIRT, 1);
    Waypoint waypoint = storage.createNode(waypointNodeType, new Location(world, 1, 2, 3)).join();
    Assertions.assertNotNull(waypoint);

    Collection<Node<?>> nodesAfter = storage.getNodes().join();
    Assertions.assertEquals(1, nodesAfter.size());
  }


}
