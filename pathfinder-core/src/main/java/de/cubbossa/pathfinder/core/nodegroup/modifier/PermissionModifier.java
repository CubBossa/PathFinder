package de.cubbossa.pathfinder.core.nodegroup.modifier;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public record PermissionModifier(String permission) implements GroupModifier {
	private static final NamespacedKey KEY = NamespacedKey.fromString("pathfinder:permission");
	@Override
	@NotNull
	public NamespacedKey getKey() {
		return KEY;
	}
}
