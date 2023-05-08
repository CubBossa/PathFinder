package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import java.util.Collection;
import java.util.List;

/**
 * A VisualizerPath resembles one visualization across a path of nodes.
 * Each node will be resolved to all its applied visualizers and all visualizers render as smaller sub paths.
 * <p/>
 * If, for example, a path consists of three nodes a, b and c and a and b have visualizer X applied,
 * while b and c have visualizer Y applied, there must be a sub path rendering for visualizer X(a, b)
 * and Y(b, c).
 * <p/>
 * <pre>
 * X    X
 *      Y    Y
 * a -- b -- c
 * </pre>
 *
 * @param <PlayerT> The type of the player object.
 */
public interface VisualizerPath<PlayerT> extends Collection<Node> {

  /**
   * May run all excessive calculation of visualizers in beforehand to cache the results.
   * It must cancel all currently running paths if called after {@link #run(PathPlayer)}.
   * The
   *
   * @param path   A list of nodes that resembles the path to display. Nodes must not be connected by an edge to be
   *               displayed with a visualizer path.
   * @param player The player attribute will be passed to the {@link PathVisualizer#prepare(List, PathPlayer)} method and therefore,
   *               all renderings of this VisualizerPath instance are made for the specified player.
   */
  void prepare(List<Node> path, PathPlayer<PlayerT> player);

  /**
   * Starts all repeating tasks and activates the path.
   * After this method has been called and as long as {@link #cancel(PathPlayer)} has not been called afterward,
   * {@link #isActive()} must return true.
   */
  void run(PathPlayer<PlayerT> player);

  /**
   * Cancels the current path rendering.
   * {@link #isActive()} must return false after calling this method.
   * The path can be reactivated by calling {@link #run(PathPlayer)}, therefore, caches should not be invalidated.
   *
   * @param player The player to cancel the current path for. If this path is not being rendered for the given player,
   *               nothing will happen.
   */
  void cancel(PathPlayer<PlayerT> player);

  /**
   * Check if this path is rendered for any player.
   *
   * @return True as long as {@link #isActive(PathPlayer)} is true for any player.
   */
  boolean isActive();

  /**
   * Check if this path is being rendered for the given player.
   *
   * @param player The player to check for.
   * @return True, if the path is currently being rendered for the given player.
   */
  boolean isActive(PathPlayer<PlayerT> player);

  /**
   * The rendering target is the player that has been used to generate all caches.
   * It is being set when calling {@link #prepare(List, PathPlayer)}.
   *
   * @return The player that has been used to generate visualizer caches for. {@code null} if the path has not been prepared.
   */
  PathPlayer<PlayerT> getRenderingTarget();

  /**
   * All players that are currently viewing this path. A player views a path if {@link #run(PathPlayer)} has been called without
   * {@link #cancel(PathPlayer)} afterward.
   *
   * @return All players that are currently viewing this path. This does not necessarily contain {@link #getRenderingTarget()},
   * because the rendering target only describes the player that was used to generate caches.
   */
  Collection<PathPlayer<PlayerT>> getViewers();
}
