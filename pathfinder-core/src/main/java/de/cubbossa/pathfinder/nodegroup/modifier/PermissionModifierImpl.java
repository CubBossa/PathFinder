package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.PermissionModifier;

public record PermissionModifierImpl(String permission) implements PermissionModifier {

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
