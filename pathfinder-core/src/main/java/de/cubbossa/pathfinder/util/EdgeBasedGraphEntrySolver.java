package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.GraphEntrySolver;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathfinder.graph.Graph;

import java.util.*;

public class EdgeBasedGraphEntrySolver implements GraphEntrySolver<GroupedNode> {

  public Graph<GroupedNode> solve(GroupedNode start, Graph<GroupedNode> scope) {

    Set<WeightedEdge> sortedEdges = new TreeSet<>(Comparator.comparingDouble(WeightedEdge::weight));
    for (GroupedNode node : scope) {
      scope.getEdges(node).forEach((end, weight) -> {
        double d = VectorUtils.distancePointToSegment(start.node().getLocation().asVector(), node.node().getLocation().asVector(), end.node().getLocation().asVector());
        sortedEdges.add(new WeightedEdge(node, end, d * weight));
      });
    }
    if (sortedEdges.size() == 0) {
      return scope;
    }
    WeightedEdge first = null;
    Collection<WeightedEdge> result = new HashSet<>();
    for (WeightedEdge edge : sortedEdges) {
      if (first == null) {
        first = edge;
        result.add(first);
        continue;
      }
      if (Math.abs(edge.weight - first.weight) > .0001) {
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

  private record WeightedEdge(GroupedNode start, GroupedNode end, double weight) {
  }
}
