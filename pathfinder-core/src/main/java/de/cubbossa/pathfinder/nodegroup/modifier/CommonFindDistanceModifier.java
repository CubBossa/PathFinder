package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.Modifier;

public record CommonFindDistanceModifier(double distance) implements Modifier, FindDistanceModifier {
}
