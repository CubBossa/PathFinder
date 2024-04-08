package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathfinder.group.FindDistanceModifier;
import de.cubbossa.pathfinder.group.Modifier;

public record FindDistanceModifierImpl(double distance) implements Modifier, FindDistanceModifier {

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
