package de.cubbossa.pathapi.navigation;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

/**
 * The core element to manage path visualizations on an existing graph.
 *
 * @param <PlayerT> The environment implementation that is wrapped by the {@link PathPlayer} class.
 */
public interface NavigationHandler<PlayerT> {

  /**
   * Adds a filter that maps the graph nodes on new nodes. Only nodes that are contained in the node collection
   * after the appliance of all filters can be used to get to the target location
   *
   * @param filter The filter function that maps the node collection into the filtered node collection.
   */
  void registerFindPredicate(Function<NavigationRequestContext, Collection<Node>> filter);

  /**
   * Checks, if a player can cross a node after applying all registered filters.
   *
   * @param uuid The uuid of the user to check the node for.
   * @param node The node to check.
   * @param scope The original graph without the appliance of any filters.
   * @return true, if the node can be crossed by the user.
   */
  boolean canFind(UUID uuid, Node node, Collection<Node> scope);

  /**
   * Filters a collection of nodes with all existing node filters. The result represents all nodes that the player
   * can pass while navigation to a target location.
   *
   * @param player The user to run the filter checks for.
   * @param nodes The scope nodes that are being filtered by all registered predicates.
   * @return The filtered collection of nodes.
   */
  Collection<Node> filterFindables(UUID player, Collection<Node> nodes);

  /**
   * Finds the potentially existing {@link SearchInfo}, which resembles an active path view.
   * @param player The player to search active paths for.
   * @return
   */
  @Deprecated
  @Nullable SearchInfo<PlayerT> getActivePath(PathPlayer<PlayerT> player);


  CompletableFuture<NavigateResult> findPathToLocation(PathPlayer<PlayerT> player, Location target);

  CompletableFuture<NavigateResult> findPathToNodes(PathPlayer<PlayerT> player, Collection<Node> targets);

  CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, Collection<NavigateLocation> target);

  CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, Collection<NavigateLocation> target,
                                             double maxDist);

  CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, NavigateLocation start,
                                             Collection<NavigateLocation> target);

  CompletableFuture<NavigateResult> findPath(PathPlayer<PlayerT> viewer, NavigateLocation start,
                                             Collection<NavigateLocation> target, double maxDist);


  void unsetPath(PathPlayer<PlayerT> playerId);

  void unsetPath(SearchInfo<PlayerT> info);

  @Deprecated
  void cancelPath(PathPlayer<PlayerT> playerId);

  @Deprecated
  void cancelPath(SearchInfo<PlayerT> info);

  @Deprecated
  void reachTarget(SearchInfo<PlayerT> info);


  enum NavigateResult {
    SUCCESS, FAIL_BLOCKED, FAIL_EMPTY, FAIL_EVENT_CANCELLED,
    FAIL_TOO_FAR_AWAY, FAIL_UNKNOWN;
  }

  record SearchInfo<PlayerT2>(PathPlayer<PlayerT2> player, VisualizerPath<PlayerT2> path, Location target, float distance) {
  }

  record NavigationRequestContext(UUID playerId, Collection<Node> nodes) {
  }

  /**
   * A navigation target represents any possible and possibly moving node in the navigation graph.
   * It is used to define a path starting point and end point on a graph.
   */
  interface NavigateLocation {
    Node getNode();
    boolean isAgile();
    void setAgile(boolean agile);
  }
}
