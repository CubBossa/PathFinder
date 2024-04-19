package de.cubbossa.pathfinder.misc;

import com.google.common.graph.MutableValueGraph;

/**
 * An interface to inject Nodes into a graph. The solver will potentially create new nodes and edges in a copy of the graph.
 * The interface splits into two methods to handle the insertion as entry or exit nodes. This separation is only
 * then necessary if the graph is directed. Otherwise, use just one of the two methods.
 *
 * @param <NodeT> The Node object type.
 */
public interface GraphEntrySolver<NodeT> {

  /**
   * Inject a node as entry and exit node. If your graph is undirected, don't call this method.
   * @param node The node to inject.
   * @param scope The graph to inject the node into. This graph might be be modified directly. You might want to
   *              create a copy if you do not want modification of the original graph.
   * @return The modified graph.
   * @throws GraphEntryNotEstablishedException If the solver was not able to inject a node.
   */
  default MutableValueGraph<NodeT, Double> solve(NodeT node, MutableValueGraph<NodeT, Double> scope)
      throws GraphEntryNotEstablishedException {
    scope = solveEntry(node, scope);
    return solveExit(node, scope);
  }

  /**
   * Inject a node as entry node. If edges need to be created, they might be directed towards the graph.
   * @param in The node to inject.
   * @param scope The graph to inject the node into. This graph might be be modified directly. You might want to
   *              create a copy if you do not want modification of the original graph.
   * @return The modified graph.
   * @throws GraphEntryNotEstablishedException If the solver was not able to inject a node.
   */
  MutableValueGraph<NodeT, Double> solveEntry(NodeT in, MutableValueGraph<NodeT, Double> scope)
      throws GraphEntryNotEstablishedException;

  /**
   * Inject a node as exit node. If edges need to be created, they might be directed towards the node.
   * @param out The node to inject.
   * @param scope The graph to inject the node into. This graph might be be modified directly. You might want to
   *              create a copy if you do not want modification of the original graph.
   * @return The modified graph.
   * @throws GraphEntryNotEstablishedException If the solver was not able to inject a node.
   */
  MutableValueGraph<NodeT, Double> solveExit(NodeT out, MutableValueGraph<NodeT, Double> scope)
      throws GraphEntryNotEstablishedException;
}
