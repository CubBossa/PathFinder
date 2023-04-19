package de.cubbossa.pathfinder.api.editor;

import de.cubbossa.pathfinder.api.misc.Keyed;

import de.cubbossa.pathfinder.api.misc.PathPlayer;

public interface NodeGroupEditor<Player> extends Keyed {

  void dispose();

  boolean isEdited();

  boolean toggleEditMode(PathPlayer<Player> player);

  void cancelEditModes();

  /**
   * Sets a player into edit mode for this roadmap.
   *
   * @param player   the player to set the edit mode for
   * @param activate activate or deactivate edit mode
   */
  void setEditMode(PathPlayer<Player> player, boolean activate);

  boolean isEditing(PathPlayer<Player> uuid);
}
