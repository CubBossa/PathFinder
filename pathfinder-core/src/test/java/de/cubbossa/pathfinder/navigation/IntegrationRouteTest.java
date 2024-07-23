package de.cubbossa.pathfinder.navigation;

import static de.cubbossa.pathfinder.navigation.NavigationLocation.fixedExternalNode;
import static de.cubbossa.pathfinder.navigation.NavigationLocation.fixedGraphNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.TestNode;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class IntegrationRouteTest extends PathFinderTest {

  @BeforeAll
  static void beforeAll() {
    NavigationLocationImpl.GRAPH_ENTRY_SOLVER = new EdgeBasedGraphEntrySolver();
  }

  @BeforeEach
  void beforeEach() {
    setupWorldMock();
    setupPathFinder();
  }

  @AfterEach
  void afterEach() {
    shutdownPathFinder();
  }

  @Test
  void testA() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(0, 0, 0, world));
    Node b = new TestNode("b", new Location(10, 0, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, a, 10d);
    var result = Route.from(a).to(b).calculatePath(graph);
    assertEquals(List.of(a, b), result.getPath());
    assertEquals(10, result.getCost());

    result = Route.from(a).to(b).to(a).to(b).calculatePath(graph);
    assertEquals(List.of(a, b, a, b), result.getPath());
    assertEquals(30, result.getCost());

    result = Route.from(a).to(a).to(a).calculatePath(graph);
    assertEquals(List.of(a), result.getPath());
  }

  @Test
  void testB() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(0, 0, 0, world));
    Node b = new TestNode("b", new Location(10, 0, 0, world));
    Node c = new TestNode("c", new Location(20, 0, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, a, 10d);
    graph.putEdgeValue(c, b, 10d);
    graph.putEdgeValue(b, c, 10d);
    graph.putEdgeValue(a, c, 20d);
    graph.putEdgeValue(c, a, 20d);

    var result = Route
        .from(a)
        .to(Route.from(b).to(a).to(b))
        .to(c)
        .calculatePath(graph);
    assertEquals(40, result.getCost());
    assertEquals(List.of(a, b, a, b, c), result.getPath());

    result = Route
        .from(a)
        .to(c)
        .to(Route.from(b).to(a).to(b))
        .calculatePath(graph);
    assertEquals(50, result.getCost());
    assertEquals(List.of(a, c, b, a, b), result.getPath());

    result = Route
        .from(a)
        .to(Route.from(a).to(b).to(a))
        .to(a)
        .calculatePath(graph);
    assertEquals(20, result.getCost());
    assertEquals(List.of(a, b, a), result.getPath());
  }

  @Test
  void testC() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(-10, 0, 0, world));
    Node b = new TestNode("b", new Location(0, 0, 0, world));
    Node c = new TestNode("c", new Location(10, 0, 0, world));
    Node d = new TestNode("d", new Location(20, 0, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(c, b, 10d);
    graph.putEdgeValue(b, c, 10d);

    var result = Route
        .from(fixedExternalNode(a))
        .to(fixedExternalNode(d))
        .calculatePath(graph);

    assertEquals(30, result.getCost());
    assertEquals(
        Stream.of(a, b, c, d).map(Node::getLocation).toList(),
        result.getPath().stream().map(Node::getLocation).toList()
    );
  }

  @Test
  void testD() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(-10, 0, 0, world));
    Node b = new TestNode("b", new Location(0, 0, 0, world));
    Node c = new TestNode("c", new Location(10, 0, 0, world));
    Node d = new TestNode("d", new Location(0, 15, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, c, 10d);
    graph.putEdgeValue(b, d, 15d);

    var results = Route
        .from(fixedGraphNode(a))
        .toAny(fixedExternalNode(c), fixedGraphNode(d))
        .calculatePaths(graph);
    assertEquals(2, results.size());
    assertEquals(results.get(0).getPath().get(0), a);
    assertEquals(3, results.get(0).getPath().size());
    assertEquals(3, results.get(1).getPath().size());
    assertEquals(c, results.get(0).getPath().get(results.get(0).getPath().size() - 1));
    assertEquals(d, results.get(1).getPath().get(results.get(1).getPath().size() - 1));

    var result = Route
        .from(fixedGraphNode(a))
        .toAny(fixedExternalNode(c), fixedGraphNode(d))
        .calculatePath(graph);

    assertEquals(List.of(a, b, c), result.getPath());
    assertEquals(20, result.getCost());
  }

  @Test
  void testE() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(0, 0, 0, world));
    Node b = new TestNode("b", new Location(10, 0, 0, world));
    Node start = new TestNode("start", new Location(20, 0, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.putEdgeValue(a, b, 10d);
    var result = Route
        .from(NavigationLocation.movingExternalNode(start))
        .to(b)
        .calculatePath(graph);
    assertEquals(List.of(start, b), result.getPath());
    assertEquals(10, result.getCost());
  }

  @Test
  void testF() throws NoPathFoundException {
    Node start = new TestNode("start", new Location(0, 0, 0, world));
    Node a = new TestNode("a", new Location(1, 0, 0, world));
    Node b = new TestNode("b", new Location(2, 0, 0, world));
    Node c = new TestNode("c", new Location(3, 0, 0, world));
    Node d = new TestNode("d", new Location(4, 0, 0, world));
    Node end = new TestNode("end", new Location(5, 0, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.putEdgeValue(a, b, 1d);
    graph.putEdgeValue(b, a, 1d);
    graph.putEdgeValue(c, b, 1d);
    graph.putEdgeValue(c, d, 1d);
    graph.putEdgeValue(d, c, 1d);

    var result = Route
        .from(NavigationLocation.fixedExternalNode(start))
        .to(NavigationLocation.fixedExternalNode(end));
    Assertions.assertThrows(NoPathFoundException.class, () -> result.calculatePath(graph));
  }

  @Test
  void testIslands() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(0, -5, 0, world));
    Node b = new TestNode("b", new Location(10, -5, 0, world));
    Node c = new TestNode("c", new Location(0, 5, 0, world));
    Node d = new TestNode("d", new Location(10, 5, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, a, 10d);
    graph.putEdgeValue(c, d, 10d);
    graph.putEdgeValue(d, c, 10d);

    var results = Route
        .from(fixedExternalNode(new TestNode("start", new Location(5, -10, 0, world))))
        .to(fixedExternalNode(new TestNode("end", new Location(5, 10, 0, world))))
        .calculatePaths(graph);
    assertEquals(1, results.size());
    assertEquals(5, results.get(0).getPath().size());
  }

//  @Test
//  void testGroupedNodePreservance() throws NoPathFoundException {
//    Node a = new GroupedNodeImpl(new TestNode("a", new Location(0, -5, 0, world)), new ArrayList<>());
//    Node b = new GroupedNodeImpl(new TestNode("b", new Location(10, -5, 0, world)), new ArrayList<>());
//    Node c = new GroupedNodeImpl(new TestNode("c", new Location(10, 5, 0, world)), new ArrayList<>());
//    Node d = new GroupedNodeImpl(new TestNode("d", new Location(0, 5, 0, world)), new ArrayList<>());
//    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
//    graph.addNode(a);
//    graph.addNode(b);
//    graph.addNode(c);
//    graph.addNode(d);
//    graph.putEdgeValue(a, b, 10d);
//    graph.putEdgeValue(b, c, 10d);
//    graph.putEdgeValue(c, d, 10d);
//
//    var results = Route
//        .from(fixedExternalNode(new TestNode("start", new Location(5, -10, 0, world))))
//        .to(fixedExternalNode(new TestNode("end", new Location(5, 10, 0, world))))
//        .calculatePaths(graph);
//    assertEquals(1, results.size());
//    assertEquals(6, results.get(0).getPath().size());
//    assertEquals(30, results.get(0).getCost());
//
//    assertInstanceOf(GroupedNode.class, results.get(0).getPath().get(0));
//  }

  @Test
  void testPerformance() {

    var graph = generateGraph(10_000);

    Node startNode = new TestNode("start", new Location(-500, -500, -500, world));
    Node endNode = new TestNode("end", new Location(500, 500, 500, world));

    System.out.println("Beginn navigation on graph with " + graph.edges().size() + " edges.");
    long now = System.currentTimeMillis();
    System.out.println(now);
    try {
      Route
          .from(NavigationLocation.fixedExternalNode(startNode))
          .to(NavigationLocation.fixedExternalNode(endNode))
          .calculatePath(graph);
    } catch (NoPathFoundException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Complete!");
    long after = System.currentTimeMillis();
    System.out.println("Time taken: " + (after - now));
  }

  private ValueGraph<Node, Double> generateGraph(int size) {
    long seed = System.currentTimeMillis();
    System.out.println("Seed = " + seed);
    Random r = new Random(seed);
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();

    List<Node> nodes = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      var n = new TestNode(UUID.randomUUID(), new Location(
          r.nextDouble(-1000, 1000),
          r.nextDouble(-1000, 1000),
          r.nextDouble(-1000, 1000),
          world
      ));
      graph.addNode(n);
      nodes.add(n);
    }
    int prog = 0;
    for (Node node : new ArrayList<>(nodes)) {
      int edgeCount = r.nextDouble() > .9 ? r.nextInt(0, 6) : r.nextInt(2, 3);
      for (int i = 0; i < edgeCount; i++) {

//        nodes.sort(Comparator.comparingDouble(n -> n.getLocation().distanceSquared(node.getLocation())));
        int edgeTargetIndex = (int) Math.pow(r.nextDouble(), 4) * graph.nodes().size();
        Node target = nodes.get(edgeTargetIndex);
        if (target.getNodeId() == node.getNodeId()) {
          continue;
        }

        double dist = node.getLocation().distanceSquared(target.getLocation());
        graph.putEdgeValue(node, target, dist);
        if (r.nextDouble() > 0.05f) {
          graph.putEdgeValue(target, node, dist);
        }
      }
      prog++;
      if (prog % 50 == 0) {
        System.out.println(prog + "/" + size);
      }
    }
    return graph;
  }
}