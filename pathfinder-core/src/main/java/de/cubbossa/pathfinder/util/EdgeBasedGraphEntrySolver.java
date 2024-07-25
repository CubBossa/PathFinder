package de.cubbossa.pathfinder.util;

import com.google.common.base.Preconditions;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
import de.cubbossa.pathfinder.graph.GraphUtils;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class EdgeBasedGraphEntrySolver implements GraphEntrySolver<Node> {

  private final Float maxDistance;
  private final double maxDistanceSq;
  private final Float weightFactor;

  public EdgeBasedGraphEntrySolver() {
    this(null, 1f);
  }

  public EdgeBasedGraphEntrySolver(@Nullable Float maxDistance, @Nullable Float weightFactor) {
    this.maxDistance = maxDistance;
    this.maxDistanceSq = maxDistance == null ? Double.MAX_VALUE : Math.pow(maxDistance, 2);
    this.weightFactor = weightFactor == null ? 1f : weightFactor;
  }

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

    Preconditions.checkNotNull(node);
    Preconditions.checkArgument(entry || exit, "Either entry or exit or both must be true.");

    if (scope.nodes().isEmpty()) {
      throw new GraphEntryNotEstablishedException();
    }
    if (scope.nodes().contains(node)) {
      return scope;
    }
    if (scope.edges().isEmpty()) {
      // if no edges but more than one node this cannot be one island!
      if (scope.nodes().size() != 1) {
        throw new IllegalArgumentException("Invalid graph to inject node into.");
      }
      // Its just one node, fetch it by iterator.next
      Node nearest = scope.nodes().iterator().next();
      double distSq = node.getLocation().distanceSquared(nearest.getLocation());
      if (distSq >= maxDistanceSq) {
        throw new GraphEntryNotEstablishedException();
      }
      double dist = Math.sqrt(distSq);
      scope.addNode(node);
      if (entry) {
        scope.putEdgeValue(node, nearest, dist * weightFactor);
      }
      if (exit) {
        scope.putEdgeValue(nearest, node, dist * weightFactor);
      }
      return scope;
    }

    List<WeightedEdge> sortedEdges = new LinkedList<>();
    scope.edges().forEach((e) -> {
      // If a max distance is specified, first check if the edge is valid for distance check
      if (maxDistance != null) {
        float md = maxDistance;
        // if x of both start and end is bigger than x of node, the edge cant be within distance.
        if (e.nodeU().getLocation().getX() - node.getLocation().getX() > md && e.nodeV().getLocation().getX() - node.getLocation().getX() > md
            || node.getLocation().getX() - e.nodeU().getLocation().getX() > md && node.getLocation().getX() - e.nodeV().getLocation().getX() > md
            || e.nodeU().getLocation().getY() - node.getLocation().getY() > md && e.nodeV().getLocation().getY() - node.getLocation().getY() > md
            || node.getLocation().getY() - e.nodeU().getLocation().getY() > md && node.getLocation().getY() - e.nodeV().getLocation().getY() > md
            || e.nodeU().getLocation().getZ() - node.getLocation().getZ() > md && e.nodeV().getLocation().getZ() - node.getLocation().getZ() > md
            || node.getLocation().getZ() - e.nodeU().getLocation().getZ() > md && node.getLocation().getZ() - e.nodeV().getLocation().getZ() > md
        ) {
          // Not close enough, skip this edge without doing expensive maths
          return;
        }
      }
      // find the closest point on this edge to the node and check if its distance is smaller than maxDist
      Vector p = VectorUtils.closestPointOnSegment(node.getLocation().asVector(), e.nodeU().getLocation().asVector(),
          e.nodeV().getLocation().asVector());
      double d = p.distanceSquared(node.getLocation());
      if (maxDistance != null && Math.pow(maxDistance, 2) < d) {
        return;
      }
      sortedEdges.add(new WeightedEdge(e.nodeU(), e.nodeV(), p, d));
    });
    if (sortedEdges.isEmpty()) {
      // None of the edges was close enough to the target point
      throw new GraphEntryNotEstablishedException();
    }
    Collections.sort(sortedEdges);

    WeightedEdge first = null;
    Collection<WeightedEdge> result = new HashSet<>();
    // Edges are already sorted, but we want to fetch all edges of similar distance
    for (WeightedEdge edge : sortedEdges) {
      if (first == null) {
        first = edge;
        result.add(first);
        continue;
      }
      // Not clean since weight is distance squared, but good enough trade for performance
      if (Math.abs(edge.weight - first.weight) > .01) {
        break;
      }
      result.add(edge);
    }

    // make copy of graph
    MutableValueGraph<Node, Double> graph = Graphs.copyOf(scope);

    // Add node via split and extrude
    for (WeightedEdge edge : result) {

      Node inject;
      // We can skip the complex injection part if one of the end nodes is the closest point
      if (edge.closest.distanceSquared(edge.start.getLocation().asVector()) < 0.001f) {
        inject = edge.start;
      } else if (edge.closest.distanceSquared(edge.end.getLocation().asVector()) < 0.001f) {
        inject = edge.end;
      } else {
        inject = NodeGraphUtil.mergeGroupedNodes(edge.start, edge.end, new Waypoint(UUID.randomUUID()));
        inject.setLocation(edge.closest.toLocation(edge.start.getLocation().getWorld()));

        graph = GraphUtils.mutable(NodeGraphUtil.split(graph, edge.start, edge.end, inject));
      }
      graph = GraphUtils.mutable(NodeGraphUtil.extrude(graph, inject, node, entry ? 1d : null, exit ? 1d : null));
    }
    return graph;
  }

  private record WeightedEdge(Node start, Node end, Vector closest, double weight)
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
