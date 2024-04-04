package de.cubbossa.pathfinder.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.TestModifier;
import de.cubbossa.pathfinder.TestModifierType;
import de.cubbossa.pathfinder.TestVisualizer;
import de.cubbossa.pathfinder.node.NodeSelectionImpl;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public abstract class StorageTest extends PathFinderTest {

  protected File storageTestDir = null;
  protected boolean useCaches = false;

  abstract StorageImplementation storage(File dir);


  public StorageTest() {
    setupMiniMessage();
    setupWorldMock();
  }

  @BeforeEach
  @Timeout(value = 2)
  void beforeEach() {
    try {
      if (storageTestDir != null && storageTestDir.exists()) {
        for (File file : storageTestDir.listFiles()) {
          file.deleteOnExit();
        }
        storageTestDir.delete();
      }
      storageTestDir = Files.createTempDirectory("pathfinder_storage_test").toFile();
      setupPathFinder(useCaches, () -> storage(storageTestDir));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void afterEach() {
    shutdownPathFinder();
    for (File file : storageTestDir.listFiles()) {
      file.deleteOnExit();
    }
    storageTestDir.delete();
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void createNode() {
    assertNodeCount(0);
    Waypoint waypoint = makeWaypoint();
    assertNodeExists(waypoint.getNodeId());
    assertNodeCount(1);
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void createNodeWithDefaultGroup() {
    Waypoint waypoint = makeWaypoint();
    assertEquals(1, getGroups(waypoint).size());

    Waypoint loaded = assertNodeExists(waypoint.getNodeId());
    assertEquals(1, getGroups(loaded).size());
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void getNodes() {
    assertNodeCount(0);
    makeWaypoint();
    assertNodeCount(1);
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  <N extends Node> void getNodeType() {
    Waypoint waypoint = makeWaypoint();
    Optional<NodeType<N>> type = assertResult(() -> storage.loadNodeType(waypoint.getNodeId()));
    assertTrue(type.isPresent());
    assertEquals(waypointNodeType, type.get());
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void updateNode() {
    Waypoint waypoint = makeWaypoint();
    assertFuture(() -> storage.modifyNode(waypoint.getNodeId(), node -> {
      node.setLocation(waypoint.getLocation().clone().add(0, 2, 0));
    }));
    Node after = assertNodeExists(waypoint.getNodeId());
    assertEquals(waypoint.getLocation().add(0, 2, 0), after.getLocation());
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void testLoadNodesByModifier() {
    NamespacedKey gk = AbstractPathFinder.pathfinder("testxy");
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();
    NodeGroup g = makeGroup(gk);
    Modifier m = new TestModifier("abc");
    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.add(a.getNodeId());
      group.addModifier(m);
    }));
    NodeGroup after = assertGroupExists(gk);
    assertTrue(after.hasModifier(TestModifierType.KEY));
    assertTrue(after.contains(a.getNodeId()));

    Map<Node, Collection<TestModifier>> nodes = assertResult(() -> storage.loadNodes(TestModifierType.KEY));
    assertEquals(1, nodes.size());
    assertTrue(nodes.containsKey(a));
    assertFalse(nodes.containsKey(b));
    assertNotNull(nodes.get(a));
    assertEquals(List.of(m), nodes.get(a));
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void testConnectNodes() {
    Waypoint a = makeWaypoint();
    Waypoint b = makeWaypoint();
    assertNoEdge(a.getNodeId(), b.getNodeId());
    makeEdge(a, b);
    assertEdge(a.getNodeId(), b.getNodeId());
  }

  @SneakyThrows
  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
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
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void createGroup() {
    NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
    assertGroupNotExists(key);
    makeGroup(key);
    assertGroupExists(key);
  }

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void deleteGroup() {
    NamespacedKey key = NamespacedKey.fromString("pathfinder:abc");
    assertGroupNotExists(key);
    makeGroup(key);
    assertGroupExists(key);
    deleteGroup(key);
    assertGroupNotExists(key);
  }

  @Test
  void loadGroupsOfNodes() {
    NamespacedKey xKey = NamespacedKey.fromString("pathfinder:x");
    NamespacedKey yKey = NamespacedKey.fromString("pathfinder:y");
    Waypoint waypoint = makeWaypoint();

    NodeGroup x = makeGroup(xKey);
    NodeGroup y = makeGroup(yKey);
    Collection<NodeGroup> groups = Set.of(globalGroup, x, y);

    groups.forEach(nodeGroup -> {
      nodeGroup.add(waypoint.getNodeId());
      storage.saveGroup(nodeGroup).join();
    });

    var result = storage.loadGroupsOfNodes(Collections.singletonList(waypoint)).join();
    Assertions.assertEquals(
            groups.stream().map(Keyed::getKey).collect(Collectors.toSet()),
            result.get(waypoint).stream().map(Keyed::getKey).collect(Collectors.toSet())
    );
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void loadGroupsByMod() {
    NamespacedKey xKey = NamespacedKey.fromString("pathfinder:x");
    NamespacedKey yKey = NamespacedKey.fromString("pathfinder:y");
    Waypoint waypoint = makeWaypoint();

    NodeGroup x = makeGroup(xKey);
    NodeGroup y = makeGroup(yKey);
    Collection<NodeGroup> groups = Set.of(x, y);

    int i = 0;
    for (NodeGroup g : groups) {
      g.addModifier(new TestModifier(i++ + ""));
      g.add(waypoint.getNodeId());
      storage.saveGroup(g).join();
    }

    Map<Node, Collection<String>> expected = Map.of(waypoint, groups.stream()
        .map(group -> group.<TestModifier>getModifier(TestModifierType.KEY))
        .filter(Optional::isPresent).map(Optional::get)
        .map(TestModifier::data)
        .collect(Collectors.toList()));
    Map<Node, Collection<String>> result = storage.<TestModifier>loadNodes(TestModifierType.KEY).join().entrySet().stream()
        .map(e -> Map.entry(e.getKey(), e.getValue().stream().map(TestModifier::data).collect(Collectors.toList())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Assertions.assertTrue(Maps.difference(expected, result).areEqual());
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void loadGroupsByMod2() {
    NamespacedKey xKey = NamespacedKey.fromString("pathfinder:x");
    NamespacedKey yKey = NamespacedKey.fromString("pathfinder:y");

    NodeGroup x = makeGroup(xKey);
    NodeGroup y = makeGroup(yKey);
    Set<NodeGroup> groups = Set.of(x, y);

    int i = 0;
    for (NodeGroup g : groups) {
      g.addModifier(new TestModifier(i++ + ""));
      storage.saveGroup(g).join();
    }

    Set<NodeGroup> result = new HashSet<>(storage.loadGroupsByMod(Collections.singletonList(TestModifierType.KEY)).join());
    assertTrue(Sets.difference(groups, result).isEmpty());
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void getNodeGroupKeySet() {
    NamespacedKey a = NamespacedKey.fromString("pathfinder:a");
    NamespacedKey b = NamespacedKey.fromString("pathfinder:b");

    makeGroup(a);
    makeGroup(b);

    Collection<NamespacedKey> keys = assertResult(() -> storage.loadAllGroups()
        .thenApply(nodeGroups -> nodeGroups.stream()
            .map(NodeGroup::getKey)
            .collect(Collectors.toSet())));
    assertEquals(Set.of(a, b, AbstractPathFinder.globalGroupKey()), keys);
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void deleteNodes() {
    assertNodeCount(0);
    Waypoint a = makeWaypoint();
    makeWaypoint();
    assertNodeCount(2);
    assertFuture(() -> storage.deleteNodes(new NodeSelectionImpl(a).getIds()));
    assertNodeCount(1);
    assertFuture(() -> storage.deleteNodes(List.of(UUID.randomUUID())));
    assertNodeCount(1);
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void assignNodesToGroup() {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = makeWaypoint();
    NodeGroup g = makeGroup(gk);

    System.out.println("#".repeat(30));

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.add(a.getNodeId());
    }));

    Waypoint waypoint = assertNodeExists(a.getNodeId());
    assertTrue(getGroups(waypoint).stream().map(NodeGroup::getKey).anyMatch(k -> k.equals(gk)));
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void unassignNodesFromGroup() {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = makeWaypoint();
    NodeGroup g = makeGroup(gk);

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.add(a.getNodeId());
    }));

    Waypoint a1 = assertNodeExists(a.getNodeId());
    assertTrue(getGroups(a1).stream().map(NodeGroup::getKey).anyMatch(gk::equals));
    NodeGroup g1 = assertGroupExists(gk);
    assertTrue(g1.contains(a.getNodeId()));

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.remove(a.getNodeId());
    }));

    NodeGroup g2 = assertGroupExists(gk);
    assertFalse(g2.contains(a.getNodeId()));
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void deleteNodesWithGroups() {
    NamespacedKey gk = NamespacedKey.fromString("pathfinder:g");
    Waypoint a = makeWaypoint();
    NodeGroup g = makeGroup(gk);

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.remove(a.getNodeId());
    }));
    assertFuture(() -> storage.deleteNodes(new NodeSelectionImpl(a).getIds()));

    NodeGroup g1 = assertGroupExists(gk);
    assertEquals(0, g1.size());
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
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
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void setModifier() {
    NamespacedKey gk = NamespacedKey.fromString("test:abc");
    NodeGroup g = makeGroup(gk);
    Modifier mod = new TestModifier("abc");

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.addModifier(mod);
    }));
    NodeGroup g1 = assertGroupExists(gk);
    assertTrue(g1.hasModifier(TestModifier.class));
    assertEquals("abc", g1.<TestModifier>getModifier(TestModifierType.KEY).orElseThrow().data());

    assertFuture(() -> storage.modifyGroup(gk, group -> {
      group.removeModifier(TestModifier.class);
    }));
    NodeGroup g2 = assertGroupExists(gk);
    assertFalse(g2.hasModifier(TestModifier.class));
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void createVisualizer() {
    NamespacedKey key = AbstractPathFinder.pathfinder("abc");
    TestVisualizer visualizer = makeVisualizer(key);
  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void deleteVisualizer() {

  }

  @Test
  @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
  void loadVisualizer() {

  }
}
