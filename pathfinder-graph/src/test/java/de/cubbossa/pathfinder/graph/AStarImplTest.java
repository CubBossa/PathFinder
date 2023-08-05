package de.cubbossa.pathfinder.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AStarImplTest {

  // Test Graph: https://i.stack.imgur.com/VW9yr.png

  Node a = n("a", 0, 3.0);
  Node b = n("b", 2, 3.0);
  Node c = n("c", 4, 3.5);
  Node d = n("d", 3, 0.5);
  Node e = n("e", 6, 2.5);
  Node f = n("f", 5, 0.0);
  Node g = n("g", 1, 0.0);
  private MutableValueGraph<Node, Double> graph;
  private AStarImpl<Node> solver;

  private static double distSquared(Node a, Node b) {
    return Math.pow(b.x() - a.x(), 2) + Math.pow(b.y() - a.y(), 2);
  }

  private static Node n(String name, double x, double y) {
    return new Node(name, x, y);
  }

  @BeforeAll
  void setup() {

    graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.addNode(e);
    graph.addNode(f);
    graph.addNode(g);
    graph.putEdgeValue(a, b, 1.);
    graph.putEdgeValue(b, c, 3.);
    graph.putEdgeValue(b, d, 2.);
    graph.putEdgeValue(b, e, 1.);
    graph.putEdgeValue(c, d, 1.);
    graph.putEdgeValue(c, e, 4.);
    graph.putEdgeValue(d, a, 2.);
    graph.putEdgeValue(d, e, 2.);
    graph.putEdgeValue(e, f, 3.);
    graph.putEdgeValue(g, d, 1.);

    solver = new AStarImpl<>(AStarImplTest::distSquared);
    solver.setGraph(graph);
  }

  @Test
  void shortestPath1() throws NoPathFoundException {
    Assertions.assertEquals(List.of(a, b, e, f), solver.solvePath(a, f));
  }

  @Test
  void shortestPath2() {
    Assertions.assertThrows(NoPathFoundException.class, () -> solver.solvePath(a, g));
  }

  @Test
  void shortestPathSelf() throws NoPathFoundException {
    Assertions.assertEquals(List.of(a), solver.solvePath(a, a));
  }

  @Test
  void shortestPathAny() throws NoPathFoundException {
    Assertions.assertEquals(List.of(a, b, c), solver.solvePath(a, List.of(c, f)));
  }

  private record Node(String name, double x, double y) {
  }
}