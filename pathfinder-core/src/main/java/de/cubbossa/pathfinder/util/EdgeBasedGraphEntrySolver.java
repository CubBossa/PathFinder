package de.cubbossa.pathfinder.util;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
import de.cubbossa.pathfinder.misc.Vector;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class EdgeBasedGraphEntrySolver implements GraphEntrySolver<Node> {

  @Override
  public MutableValueGraph<Node, Double> solve(Node node, MutableValueGraph<Node, Double> scope)
      throws GraphEntryNotEstablishedException {
    return solve(node, true, true, scope);
  }

  @Override
  public MutableValueGraph<Node, Double> solveEntry(Node in, MutableValueGraph<Node, Double> scope)
      throws GraphEntryNotEstablishedException {
    return solve(in, true, false, scope);
  }

  @Override
  public MutableValueGraph<Node, Double> solveExit(Node out, MutableValueGraph<Node, Double> scope)
      throws GraphEntryNotEstablishedException {
    return solve(out, false, true, scope);
  }

  private MutableValueGraph<Node, Double> solve(Node node, boolean entry, boolean exit, MutableValueGraph<Node, Double> scope)
      throws GraphEntryNotEstablishedException {

    if (scope.nodes().contains(node)) {
      return scope;
    }

    List<WeightedEdge> sortedEdges = new LinkedList<>();
    scope.edges().forEach((e) -> {
      double d = VectorUtils.distancePointToSegment(node.getLocation().asVector(), e.nodeU().getLocation().asVector(),
          e.nodeV().getLocation().asVector());
      sortedEdges.add(new WeightedEdge(e.nodeU(), e.nodeV(), d));
    });
    Collections.sort(sortedEdges);
    if (sortedEdges.isEmpty()) {
      return scope;
    }
    WeightedEdge first = null;
    Collection<WeightedEdge> result = new HashSet<>();
    // Edges are already sorted, but we want to fetch all edges of similar distance
    for (WeightedEdge edge : sortedEdges) {
      if (first == null) {
        first = edge;
        result.add(first);
        continue;
      }
      if (Math.abs(edge.weight - first.weight) > .01) {
        break;
      }
      result.add(edge);
    }

    // make copy of graph
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.from(scope).build();
    scope.nodes().forEach(graph::addNode);
    scope.edges().forEach(e -> graph.putEdgeValue(e, scope.edgeValue(e).orElseThrow()));

    // Add node via split and extrude
    result.forEach(edge -> {
      Node inject = NodeGraphUtil.mergeGroupedNodes(edge.start, edge.end, new Waypoint(UUID.randomUUID()));

      Vector closest = VectorUtils.closestPointOnSegment(node.getLocation(), edge.start.getLocation(), edge.end.getLocation());
      inject.setLocation(closest.toLocation(edge.start.getLocation().getWorld()));

      ValueGraph<Node, Double> g = graph;
      g = NodeGraphUtil.split(g, edge.start, edge.end, inject);
      NodeGraphUtil.extrude(g, inject, node, entry ? 0d : null, exit ? 0d : null);
    });
    return graph;
  }

  private record WeightedEdge(Node start, Node end, double weight)
      implements Comparable<WeightedEdge> {

    @Override
    public boolean equals(Object obj) {
      return obj instanceof WeightedEdge w && compareTo(w) == 0;
    }

    @Override
    public int compareTo(@NotNull EdgeBasedGraphEntrySolver.WeightedEdge o) {
      return Double.compare(this.weight, o.weight);
    }
  }
}
