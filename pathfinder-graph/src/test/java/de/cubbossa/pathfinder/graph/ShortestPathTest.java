package de.cubbossa.pathfinder.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class ShortestPathTest {


  private MutableValueGraph<String, Double> graph;

  abstract <T> PathSolver<T, Double> solver();

  @BeforeAll
  void setup() {
    graph = ValueGraphBuilder.undirected().build();
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
    graph.addNode("k");
    graph.putEdgeValue("a", "b", 3.);
    graph.putEdgeValue("a", "c", 5.);
    graph.putEdgeValue("a", "k", 3.);
    graph.putEdgeValue("b", "c", 3.);
    graph.putEdgeValue("b", "d", 5.);
    graph.putEdgeValue("c", "d", 2.);
    graph.putEdgeValue("c", "j", 2.);
    graph.putEdgeValue("d", "j", 4.);
    graph.putEdgeValue("d", "e", 7.);
    graph.putEdgeValue("e", "f", 6.);
    graph.putEdgeValue("e", "j", 3.);
    graph.putEdgeValue("f", "g", 4.);
    graph.putEdgeValue("f", "h", 2.);
    graph.putEdgeValue("g", "h", 3.);
    graph.putEdgeValue("g", "i", 5.);
    graph.putEdgeValue("h", "i", 3.);
    graph.putEdgeValue("h", "j", 2.);
    graph.putEdgeValue("i", "j", 4.);
    graph.putEdgeValue("i", "k", 6.);
    graph.putEdgeValue("j", "k", 3.);
  }

  @Test
  void shortestPath1() throws NoPathFoundException {
    PathSolver<String, Double> solver = solver();
    solver.setGraph(graph);
    Assertions.assertEquals(List.of("a", "b"), solver.solvePath("a", "b"));
  }

  @Test
  void shortestPathSelf() throws NoPathFoundException {
    PathSolver<String, Double> solver = solver();
    solver.setGraph(graph);
    Assertions.assertEquals(List.of("a"), solver.solvePath("a", "a"));
  }

  @Test
  void shortestPathAny() throws NoPathFoundException {
    PathSolver<String, Double> solver = solver();
    solver.setGraph(graph);
    Assertions.assertEquals(List.of("b", "a", "k"), solver.solvePath("b", List.of("k", "f")));
    Assertions.assertEquals(List.of("b", "a", "k"), solver.solvePath("b", List.of("k", "f")));
  }

  @Test
  void shortestPathAnySeparated() throws NoPathFoundException {
    PathSolver<String, Double> solver = solver();
    solver.setGraph(graph);
    Assertions.assertEquals(List.of("b", "c", "j", "h", "g"), solver.solvePath("b", List.of("g")));
  }

  @RequiredArgsConstructor
  @EqualsAndHashCode(onlyExplicitlyIncluded = true)
  static class Heavy {
    @EqualsAndHashCode.Include
    public final UUID id;
    public final String load;
  }

  @RepeatedTest(10)
  void performanceTestOpt() throws NoPathFoundException {
    int nodes = 100_000;
    int edges = 200_000;
    int heavySize = 0x1000;

    MutableValueGraph<StaticDijkstraTest.Heavy, Double> g = ValueGraphBuilder.directed().build();
    StaticDijkstraTest.Heavy[] ids = new StaticDijkstraTest.Heavy[nodes];
    StaticDijkstraTest.Heavy start = new StaticDijkstraTest.Heavy(UUID.randomUUID(), "a".repeat(heavySize));
    StaticDijkstraTest.Heavy end = new StaticDijkstraTest.Heavy(UUID.randomUUID(), "a".repeat(heavySize));
    g.addNode(start);
    g.addNode(end);
    ids[0] = start;
    ids[1] = end;
    for (int i = 2; i < nodes; i++) {
      StaticDijkstraTest.Heavy u = new StaticDijkstraTest.Heavy(UUID.randomUUID(), "a".repeat(heavySize));
      ids[i] = u;
      g.addNode(u);
    }
    Random random = new Random(1920648153);
    for (int i = 0; i < edges; i++) {
      StaticDijkstraTest.Heavy a = ids[random.nextInt(nodes)];
      StaticDijkstraTest.Heavy b = ids[random.nextInt(nodes)];
      if (a.equals(b)) continue;
      g.putEdgeValue(a, b, 1.);
    }

    long startTime = System.currentTimeMillis();
    PathSolver<StaticDijkstraTest.Heavy, Double> d = solver();
    d.setGraph(g);
    System.out.println(System.currentTimeMillis() - startTime);
    startTime = System.currentTimeMillis();
    d.solvePath(start, end);
    System.out.println(System.currentTimeMillis() - startTime);
  }
}
