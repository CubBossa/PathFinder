package de.cubbossa.pathfinder.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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

  @Test
  void performanceTest() throws NoPathFoundException {
    int nodes = 10_000;
    int edges = 100_000;

    Graph<UUID> g = new Graph<>();
    UUID[] ids = new UUID[nodes];
    UUID start = UUID.randomUUID();
    UUID end = UUID.randomUUID();
    g.addNode(start);
    g.addNode(end);
    ids[0] = start;
    ids[1] = end;
    for (int i = 2; i < nodes; i++) {
      UUID u = UUID.randomUUID();
      ids[i] = u;
      g.addNode(u);
    }
    Random random = new Random(1920648153);
    for (int i = 0; i < edges; i++) {
      UUID a = ids[random.nextInt(nodes)];
      UUID b = ids[random.nextInt(nodes)];
      if (a.equals(b)) continue;
      g.connect(a, b);
    }

    PathSolver<UUID> d = new SimpleDijkstra<>();
    d.solvePath(g, start, end);
  }
}