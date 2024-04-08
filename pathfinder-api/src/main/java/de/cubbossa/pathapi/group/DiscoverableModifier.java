package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.Nameable;
import de.cubbossa.pathapi.misc.Named;
import de.cubbossa.pathapi.misc.NamespacedKey;

public interface DiscoverableModifier extends Modifier, Nameable {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:discoverable");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

}
