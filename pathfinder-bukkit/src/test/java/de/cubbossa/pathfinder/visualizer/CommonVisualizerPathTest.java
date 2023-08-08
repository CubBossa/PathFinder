package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.TestPlayer;
import de.cubbossa.pathfinder.TestVisualizer;
import de.cubbossa.pathfinder.node.SimpleGroupedNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.nodegroup.modifier.CommonVisualizerModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.CurveLengthModifierType;
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifierType;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifierType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonVisualizerPathTest extends PathFinderTest {

  private final Map<PathVisualizer<?, ?>, NodeGroup> groupMap;

  public CommonVisualizerPathTest() {
    groupMap = new HashMap<>();
    setupMiniMessage();
    setupWorldMock();
  }

  private SimpleGroupedNode pathNode(PathVisualizer<?, ?>... visualizers) {
    Waypoint waypoint = makeWaypoint();
    Collection<NodeGroup> groups = new HashSet<>();
    for (PathVisualizer<?, ?> vis : visualizers) {
      NodeGroup group = groupMap.computeIfAbsent(vis, v -> {
        NodeGroup g = makeGroup(v.getKey());
        g.addModifier(new CommonVisualizerModifier(v.getKey()));
        g.add(waypoint.getNodeId());
        storage.saveGroup(g).join();
        return g;
      });
      groups.add(group);
    }
    groups.add(globalGroup);
    return new SimpleGroupedNode(waypoint, groups);
  }

  private TestVisualizer visualizer(String key) {
    return storage.createAndLoadVisualizer(visualizerType, NamespacedKey.fromString("pathfinder:" + key)).join();
  }

  @BeforeEach
  public void beforeEach() {
    modifierRegistry = new ModifierRegistryImpl();
    pathFinder.getModifierRegistry().registerModifierType(new VisualizerModifierType());
    pathFinder.getModifierRegistry().registerModifierType(new CurveLengthModifierType());
    pathFinder.getModifierRegistry().registerModifierType(new FindDistanceModifierType());
    setupPathFinder();
  }

  @AfterEach
  public void afterEach() {
    shutdownPathFinder();
  }

  @Test
  void prepare1() {
    TestVisualizer vis = visualizer("a");
    List<GroupedNode> path = List.of(
        pathNode(vis),
        pathNode(vis)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>(path, p);
    assertTrue(visPath.isActive());
    assertEquals(1, visPath.paths.size());
    assertEquals(Set.of(vis.getKey()), visPath.paths.stream().map(s -> s.visualizer.getKey()).collect(Collectors.toSet()));
    assertTrue(visPath.paths.stream().allMatch(subPath -> subPath.path.size() == 2));

    storage.deleteVisualizer(vis).join();
  }

  @Test
  void prepare2() {
    TestVisualizer a = visualizer("a");
    TestVisualizer b = visualizer("b");
    List<GroupedNode> path = List.of(
        pathNode(a),
        pathNode(a, b),
        pathNode(b)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>(path, p);
    assertTrue(visPath.isActive());
    assertEquals(2, visPath.paths.size());
    assertTrue(visPath.paths.stream().anyMatch(subPath -> subPath.visualizer.equals(a)));
    assertTrue(visPath.paths.stream().anyMatch(subPath -> subPath.visualizer.equals(b)));

    storage.deleteVisualizer(a).join();
    storage.deleteVisualizer(b).join();
  }

  @Test
  void prepare3() {
    TestVisualizer a = visualizer("a");
    TestVisualizer b = visualizer("b");
    List<GroupedNode> path = List.of(
        pathNode(a),
        pathNode(b),
        pathNode(a)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>(path, p);
    assertTrue(visPath.isActive());
    assertEquals(3, visPath.paths.size());
    assertEquals(2, visPath.paths.stream().filter(subPath -> subPath.visualizer.equals(a)).count());
    assertEquals(1, visPath.paths.stream().filter(subPath -> subPath.visualizer.equals(b)).count());
    assertTrue(visPath.paths.stream()
        .filter(subPath -> !subPath.visualizer.getKey().toString().contains("default"))
        .allMatch(subPath -> subPath.path.size() == 1));

    storage.deleteVisualizer(a).join();
    storage.deleteVisualizer(b).join();
  }

  @Test
  void prepare4() {
    TestVisualizer vis = visualizer("a");
    List<GroupedNode> path = List.of(
        pathNode(vis),
        pathNode(vis)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>(path, p);
    assertTrue(visPath.isActive());
    assertEquals(1, visPath.paths.size());
    assertTrue(visPath.paths.stream().allMatch(subPath -> subPath.path.size() == 2));

    storage.deleteVisualizer(vis).join();
  }
}