package de.cubbossa.pathfinder.misc;

import com.google.common.graph.MutableValueGraph;

public interface GraphEntrySolver<NodeT> {

    MutableValueGraph<NodeT, Double> solveEntry(NodeT in, MutableValueGraph<NodeT, Double> scope);

    MutableValueGraph<NodeT, Double> solveExit(NodeT out, MutableValueGraph<NodeT, Double> scope);
}
