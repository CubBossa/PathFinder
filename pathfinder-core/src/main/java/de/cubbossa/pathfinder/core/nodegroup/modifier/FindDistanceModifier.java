package de.cubbossa.pathfinder.core.nodegroup.modifier;

import de.cubbossa.pathfinder.Modifier;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public record FindDistanceModifier(double distance) implements Modifier {
}
