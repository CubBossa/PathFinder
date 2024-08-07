package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.ValueGraph;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.PathSolverResult;
import de.cubbossa.pathfinder.graph.PathSolverResultImpl;
import de.cubbossa.pathfinder.node.Node;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
class SingletonRoute implements Route {

  private final NavigationLocation location;

  @Override
  public @NotNull NavigationLocation getStart() {
    return location;
  }

  @Override
  public @NotNull Collection<NavigationLocation> getEnd() {
    return Collections.singleton(location);
  }

  @Override
  public @NotNull Route withPathSolver(@NotNull PathSolver<Node, Double> solver) {
    throw new UnsupportedOperationException("Cannot set solver for SingletonRoute.");
  }

  public @NotNull Route withEntrySolver(@NotNull GraphEntrySolver<Node> solver) {
    throw new UnsupportedOperationException("Cannot set solver for SingletonRoute.");
  }

  @Override
  public @NotNull Route to(@NotNull Route route) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public @NotNull Route to(@NotNull List<Node> nodes) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public @NotNull Route to(@NotNull Node node) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public @NotNull Route to(@NotNull NavigationLocation location) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public @NotNull Route toAny(@NotNull Node... nodes) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public @NotNull Route toAny(@NotNull Collection<Node> nodes) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public @NotNull Route toAny(@NotNull NavigationLocation... locations) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public @NotNull Route toAny(@NotNull Route... routes) {
    throw new UnsupportedOperationException("Cannot modify immutable SingletonRoute.");
  }

  @Override
  public PathSolverResult<Node, Double> calculatePath(@NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException {

    Node graphRepresentation = environment.nodes().stream()
        .filter(node -> node.getNodeId().equals(location.getNode().getNodeId()))
        .findAny().orElse(location.getNode());

    return new PathSolverResultImpl<>(Collections.singletonList(graphRepresentation), Collections.emptyList(), 0);
  }

  @Override
  public List<PathSolverResult<Node, Double>> calculatePaths(@NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException {
    return List.of(calculatePath(environment));
  }

  @Override
  public String toString() {
    return "SingletonRoute{loc=" + location + "}";
  }
}
