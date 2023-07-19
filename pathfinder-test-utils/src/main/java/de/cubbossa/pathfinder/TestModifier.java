package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.misc.NamespacedKey;

public record TestModifier(String data) implements Modifier {
  @Override
  public NamespacedKey getKey() {
    return TestModifierType.KEY;
  }
}
