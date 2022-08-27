package de.cubbossa.pathfinder;

import org.jetbrains.annotations.Nullable;

public interface PermissionHolder {

	@Nullable String getPermission();

	void setPermission(@Nullable String permission);
}
