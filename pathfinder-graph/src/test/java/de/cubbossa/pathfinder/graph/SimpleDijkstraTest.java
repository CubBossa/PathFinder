package de.cubbossa.pathfinder.graph;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleDijkstraTest {

  // Test Graph: https://i.stack.imgur.com/VW9yr.png

  private Graph<String> graph;
  private SimpleDijkstra<String> dijkstra;

  @BeforeAll
  void setup() {
    graph = new Graph<>();
    graph.addNode("a");
    graph.addNode("b");
    graph.addNode("c");
    graph.addNode("d");
    graph.addNode("e");
    graph.addNode("f");
    graph.addNode("g");
    graph.addNode("h");
    graph.addNode("i");
    graph.addNode("j");
    graph.connect("a", "b", 1);
    graph.connect("b", "c", 3);
    graph.connect("b", "d", 2);
    graph.connect("b", "e", 1);
    graph.connect("c", "d", 1);
    graph.connect("c", "e", 4);
    graph.connect("d", "a", 2);
    graph.connect("d", "e", 2);
    graph.connect("e", "f", 3);
    graph.connect("g", "d", 1);
    graph.connect("h", "j", 1);
    graph.connect("i", "h", 1);
    graph.connect("j", "i", 1);

    dijkstra = new SimpleDijkstra<>();
  }

  @Test
  void shortestPath1() throws NoPathFoundException {
    Assertions.assertEquals(List.of("a", "b", "e", "f"), dijkstra.solvePath(graph, "a", "f"));
  }

  @Test
  void shortestPath2() {
    Assertions.assertThrows(NoPathFoundException.class, () -> dijkstra.solvePath(graph, "a", "g"));
  }

  @Test
  void shortestPathSelf() throws NoPathFoundException {
    Assertions.assertEquals(List.of("a"), dijkstra.solvePath(graph, "a", "a"));
  }

  @Test
  void shortestPathAny() throws NoPathFoundException {
    Assertions.assertEquals(List.of("a", "b", "c"),
        dijkstra.solvePath(graph, "a", List.of("c", "f")));
  }

  @Test
  void shortestPathAnySeparated() throws NoPathFoundException {
    Assertions.assertEquals(List.of("a", "b", "c"),
        dijkstra.solvePath(graph, "a", List.of("c", "i")));
  }
}