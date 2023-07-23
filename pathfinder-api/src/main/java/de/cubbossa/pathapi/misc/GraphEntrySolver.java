package de.cubbossa.pathapi.misc;

import de.cubbossa.pathfinder.graph.Graph;

public interface GraphEntrySolver<T> {

  Graph<T> solve(T start, Graph<T> scope);
}
