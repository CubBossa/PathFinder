package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.NamespacedKey;

public interface FindDistanceModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:find-distance");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  double distance();
}
