package de.cubbossa.pathfinder.graph;

import java.util.HashMap;
import java.util.Map;

public class Graph<N> {

  private final Map<N, Map<N, Double>> nodes;

  public Graph() {
    this(16);
  }

  public Graph(int capacity) {
    nodes = new HashMap<>(capacity);
  }

  public void merge(Graph<N> other) {
    for (var entry : other.nodes.entrySet()) {
      if (nodes.containsKey(entry.getKey())) {
        Map<N, Double> combined = new HashMap<>(nodes.get(entry.getKey()));
        combined.putAll(entry.getValue());
        nodes.put(entry.getKey(), combined);
      } else {
        nodes.put(entry.getKey(), entry.getValue());
      }
    }
    nodes.putAll(other.nodes);
  }

  public void addNode(N node) {
    nodes.put(node, new HashMap<>());
  }

  public void removeNode(N node) {
    if (nodes.remove(node) == null) {
      throw new IllegalArgumentException("Node must be in graph.");
    }
    nodes.values().forEach(ns -> {
      ns.remove(node);
    });
  }

  public boolean hasNode(N node) {
    return nodes.containsKey(node);
  }

  public Map<N, Double> getEdges(N node) {
    if (!nodes.containsKey(node)) {
      throw new IllegalArgumentException("Node must be in graph.");
    }
    return new HashMap<>(nodes.get(node));
  }

  public void connect(N start, N end) {
    connect(start, end, 1);
  }

  public void connect(N start, N end, double weight) throws IllegalArgumentException {
    if (!nodes.containsKey(start) || !nodes.containsKey(end)) {
      throw new IllegalArgumentException("Node must be in graph.");
    }
    nodes.get(start).put(end, weight);
  }

  public void disconnect(N start, N end) {
    var map = nodes.get(start);
    if (map == null || !map.containsKey(end)) {
      throw new IllegalArgumentException("Node must be in graph.");
    }
    map.remove(end);
  }

  public void disconnectAllTo(N node) {
    if (!nodes.containsKey(node)) {
      throw new IllegalArgumentException("Node must be in graph.");
    }
    nodes.get(node).clear();
  }

  public void disconnectAllFrom(N node) {
    nodes.values().forEach(e -> e.remove(node));
  }

  public double getEdgeWeight(N start, N end) {
    var map = nodes.get(start);
    if (map == null) {
      throw new IllegalArgumentException("The start node is not contained in this graph.");
    }
    if (map.containsKey(end)) {
      return map.get(end);
    }
    throw new IllegalArgumentException("The end node is not contained in this graph.");
  }
}
