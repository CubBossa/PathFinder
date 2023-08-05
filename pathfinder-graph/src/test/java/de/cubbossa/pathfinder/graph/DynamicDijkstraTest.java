package de.cubbossa.pathfinder.graph;

import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamicDijkstraTest extends ShortestPathTest {
  @Override
  PathSolver<String> solver() {
    return new DynamicDijkstra<>();
  }
}