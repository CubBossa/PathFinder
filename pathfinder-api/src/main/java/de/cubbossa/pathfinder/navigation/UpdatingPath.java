package de.cubbossa.pathfinder.navigation;

import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.node.Node;
import java.util.List;

public interface UpdatingPath {

  List<Node> getNodes() throws NoPathFoundException;
}
