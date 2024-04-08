package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.group.Modifier;
import de.cubbossa.pathfinder.misc.NamespacedKey;

public record TestModifier(String data) implements Modifier {
  @Override
  public NamespacedKey getKey() {
    return TestModifierType.KEY;
  }
}
