package de.cubbossa.pathfinder.util;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.GroupedNodeImpl;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class NodeGraphUtil {

  public static Node mergeGroupedNodes(Node a, Node b, Node into) {
    Node result;
    if (a instanceof GroupedNode startGrouped && b instanceof GroupedNode endGrouped) {
      Collection<NodeGroup> groups = new ArrayList<>(startGrouped.groups());
      groups.addAll(endGrouped.groups());
      result = new GroupedNodeImpl(into, groups);
    } else if (a instanceof GroupedNode startGrouped) {
      result = new GroupedNodeImpl(new Waypoint(UUID.randomUUID()), startGrouped.groups());
    } else if (b instanceof GroupedNode endGrouped) {
      result = new GroupedNodeImpl(new Waypoint(UUID.randomUUID()), endGrouped.groups());
    } else {
      result = new Waypoint(UUID.randomUUID());
    }
    return result;
  }

  public static MutableValueGraph<Node, Double> mutable(ValueGraph<Node, Double> graph) {
    if (graph instanceof MutableValueGraph<Node, Double> mGraph) {
      return mGraph;
    }
    MutableValueGraph<Node, Double> g = ValueGraphBuilder.from(graph).build();
    graph.nodes().forEach(g::addNode);
    for (EndpointPair<Node> e : graph.edges()) {
      g.putEdgeValue(e.nodeU(), e.nodeV(), g.edgeValue(e.nodeU(), e.nodeV()).orElse(0d));
    }
    return g;
  }

  public static ValueGraph<Node, Double> split(ValueGraph<Node, Double> graph, Node from, Node to, Node factory) {
    if (!graph.hasEdgeConnecting(from, to)) {
      throw new IllegalArgumentException("From and To values are not connected.");
    }
    MutableValueGraph<Node, Double> g = mutable(graph);
    double oldDistance = g.removeEdge(from, to);
    double distance = from.getLocation().distance(to.getLocation());
    double ratio = oldDistance / distance;

    Node inject = mergeGroupedNodes(from, to, factory);
    g.addNode(inject);
    g.putEdgeValue(from, inject, from.getLocation().distance(factory.getLocation()) * ratio);
    g.putEdgeValue(inject, to, factory.getLocation().distance(to.getLocation()) * ratio);
    return g;
  }

  public static ValueGraph<Node, Double> extrude(ValueGraph<Node, Double> graph, Node base, Node newNode, @Nullable Double entryWeight, @Nullable Double exitWeight) {

    if (!(newNode instanceof GroupedNode) && base instanceof GroupedNode gn) {
      newNode = new GroupedNodeImpl(newNode, gn.groups());
    }

    MutableValueGraph<Node, Double> g = mutable(graph);
    g.addNode(newNode);
    if (!g.nodes().contains(base)) {
      g.addNode(base);
    }
    if (entryWeight == null && exitWeight == null) {
      return g;
    }

    double dist = newNode.getLocation().distance(base.getLocation());
    if (entryWeight != null) {
      g.putEdgeValue(newNode, base, dist * entryWeight);
    }
    if (exitWeight != null) {
      g.putEdgeValue(base, newNode, dist * exitWeight);
    }
    return g;
  }


}
