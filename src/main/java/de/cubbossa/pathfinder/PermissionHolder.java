package de.cubbossa.pathfinder;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that stores a permission node.
 * The permission node is used to provide and prevent access to features of the plugin.
 */
public interface PermissionHolder {

  /**
   * @return null, if no permission is set. Null will by default be interpreted as "access".
   */
  @Nullable
  String getPermission();

  /**
   * @param permission The permission node to restrict this object or null, if unrestricted access.
   */
  void setPermission(@Nullable String permission);
}
