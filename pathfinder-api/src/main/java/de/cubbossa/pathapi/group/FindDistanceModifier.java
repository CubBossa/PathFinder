package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;

public interface FindDistanceModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:find-distance");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  double distance();
}
