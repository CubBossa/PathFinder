package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.CurveLengthModifier;
import de.cubbossa.pathapi.group.Modifier;

public record CurveLengthModifierImpl(double curveLength) implements CurveLengthModifier {

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
