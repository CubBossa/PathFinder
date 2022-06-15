package de.bossascrew.pathfinder.node;

import net.kyori.adventure.text.Component;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Node {

	int getNodeId();

	int getRoadMapId();

	int getGroupId();

	String getNameFormat();

	Component getDisplayName();

	@Nullable String getPermission();

	Vector getPosition();

	List<Integer> getEdges();

	float getBezierTangentLength();
}
