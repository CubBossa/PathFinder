package de.cubbossa.pathfinder.editor;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;

public interface GraphEditor<Player> extends Disposable {

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
