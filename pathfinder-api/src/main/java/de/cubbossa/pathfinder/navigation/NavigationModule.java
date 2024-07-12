package de.cubbossa.pathfinder.navigation;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The core element to manage path visualizations on an existing graph.
 *
 * @param <PlayerT> The environment implementation that is wrapped by the {@link PathPlayer} class.
 */
public interface NavigationModule<PlayerT> extends Disposable {

  static <PlayerT> NavigationModule<PlayerT> get() {
    return NavigationModuleProvider.get();
  }

  /**
   * Adds a filter that maps the graph nodes on new nodes. Only nodes that are contained in the node collection
   * after the appliance of all filters can be used to get to the target location
   *
   * @param filter The filter function that maps the node collection into the filtered node collection.
   */
  void registerNavigationConstraint(NavigationConstraint filter);

  /**
   * Checks, if a player can cross a node after applying all registered filters.
   *
   * @param uuid  The uuid of the user to check the node for.
   * @param node  The node to check.
   * @param scope The original graph without the appliance of any filters.
   * @return true, if the node can be crossed by the user.
   */
  boolean canNavigateTo(UUID uuid, Node node, Collection<Node> scope);

  /**
   * Filters a collection of nodes with all existing node filters. The result represents all nodes that the player
   * can pass while navigation to a target location.
   *
   * @param player The user to run the filter checks for.
   * @param nodes  The scope nodes that are being filtered by all registered predicates.
   * @return The filtered collection of nodes.
   */
  Collection<Node> applyNavigationConstraints(UUID player, Collection<Node> nodes);

  /**
   * Finds the currently active path for a player or null if none is active.
   *
   * @param player The player to search active paths for.
   * @return
   */
  @Nullable Navigation<PlayerT> getActiveFindCommandPath(final @NotNull PathPlayer<PlayerT> player);

  void setFindCommandPath(Navigation<PlayerT> navigation);

  Navigation<PlayerT> navigate(PathPlayer<PlayerT> viewer, Route route);
}
