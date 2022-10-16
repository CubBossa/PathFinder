package de.cubbossa.pathfinder.core.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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
	void merge() {
		a.merge(b);
		SimpleDijkstra<String> dijkstra = new SimpleDijkstra<>(a);
		dijkstra.setStartNode("a");
		Assertions.assertEquals(List.of("a", "b", "c"), dijkstra.shortestPath("c"));
	}
}