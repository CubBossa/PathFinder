package de.cubbossa.pathfinder.graph;

import java.util.function.Function;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamicDijkstraTest extends ShortestPathTest {
  @Override
  PathSolver<String, Double> solver() {
    return new DynamicDijkstra<>(Function.identity());
  }
}