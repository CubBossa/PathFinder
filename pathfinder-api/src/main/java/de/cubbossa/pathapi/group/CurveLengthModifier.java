package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;

public interface CurveLengthModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:curve-length");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  double curveLength();
}
