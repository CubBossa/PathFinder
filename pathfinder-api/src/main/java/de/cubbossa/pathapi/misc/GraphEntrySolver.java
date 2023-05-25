package de.cubbossa.pathapi.misc;

import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.graph.Graph;

public interface GraphEntrySolver<T> {

  Graph<T> solve(Node start, Graph<T> scope);
}
