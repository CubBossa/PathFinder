package de.cubbossa.pathfinder.api.group;

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

  void showArmorStands(PathPlayer<Player> player);

  void hideArmorStands(PathPlayer<Player> player);

  boolean isEditing(PathPlayer<Player> uuid);

  /**
   * Erstellt eine Liste aus Partikel Packets, die mithilfe eines Schedulers immerwieder an die Spieler im Editmode geschickt werden.
   * Um gelöschte und neue Kanten darstellen zu können, muss diese Liste aus Packets aktualisiert werden.
   * Wird asynchron ausgeführt
   */
  void updateEditModeParticles();
}
