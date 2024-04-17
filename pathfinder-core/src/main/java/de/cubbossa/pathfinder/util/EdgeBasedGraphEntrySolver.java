package de.cubbossa.pathfinder.util;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.misc.GraphEntrySolver;
import de.cubbossa.pathfinder.misc.Vector;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.GroupedNodeImpl;
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
  public MutableValueGraph<Node, Double> solveEntry(Node in, MutableValueGraph<Node, Double> scope) {
    return solve(in, true, scope);
  }

  @Override
  public MutableValueGraph<Node, Double> solveExit(Node out, MutableValueGraph<Node, Double> scope) {
    return solve(out, false, scope);
  }

  private MutableValueGraph<Node, Double> solve(Node start, boolean entry, MutableValueGraph<Node, Double> scope) {

    List<WeightedEdge> sortedEdges = new LinkedList<>();
    scope.edges().forEach((e) -> {
      double d = VectorUtils.distancePointToSegment(start.getLocation().asVector(), e.nodeU().getLocation().asVector(), e.nodeV().getLocation().asVector());
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
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed()
        .build();
    scope.nodes().forEach(graph::addNode);
    scope.edges().forEach(e -> graph.putEdgeValue(e, scope.edgeValue(e).orElseThrow()));

    result.forEach(edge -> {
      Node w;
      if (edge.start instanceof GroupedNode startGrouped && edge.end instanceof GroupedNode endGrouped) {
        w = startGrouped.merge(endGrouped);
      } else {
        if (edge.start instanceof GroupedNode startGrouped) {
          w = new GroupedNodeImpl(new Waypoint(UUID.randomUUID()), startGrouped.groups());
        } else if (edge.end instanceof GroupedNode endGrouped) {
          w = new GroupedNodeImpl(new Waypoint(UUID.randomUUID()), endGrouped.groups());
        } else {
          w = new Waypoint(UUID.randomUUID());
        }
      }
      Vector closest = VectorUtils.closestPointOnSegment(start.getLocation(), edge.start.getLocation(), edge.end.getLocation());
      w.setLocation(closest.toLocation(edge.start.getLocation().getWorld()));

      graph.removeEdge(edge.start, edge.end);
      graph.addNode(w);
      graph.putEdgeValue(edge.start, w, edge.start.getLocation().distance(w.getLocation()));
      graph.putEdgeValue(w, edge.end, edge.end.getLocation().distance(w.getLocation()));
      if (entry) {
        graph.putEdgeValue(start, w, 0.);
      } else {
        graph.putEdgeValue(w, start, 0.);
      }
    });
    return graph;
  }

  private record WeightedEdge(Node start, Node end,
                              double weight) implements Comparable<WeightedEdge> {

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
