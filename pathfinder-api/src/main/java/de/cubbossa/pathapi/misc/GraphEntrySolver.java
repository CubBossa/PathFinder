package de.cubbossa.pathapi.misc;

import com.google.common.graph.MutableValueGraph;
import de.cubbossa.pathapi.node.GroupedNode;

public interface GraphEntrySolver<T> {

  MutableValueGraph<GroupedNode, Double> solve(T start, MutableValueGraph<GroupedNode, Double> scope);
}
