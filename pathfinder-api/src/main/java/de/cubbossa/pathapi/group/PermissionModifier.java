package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;

public interface PermissionModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:permission");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  String permission();
}
