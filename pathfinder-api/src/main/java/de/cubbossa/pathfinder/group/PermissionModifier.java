package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.NamespacedKey;

public interface PermissionModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:permission");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  String permission();
}
