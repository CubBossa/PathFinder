package de.cubbossa.pathfinder.graph;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class GraphUtils {

  public static <N, V> MutableValueGraph<N, V> mutable(ValueGraph<N, V> graph) {
    if (graph instanceof MutableValueGraph<N, V> mGraph) {
      return mGraph;
    }
    MutableValueGraph<N, V> g = ValueGraphBuilder.from(graph).build();
    graph.nodes().forEach(g::addNode);
    for (EndpointPair<N> e : graph.edges()) {
      g.edgeValue(e.nodeU(), e.nodeV()).ifPresent(v -> g.putEdgeValue(e.nodeU(), e.nodeV(), v));
    }
    return g;
  }

  public static <N, V> ValueGraph<N, V> merge(Iterable<? extends ValueGraph<N, V>> islands) {
    var iterator = islands.iterator();
    MutableValueGraph<N, V> g = mutable(iterator.next());
    while (iterator.hasNext()) {
      var other = iterator.next();
      for (N node : other.nodes()) {
        g.addNode(node);
      }
      for (EndpointPair<N> edge : other.edges()) {
        V val = other.edgeValue(edge.nodeU(), edge.nodeV()).orElseThrow();
        g.putEdgeValue(edge.nodeU(), edge.nodeV(), val);
      }
    }
    return g;
  }

  public static <N, V> ValueGraph<N, V> merge(ValueGraph<N, V> a, ValueGraph<N, V> b) {
    MutableValueGraph<N, V> ma = mutable(a);
    for (N node : b.nodes()) {
      ma.addNode(node);
    }
    for (EndpointPair<N> edge : b.edges()) {
      V val = b.edgeValue(edge.nodeU(), edge.nodeV()).orElseThrow();
      ma.putEdgeValue(edge.nodeU(), edge.nodeV(), val);
    }
    return ma;
  }

  public static <N, V> Collection<ValueGraph<N, V>> islands(ValueGraph<N, V> graph) {
    Collection<ValueGraph<N, V>> results = new ArrayList<>();
    Collection<N> graphNodes = new HashSet<>(graph.nodes());

    while (!graphNodes.isEmpty()) {
      N startNode = graphNodes.stream().findAny().get();
      graphNodes.remove(startNode);

      HashSet<N> islandNodes = new HashSet<>();
      Queue<N> queue = new LinkedList<>();
      queue.add(startNode);
      while (!queue.isEmpty()) {
        var n = queue.poll();
        islandNodes.add(n);
        graphNodes.remove(n);
        var pre = graph.predecessors(n);
        var suc = graph.successors(n);
        for (N p : pre) {
          if (graphNodes.contains(p)) {
            queue.add(p);
          }
        }
        for (N s : suc) {
          if (graphNodes.contains(s)) {
            queue.add(s);
          }
        }
      }
      results.add(Graphs.inducedSubgraph(graph, islandNodes));
    }
    return results;
  }
}
