package de.cubbossa.pathfinder.core.nodegroup.modifier;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public record FindDistanceModifier(double distance) implements GroupModifier {

	private static final NamespacedKey KEY = NamespacedKey.fromString("pathfinder:find_distance");
	@Override
	@NotNull
	public NamespacedKey getKey() {
		return KEY;
	}
}
