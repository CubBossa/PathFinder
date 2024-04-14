package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.ValueGraph;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolverResult;
import de.cubbossa.pathfinder.node.Node;
import java.util.Collection;
import java.util.List;

public interface Route {

  static Route from(NavigationLocation location) {
    return new AbstractRoute(location);
  }

  static Route from(Node location) {
    if (location instanceof NavigationLocation nav) {
      return from(nav);
    }
    return from(NavigationLocation.fixedGraphNode(location));
  }

  static Route from(Route route) {
    return new AbstractRoute(route);
  }

  NavigationLocation getStart();

  Collection<NavigationLocation> getEnd();

  Route to(Route route);

  Route to(List<Node> nodes);

  Route to(Node node);

  Route to(NavigationLocation location);

  Route toAny(Node... nodes);

  Route toAny(Collection<Node> nodes);

  Route toAny(NavigationLocation... locations);

  Route toAny(String searchString);

  Route toAny(Route... other);

  PathSolverResult<Node, Double> calculatePath(ValueGraph<Node, Double> environment) throws NoPathFoundException;

  List<PathSolverResult<Node, Double>> calculatePaths(ValueGraph<Node, Double> environment) throws NoPathFoundException;
}
