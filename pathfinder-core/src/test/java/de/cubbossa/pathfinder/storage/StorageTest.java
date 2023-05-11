package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.*;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.visualizer.impl.ParticleVisualizer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public abstract class StorageTest extends PathFinderTest {

  protected boolean useCaches = false;

  abstract StorageImplementation storage(NodeTypeRegistry registry,
                                         ModifierRegistry modifierRegistry,
                                         VisualizerTypeRegistry visualizerTypeRegistry);


  public StorageTest() {
    setupMiniMessage();
    setupWorldMock("test");
  }

  @BeforeEach
  void beforeEach() {
    setupStorage(useCaches, () -> storage(nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry));
  }

  @AfterEach
  void afterEach() {
    shutdownStorage();
  }

  @Test
  @Order(1)
  void createNode() {
    assertNodeCount(0);
    Waypoint waypoint = makeWaypoint();
    assertNodeExists(waypoint.getNodeId());
    assertNodeCount(1);
  }

  @Test
  @Order(2)
  void getNodes() {
    assertNodeCount(0);
    makeWaypoint();
    assertNodeCount(1);
  }

  @Test
  @Order(3)
  <N extends Node> void getNodeType() {
    Waypoint waypoint = makeWaypoint();
    Optional<NodeType<N>> type = assertResult(() -> storage.loadNodeType(waypoint.getNodeId()));
    assertTrue(type.isPresent());
    assertEquals(waypointNodeType, type.get());
  }

  @Test
  @Order(4)
  void updateNode() {
    Waypoint waypoint = makeWaypoint();
    assertFuture(() -> storage.modifyNode(waypoint.getNodeId(), node -> {
      node.setLocation(waypoint.getLocation().clone().add(0, 2, 0));
    }));
    Node after = assertNodeExists(waypoint.getNodeId());
    assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
  }

  @Test
  @Order(6)
  void testLoadNodesByModifier() {
    NamespacedKey gk = CommonPathFinder.pathfinder("testxy");
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();
    NodeGroup g = makeGroup(gk);
    Modifier m = new PermissionModifier("abc");
    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.add(a.getNodeId());
      group.addModifier(m);
    }));
    NodeGroup after = assertGroupExists(gk);
    assertTrue(after.hasModifier(PermissionModifier.class));
    assertTrue(after.contains(a.getNodeId()));

    Map<Node, Collection<PermissionModifier>> nodes = assertResult(() -> storage.loadNodes(PermissionModifier.class));
    assertEquals(1, nodes.size());
    assertTrue(nodes.containsKey(a));
    assertFalse(nodes.containsKey(b));
    assertNotNull(nodes.get(a));
    assertEquals(Set.of(m), nodes.get(a));
  }

  @Test
  @Order(6)
  void testConnectNodes() {
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();
    assertNoEdge(a.getNodeId(), b.getNodeId());
    makeEdge(a, b);
    assertEdge(a.getNodeId(), b.getNodeId());
  }

  @SneakyThrows
  @Test
  @Order(7)
  void disconnectNodes() {
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();
    assertNoEdge(a.getNodeId(), b.getNodeId());

    Edge edge = makeEdge(a, b);
    assertEdge(a.getNodeId(), b.getNodeId());

    assertFuture(() -> storage.modifyNode(edge.getStart(), node -> {
      node.disconnect(edge.getEnd());
    }));
    assertNoEdge(a.getNodeId(), b.getNodeId());
  }

  @Test
  @Order(11)
  void createGroup() {
    NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
    assertGroupNotExists(key);
    makeGroup(key);
    assertGroupExists(key);
  }

  @Test
  @Order(12)
  void deleteGroup() {
    NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
    assertGroupNotExists(key);
    makeGroup(key);
    assertGroupExists(key);
    deleteGroup(key);
    assertGroupNotExists(key);
  }

  @Test
  @Order(13)
  void getNodeGroupKeySet() {
    NamespacedKey a = NamespacedKey.fromString("pathfinder:a");
    NamespacedKey b = NamespacedKey.fromString("pathfinder:b");

    makeGroup(a);
    makeGroup(b);

    Collection<NamespacedKey> keys = assertResult(() -> storage.loadAllGroups()
        .thenApply(nodeGroups -> nodeGroups.stream()
            .map(NodeGroup::getKey)
            .collect(Collectors.toSet())));
    assertEquals(Set.of(a, b, CommonPathFinder.globalGroupKey()), keys);
  }

  @Test
  @Order(14)
  void deleteNodes() {
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
  void assignNodesToGroup() {
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
  void unassignNodesFromGroup() {
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
  void deleteNodesWithGroups() {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = makeWaypoint();
    NodeGroup g = makeGroup(gk);

    assertFuture(() -> storage.modifyNode(a.getNodeId(), node -> {
      if (node instanceof Groupable groupable) {
        groupable.addGroup(g);
      }
    }));
    assertFuture(() -> storage.deleteNodes(new NodeSelection(a)));

    NodeGroup g1 = assertGroupExists(gk);
    assertEquals(0, g1.size());
  }

  @Test
  void deleteNodeWithEdges() {
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();

    assertFuture(() -> storage.modifyNode(a.getNodeId(), n -> {
      n.connect(b);
    }));

    assertEdge(a.getNodeId(), b.getNodeId());
    deleteWaypoint(b);

    Waypoint a1 = assertNodeExists(a.getNodeId());
    assertFalse(a1.hasConnection(b));
  }

  @Test
  void setModifier() {
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
    NamespacedKey key = CommonPathFinder.pathfinder("abc");
    ParticleVisualizer visualizer = makeVisualizer(key);
  }

  @Test
  void deleteVisualizer() {

  }

  @Test
  void loadVisualizer() {

  }
}
