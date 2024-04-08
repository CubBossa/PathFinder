package de.cubbossa.pathapi.misc;

/**
 * A {@link Named} that can also be renamed.
 */
public interface Nameable extends Named {

  /**
   * Sets the display name. The call if this method must also refresh the display name component if stored locally.
   *
   * @param name The display name formatted in the MiniMessage style.
   * @see "https://docs.adventure.kyori.net/minimessage/format.html"
   */
  void setNameFormat(String name);
}
