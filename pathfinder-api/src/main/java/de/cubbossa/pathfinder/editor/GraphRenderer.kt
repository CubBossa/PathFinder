package de.cubbossa.pathfinder.editor;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Renders a set of nodes and connecting edges.
 *
 * @param <Player> Defines the type of class that is wrapped within the {@link PathPlayer} object.
 */
public interface GraphRenderer<Player> extends Disposable {

  /**
   * Clear all existing node renderings.
   *
   * @param player The player to clear the graph for.
   * @return A completable future that is complete once all rendering is cleared.
   */
  CompletableFuture<Void> clear(PathPlayer<Player> player);

  /**
   * Render a scope of nodes with their edges and group information.
   * This will render on top of the already rendered nodes, so to remove a certain node from screen
   * you would either {@link this#eraseNodes(PathPlayer, Collection)} or {@link this#clear(PathPlayer)} and re-render the whole scope. The function also renders edges for each node.
   *
   * @param player The player to display the graph for.
   * @param nodes  A set of nodes to render.
   * @return A completable future that is complete once all nodes are rendered.
   */
  CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes);

  /**
   * Remove a scope of nodes with their edges and group information from view.
   *
   * @param player The player to display the graph for.
   * @param nodes  A set of nodes to remove from view.
   * @return A completable future that is complete once all nodes are erased.
   */
  CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes);
}
