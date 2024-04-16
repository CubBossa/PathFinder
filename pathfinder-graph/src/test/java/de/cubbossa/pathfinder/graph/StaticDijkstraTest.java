package de.cubbossa.pathfinder.graph;

import java.util.function.Function;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StaticDijkstraTest extends ShortestPathTest {
  @Override
  PathSolver<String, Double> solver() {
    return new StaticDijkstra<>(Function.identity());
  }
}