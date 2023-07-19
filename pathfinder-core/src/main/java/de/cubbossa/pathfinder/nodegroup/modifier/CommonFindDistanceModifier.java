package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.Modifier;

public record CommonFindDistanceModifier(double distance) implements Modifier, FindDistanceModifier {

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
