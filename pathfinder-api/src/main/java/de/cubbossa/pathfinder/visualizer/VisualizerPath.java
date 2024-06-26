package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.misc.PathPlayer;

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
public interface VisualizerPath<PlayerT> extends PathView<PlayerT> {

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

  void startUpdater(int interval);

  void stopUpdater();
}
