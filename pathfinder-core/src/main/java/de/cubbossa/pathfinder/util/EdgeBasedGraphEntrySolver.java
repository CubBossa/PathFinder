package de.cubbossa.pathfinder.util;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathapi.misc.GraphEntrySolver;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.GroupedNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EdgeBasedGraphEntrySolver implements GraphEntrySolver<GroupedNode> {

  @Override
  public MutableValueGraph<GroupedNode, Double> solveEntry(GroupedNode in, MutableValueGraph<GroupedNode, Double> scope) {
    return solve(in, true, scope);
  }

  @Override
  public MutableValueGraph<GroupedNode, Double> solveExit(GroupedNode out, MutableValueGraph<GroupedNode, Double> scope) {
    return solve(out, false, scope);
  }

  private MutableValueGraph<GroupedNode, Double> solve(GroupedNode start, boolean entry, MutableValueGraph<GroupedNode, Double> scope) {

    List<WeightedEdge> sortedEdges = new LinkedList<>();
    scope.edges().forEach((e) -> {
      double d = VectorUtils.distancePointToSegment(start.node().getLocation().asVector(), e.nodeU().node().getLocation().asVector(), e.nodeV().node().getLocation().asVector());
      sortedEdges.add(new WeightedEdge(e.nodeU(), e.nodeV(), d));
    });
    Collections.sort(sortedEdges);
    if (sortedEdges.size() == 0) {
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
    MutableValueGraph<GroupedNode, Double> graph = ValueGraphBuilder.directed()
        .build();
    scope.nodes().forEach(graph::addNode);
    scope.edges().forEach(e -> graph.putEdgeValue(e, scope.edgeValue(e).orElseThrow()));

    result.forEach(edge -> {
      GroupedNode w = edge.start.merge(edge.end);
      Vector closest = VectorUtils.closestPointOnSegment(start.node().getLocation(), edge.start.node().getLocation(), edge.end.node().getLocation());
      w.node().setLocation(closest.toLocation(edge.start.node().getLocation().getWorld()));

      graph.removeEdge(edge.start, edge.end);
      graph.addNode(w);
      graph.putEdgeValue(edge.start, w, edge.start.node().getLocation().distance(w.node().getLocation()));
      graph.putEdgeValue(w, edge.end, edge.end.node().getLocation().distance(w.node().getLocation()));
      if (entry) {
        graph.putEdgeValue(start, w, 0.);
      } else {
        graph.putEdgeValue(w, start, 0.);
      }
    });
    return graph;
  }

  private record WeightedEdge(GroupedNode start, GroupedNode end, double weight) implements Comparable<WeightedEdge> {

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
