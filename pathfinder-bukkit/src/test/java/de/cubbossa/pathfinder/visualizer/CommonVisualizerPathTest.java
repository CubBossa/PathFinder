package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.TestPlayer;
import de.cubbossa.pathfinder.TestVisualizer;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CommonVisualizerPathTest extends PathFinderTest {

  private final Map<PathVisualizer<?, ?>, NodeGroup> groupMap;

  public CommonVisualizerPathTest() {
    groupMap = new HashMap<>();
    setupMiniMessage();
    setupWorldMock();
  }

  private Waypoint pathNode(PathVisualizer<?, ?>... visualizers) {
    Waypoint waypoint = makeWaypoint();
    for (PathVisualizer<?, ?> vis : visualizers) {
      NodeGroup group = groupMap.computeIfAbsent(vis, v -> {
        NodeGroup g = makeGroup(v.getKey());
        g.addModifier(new VisualizerModifier(v));
        storage.saveGroup(g).join();
        return g;
      });
      waypoint.addGroup(group);
    }
    storage.saveNode(waypoint).join();
    return waypoint;
  }

  private TestVisualizer visualizer(String key) {
    return new TestVisualizer(NamespacedKey.fromString("pathfinder:" + key), "test");
  }

  @BeforeEach
  public void beforeEach() {
    setupInMemoryStorage();
  }

  @AfterEach
  public void afterEach() {
    shutdownStorage();
  }

  @Test
  void prepare1() {
    TestVisualizer vis = visualizer("a");
    List<Node> path = List.of(
        pathNode(vis),
        pathNode(vis)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>();
    assertFalse(visPath.isActive());
    assertEquals(0, visPath.paths.size());

    visPath.prepare(path, p);
    assertFalse(visPath.isActive());
    assertEquals(1, visPath.paths.size());
    assertEquals(Set.of(CommonPathFinder.defaultVisualizerKey(), vis.getKey()), visPath.paths.stream().map(s -> s.visualizer.getKey()).collect(Collectors.toSet()));
    assertTrue(visPath.paths.stream().allMatch(subPath -> subPath.path.size() == 2));
  }

  @Test
  void prepare2() {
    TestVisualizer a = visualizer("a");
    TestVisualizer b = visualizer("b");
    List<Node> path = List.of(
        pathNode(a),
        pathNode(a, b),
        pathNode(b)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>();
    assertFalse(visPath.isActive());
    assertEquals(0, visPath.paths.size());

    visPath.prepare(path, p);
    assertFalse(visPath.isActive());
    assertEquals(2, visPath.paths.size());
    assertTrue(visPath.paths.stream().anyMatch(subPath -> subPath.visualizer.equals(a)));
    assertTrue(visPath.paths.stream().anyMatch(subPath -> subPath.visualizer.equals(b)));
  }

  @Test
  void prepare3() {
    TestVisualizer a = visualizer("a");
    TestVisualizer b = visualizer("b");
    List<Node> path = List.of(
        pathNode(a),
        pathNode(b),
        pathNode(a)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>();
    assertFalse(visPath.isActive());
    assertEquals(0, visPath.paths.size());

    visPath.prepare(path, p);
    assertFalse(visPath.isActive());
    assertEquals(3, visPath.paths.size());
    assertEquals(2, visPath.paths.stream().filter(subPath -> subPath.visualizer.equals(a)).count());
    assertEquals(1, visPath.paths.stream().filter(subPath -> subPath.visualizer.equals(b)).count());
    assertTrue(visPath.paths.stream().allMatch(subPath -> subPath.path.size() == 1));
  }

  @Test
  void prepare4() {
    TestVisualizer vis = visualizer("a");
    List<Node> path = List.of(
        pathNode(vis),
        pathNode(vis)
    );

    PathPlayer<Object> p = new TestPlayer();
    CommonVisualizerPath<Object> visPath = new CommonVisualizerPath<>();
    assertFalse(visPath.isActive());
    assertEquals(0, visPath.paths.size());

    visPath.prepare(path, p);
    assertFalse(visPath.isActive());
    assertEquals(1, visPath.paths.size());
    assertTrue(visPath.paths.stream().allMatch(subPath -> subPath.path.size() == 2));
  }
}