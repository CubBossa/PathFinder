package de.cubbossa.pathapi.editor;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;

public interface GraphEditor<Player> {

    NamespacedKey getGroupKey();

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
