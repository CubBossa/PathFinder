package de.bossascrew.pathfinder.node;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Node extends Navigable, Comparable<Node> {

	int getNodeId();

	NamespacedKey getRoadMapKey();

	Vector getPosition();

	void setPosition(Vector position);

	Location getLocation();

	Collection<Edge> getEdges();

	@Nullable String getPermission();

	void setPermission(@Nullable String permission);

	@Nullable Double getCurveLength();

	void setCurveLength(Double value);

	Edge connect(Node target);

	void disconnect(Node target);
}
