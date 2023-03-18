package de.cubbossa.pathfinder.core.roadmap;

import java.util.UUID;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;

public interface NodeGroupEditor extends Keyed {

  void dispose();

  boolean isEdited();

  boolean toggleEditMode(UUID uuid);

  void cancelEditModes();

  /**
   * Sets a player into edit mode for this roadmap.
   *
   * @param uuid     the player to set the edit mode for
   * @param activate activate or deactivate edit mode
   */
  void setEditMode(UUID uuid, boolean activate);

  void showArmorStands(Player player);

  void hideArmorStands(Player player);

  boolean isEditing(UUID uuid);

  boolean isEditing(Player player);

  /**
   * Erstellt eine Liste aus Partikel Packets, die mithilfe eines Schedulers immerwieder an die Spieler im Editmode geschickt werden.
   * Um gelöschte und neue Kanten darstellen zu können, muss diese Liste aus Packets aktualisiert werden.
   * Wird asynchron ausgeführt
   */
  void updateEditModeParticles();
}
