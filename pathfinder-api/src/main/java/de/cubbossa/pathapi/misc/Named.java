package de.cubbossa.pathapi.misc;

import net.kyori.adventure.text.Component;

/**
 * An object that can be represented as Component.
 */
public interface Named {

  /**
   * @return The display name formatted in the MiniMessage style.
   * @see "https://docs.adventure.kyori.net/minimessage/format.html"
   */
  String getNameFormat();

  /**
   * @return The display name as kyori Component. Must be the parsed version of the name format string and
   * may be cached.
   */
  Component getDisplayName();
}
