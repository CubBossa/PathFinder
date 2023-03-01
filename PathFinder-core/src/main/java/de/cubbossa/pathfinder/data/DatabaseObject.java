package de.cubbossa.pathfinder.data;

/**
 * Ensures, that a unique key will be provided.
 *
 * @param <K> The type of the key, for example {@link Integer}, {@link java.util.UUID} or {@link org.bukkit.NamespacedKey}
 */
public interface DatabaseObject<K> {

  /**
   * @return The unique key for this object to be identified in the database.
   */
  K getKey();
}
