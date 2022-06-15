package de.bossascrew.pathfinder.data.findable;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface NavigationTarget {

	int getNodeId();

	int getRoadMapId();

	int getGroupId();

	String getNameFormat();

	@Nullable String getPermission();

	Vector getPosition();

	List<Integer> getEdges();

	float getBezierTangentLength();
}
