package de.cubbossa.pathfinder.editor;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;

/**
 * An editor that handles the rendering and the editing of a graph.
 * The graph is being reduced to a NodeGroup, which might also be the global NodeGroup.
 *
 * @param <Player> The Player object type.
 */
public interface GraphEditor<Player> extends Disposable {

  /**
   * @return The namespaced key of the group that is being edited.
   */
  NamespacedKey getGroupKey();

  /**
   * @return true if there is any active editor at the moment. Otherwise false.
   */
  boolean isEdited();

  /**
   * Toggles the edit mode for a player.
   *
   * @param player The player object to toggle the editor for.
   * @return the new state of editing.
   */
  boolean toggleEditMode(PathPlayer<Player> player);

  /**
   * Cancels the editing session for all players.
   */
  void cancelEditModes();

  /**
   * Sets a player into edit mode for the graph section.
   *
   * @param player   the player to set the edit mode for
   * @param activate activate or deactivate edit mode
   */
  void setEditMode(PathPlayer<Player> player, boolean activate);

  /**
   * @param player The player to check the editing state for
   * @return true if the player is currently editing via this editor.
   */
  boolean isEditing(PathPlayer<Player> player);
}
