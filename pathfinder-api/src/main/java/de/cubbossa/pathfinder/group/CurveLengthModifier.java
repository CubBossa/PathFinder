package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.NamespacedKey;

public interface CurveLengthModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:curve-length");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  double curveLength();
}
