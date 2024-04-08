package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.Nameable;
import de.cubbossa.pathfinder.misc.NamespacedKey;

public interface DiscoverableModifier extends Modifier, Nameable {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:discoverable");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

}
