package de.cubbossa.pathapi.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.GroupedNode;
import java.util.List;
import java.util.function.Supplier;

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

  /**
   * Refreshes the path of this visualizer path.
   *
   * @param path A list of grouped nodes that represent a path.
   */
  void update(List<GroupedNode> path);

  void startUpdater(Supplier<List<GroupedNode>> path, int ms);

  void stopUpdater();
}
