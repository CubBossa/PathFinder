package de.cubbossa.pathfinder.graph;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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
  void merge() throws NoPathFoundException {
    a.merge(b);
    PathSolver<String> solver = new SimpleDijkstra<>();
    Assertions.assertEquals(List.of("a", "b", "c"), solver.solvePath(a, "a", "c"));
  }
}