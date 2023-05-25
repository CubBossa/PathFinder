package de.cubbossa.pathfinder.graph;

import org.junit.jupiter.api.*;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GraphTest {

  private Graph<String> a, b;

  @BeforeAll
  void setup() {
    a = new Graph<>();
    a.addNode("a");
    a.addNode("b");
    a.connect("a", "b");
    b = new Graph<>();
    b.addNode("b");
    b.addNode("c");
    b.connect("b", "c");
  }

  @Test
  @Order(2)
  void merge() throws NoPathFoundException {
    a.merge(b);
    PathSolver<String> solver = new SimpleDijkstra<>();
    Assertions.assertEquals(List.of("a", "b", "c"), solver.solvePath(a, "a", "c"));
  }

  @Test
  void getWeight() {
    Assertions.assertEquals(a.getEdgeWeight("a", "b"), 1);
  }

  @Test
  @Order(0)
  void subdivide() {
    a.subdivide("a", "b", () -> "ab");
    Assertions.assertEquals(3, a.size());
    Assertions.assertTrue(a.hasConnection("a", "ab"));
    Assertions.assertTrue(a.hasConnection("ab", "b"));
    Assertions.assertFalse(a.hasConnection("a", "b"));

    a.removeNode("ab");
    a.connect("a", "b");
  }
}