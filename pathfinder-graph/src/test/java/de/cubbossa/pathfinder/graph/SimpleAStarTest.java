package de.cubbossa.pathfinder.graph;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleAStarTest {

  // Test Graph: https://i.stack.imgur.com/VW9yr.png

  Node a = n("a", 0, 3.0);
  Node b = n("b", 2, 3.0);
  Node c = n("c", 4, 3.5);
  Node d = n("d", 3, 0.5);
  Node e = n("e", 6, 2.5);
  Node f = n("f", 5, 0.0);
  Node g = n("g", 1, 0.0);
  private Graph<Node> graph;
  private SimpleAStar<Node> dijkstra;

  private static double distSquared(Node a, Node b) {
    return Math.pow(b.x() - a.x(), 2) + Math.pow(b.y() - a.y(), 2);
  }

  private static Node n(String name, double x, double y) {
    return new Node(name, x, y);
  }

  @BeforeAll
  void setup() {

    graph = new Graph<>();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.addNode(e);
    graph.addNode(f);
    graph.addNode(g);
    graph.connect(a, b, 1);
    graph.connect(b, c, 3);
    graph.connect(b, d, 2);
    graph.connect(b, e, 1);
    graph.connect(c, d, 1);
    graph.connect(c, e, 4);
    graph.connect(d, a, 2);
    graph.connect(d, e, 2);
    graph.connect(e, f, 3);
    graph.connect(g, d, 1);

    dijkstra = new SimpleAStar<>(SimpleAStarTest::distSquared);
  }

  @Test
  void shortestPath1() throws NoPathFoundException {
    Assertions.assertEquals(List.of(a, b, e, f), dijkstra.solvePath(graph, a, f));
  }

  @Test
  void shortestPath2() {
    Assertions.assertThrows(NoPathFoundException.class, () -> dijkstra.solvePath(graph, a, g));
  }

  @Test
  void shortestPathSelf() throws NoPathFoundException {
    Assertions.assertEquals(List.of(a), dijkstra.solvePath(graph, a, a));
  }

  @Test
  void shortestPathAny() throws NoPathFoundException {
    Assertions.assertEquals(List.of(a, b, c), dijkstra.solvePath(graph, a, List.of(c, f)));
  }

  private record Node(String name, double x, double y) {
  }
}