package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.GraphEntrySolver;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathfinder.graph.Graph;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EdgeBasedGraphEntrySolver implements GraphEntrySolver<GroupedNode> {

  public Graph<GroupedNode> solve(GroupedNode start, Graph<GroupedNode> scope) {

    List<WeightedEdge> sortedEdges = new LinkedList<>();
    scope.getEdgeMap().forEach((node, edgeMap) -> {
      edgeMap.forEach((end, unused) -> {
        double d = VectorUtils.distancePointToSegment(start.node().getLocation().asVector(), node.node().getLocation().asVector(), end.node().getLocation().asVector());
        sortedEdges.add(new WeightedEdge(node, end, d));
      });
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
    result.forEach(edge -> {
      GroupedNode w = edge.start.merge(edge.end);
      Vector closest = VectorUtils.closestPointOnSegment(start.node().getLocation(), edge.start.node().getLocation(), edge.end.node().getLocation());
      w.node().setLocation(closest.toLocation(edge.start.node().getLocation().getWorld()));

      scope.subdivide(edge.start, edge.end, () -> w);
      scope.connect(start, w);
    });
    return scope;
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
