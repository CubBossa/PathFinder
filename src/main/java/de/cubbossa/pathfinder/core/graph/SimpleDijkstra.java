package de.cubbossa.pathfinder.core.graph;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Target;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleDijkstra<N> {

	@Getter
	@RequiredArgsConstructor
	private class Node {
		private final N node;
		private boolean settled = false;
		private List<Node> path = new LinkedList<>();
		private final Map<Node, Double> adjacent = new HashMap<>();
		private double distance = Integer.MAX_VALUE;

		public Node(N node, double distance) {
			this.node = node;
			this.distance = distance;
		}

		@Override
		public int hashCode() {
			return node.hashCode();
		}
	}

	private final Graph<N> graph;
	private final Map<N, Node> computedGraph = new HashMap<>();

	public SimpleDijkstra(Graph<N> graph) {
		this.graph = graph;
	}

	public Node buildGraph(N source) {
		Map<N, Node> computed = new HashMap<>();
		Set<N> settled = new HashSet<>();
		Queue<Node> queue = new LinkedList<>();

		Node root = new Node(source, 0);
		queue.add(root);

		while (!queue.isEmpty()) {
			Node current = queue.poll();
			computed.put(current.node, current);
			for (var entry : graph.getEdges(current.node).entrySet()) {
				Node adjacent = computed.computeIfAbsent(entry.getKey(), Node::new);
				if (!adjacent.settled) {
					queue.add(adjacent);
				}
				current.adjacent.put(adjacent, entry.getValue());
			}
			current.settled = true;
		}

		computed.values().forEach(node -> node.settled = false);
		return root;
	}

	public void calculate(N source) {
		Set<Node> unsettled = new HashSet<>();
		unsettled.add(buildGraph(source));

		while (unsettled.size() > 0) {
			Node current = getNearest(unsettled);
			unsettled.remove(current);

			for (Map.Entry<Node, Double> entry : current.adjacent.entrySet()) {
				Node node = entry.getKey();
				if (!node.settled) {
					setMinDist(current, node, entry.getValue());
					unsettled.add(node);
				}
			}
			current.settled = true;
			computedGraph.put(current.node, current);
		}
	}

	private Node getNearest(Set<Node> unsettled) {
		Node nearest = null;
		double dist = Integer.MAX_VALUE;
		for (Node node : unsettled) {
			if (node.distance < dist) {
				nearest = node;
				dist = node.distance;
			}
		}
		return nearest;
	}

	private void setMinDist(Node node, Node neighbour, double edgeWeigh) {
		double sourceDistance = node.distance;
		if (sourceDistance + edgeWeigh < neighbour.distance) {
			neighbour.distance = sourceDistance + edgeWeigh;
			LinkedList<Node> shortestPath = new LinkedList<>(node.path);
			shortestPath.add(node);
			neighbour.path = shortestPath;
		}
	}

	public List<N> shortestPath(N target) {
		if (!computedGraph.containsKey(target)) {
			return new LinkedList<>();
		}
		List<N> result = computedGraph.get(target).path.stream().map(Node::getNode).collect(Collectors.toCollection(LinkedList::new));
		result.add(target);
		return result;
	}

	public List<N> shortestPathToAny(N... targets) {
		return shortestPathToAny(Lists.newArrayList(targets));
	}

	public List<N> shortestPathToAny(Collection<N> targets) {
		if (targets.size() == 0) {
			throw new IllegalArgumentException("Targets must contain at least 2 elements");
		}
		Collection<N> filtered = targets.stream()
				.filter(computedGraph::containsKey)
				.collect(Collectors.toCollection(HashSet::new));
		Node nearest = filtered.stream()
				.map(n -> computedGraph.get(n))
				.min(Comparator.comparingDouble(Node::getDistance))
				.orElse(null);
		if(nearest == null) {
			return new LinkedList<>();
		}
		List<N> path = nearest.path.stream()
				.map(Node::getNode)
				.collect(Collectors.toCollection(LinkedList::new));
		path.add(nearest.node);
		return path;
	}
}
