package de.cubbossa.pathfinder.navigation;

import static de.cubbossa.pathfinder.navigation.NavigationLocation.fixedExternalNode;
import static de.cubbossa.pathfinder.navigation.NavigationLocation.fixedGraphNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.TestNode;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.GroupedNodeImpl;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
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

  @Test
  void testGroupedNodePreservance() throws NoPathFoundException {
    Node a = new GroupedNodeImpl(new TestNode("a", new Location(0, -5, 0, world)), new ArrayList<>());
    Node b = new GroupedNodeImpl(new TestNode("b", new Location(10, -5, 0, world)), new ArrayList<>());
    Node c = new GroupedNodeImpl(new TestNode("c", new Location(10, 5, 0, world)), new ArrayList<>());
    Node d = new GroupedNodeImpl(new TestNode("d", new Location(0, 5, 0, world)), new ArrayList<>());
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, c, 10d);
    graph.putEdgeValue(c, d, 10d);

    var results = Route
        .from(fixedExternalNode(new TestNode("start", new Location(5, -10, 0, world))))
        .to(fixedExternalNode(new TestNode("end", new Location(5, 10, 0, world))))
        .calculatePaths(graph);
    assertEquals(1, results.size());
    assertEquals(6, results.get(0).getPath().size());
    assertEquals(30, results.get(0).getCost());

    assertInstanceOf(GroupedNode.class, results.get(0).getPath().get(0));
  }
}