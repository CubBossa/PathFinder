package de.cubbossa.pathfinder.core.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

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

		dijkstra = new SimpleDijkstra<>(graph);
		dijkstra.setStartNode("a");
	}

	@Test
	void shortestPath1() {
		Assertions.assertEquals(List.of("a", "b", "e", "f"), dijkstra.shortestPath("f"));
	}

	@Test
	void shortestPath2() {
		Assertions.assertEquals(List.of(), dijkstra.shortestPath("g"));
	}

	@Test
	void shortestPathSelf() {
		Assertions.assertEquals(List.of("a"), dijkstra.shortestPath("a"));
	}

	@Test
	void shortestPathAny() {
		Assertions.assertEquals(List.of("a", "b", "c"), dijkstra.shortestPathToAny("c", "f"));
	}
}