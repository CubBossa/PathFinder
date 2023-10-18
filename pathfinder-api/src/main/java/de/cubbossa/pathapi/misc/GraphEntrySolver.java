package de.cubbossa.pathapi.misc;

import com.google.common.graph.MutableValueGraph;
import de.cubbossa.pathapi.node.GroupedNode;

public interface GraphEntrySolver<T> {

    MutableValueGraph<GroupedNode, Double> solveEntry(T in, MutableValueGraph<GroupedNode, Double> scope);

    MutableValueGraph<GroupedNode, Double> solveExit(T out, MutableValueGraph<GroupedNode, Double> scope);
}
