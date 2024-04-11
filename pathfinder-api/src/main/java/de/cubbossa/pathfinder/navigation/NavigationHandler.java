package de.cubbossa.pathfinder.navigation;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.misc.GraphEntrySolver;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.PathView;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The core element to manage path visualizations on an existing graph.
 *
 * @param <PlayerT> The environment implementation that is wrapped by the {@link PathPlayer} class.
 */
public interface NavigationHandler<PlayerT> extends Disposable {

  /**
   * Adds a filter that maps the graph nodes on new nodes. Only nodes that are contained in the node collection
   * after the appliance of all filters can be used to get to the target location
   *
   * @param filter The filter function that maps the node collection into the filtered node collection.
   */
  void registerFindPredicate(NavigationFilter filter);

  /**
   * Checks, if a player can cross a node after applying all registered filters.
   *
   * @param uuid  The uuid of the user to check the node for.
   * @param node  The node to check.
   * @param scope The original graph without the appliance of any filters.
   * @return true, if the node can be crossed by the user.
   */
  boolean canFind(UUID uuid, Node node, Collection<Node> scope);

  /**
   * Filters a collection of nodes with all existing node filters. The result represents all nodes that the player
   * can pass while navigation to a target location.
   *
   * @param player The user to run the filter checks for.
   * @param nodes  The scope nodes that are being filtered by all registered predicates.
   * @return The filtered collection of nodes.
   */
  Collection<Node> filterFindables(UUID player, Collection<Node> nodes);

  /**
   * Finds the potentially existing {@link SearchInfo}, which resembles an active path view.
   *
   * @param player The player to search active paths for.
   * @return
   */
  Collection<VisualizerPath<PlayerT>> getActivePaths(PathPlayer<PlayerT> player);

  CompletableFuture<List<Node>> createPath(Route route) throws NoPathFoundException;

  List<Node> removeIdenticalNeighbours(List<Node> path);

  CompletableFuture<VisualizerPath<PlayerT>> renderPath(
      PathPlayer<PlayerT> viewer, Route route
  ) throws NoPathFoundException;

  <ViewT extends PathView<PlayerT>> CompletableFuture<VisualizerPath<PlayerT>> renderPath(
      PathPlayer<PlayerT> viewer, Route route, PathVisualizer<ViewT, PlayerT> renderer
  ) throws NoPathFoundException;

  void cancelPathWhenTargetReached(VisualizerPath<PlayerT> path);

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  class NavigationConfig {
    private PathSolver<Node, Double> pathSolver;
    private GraphEntrySolver<Node> insertionSolver;
    private double maxInsertionDistance;
  }
}
