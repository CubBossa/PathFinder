package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.GraphEntrySolver;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.node.implementation.Waypoint;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EdgeBasedGraphEntrySolver implements GraphEntrySolver<Node> {

  public Graph<Node> solve(Node start, Graph<Node> scope) {

    Set<WeightedEdge> sortedEdges = new TreeSet<>(Comparator.comparingDouble(WeightedEdge::weight));
    Collection<CompletableFuture<Void>> futures = new HashSet<>();
    for (Node node : scope) {
      for (Edge edge : node.getEdges()) {
        futures.add(edge.resolveEnd().thenAccept(end -> {
          double d = VectorUtils.distancePointToSegment(start.getLocation().asVector(), node.getLocation().asVector(), end.getLocation().asVector());
          sortedEdges.add(new WeightedEdge(edge, node, end, d));
        }));
      }
    }
    if (sortedEdges.size() == 0) {
      return scope;
    }
    futures.stream().parallel().forEach(CompletableFuture::join);
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
      Waypoint w = (Waypoint) edge.start.clone(UUID.randomUUID());
      Vector closest = VectorUtils.closestPointOnSegment(start.getLocation(), edge.start.getLocation(), edge.end.getLocation());
      w.setLocation(closest.toLocation(edge.start.getLocation().getWorld()));

      scope.subdivide(edge.start, edge.end, () -> w);
      scope.connect(start, w);
    });
    return scope;
  }

  private record WeightedEdge(Edge edge, Node start, Node end, double weight) {
  }
}
