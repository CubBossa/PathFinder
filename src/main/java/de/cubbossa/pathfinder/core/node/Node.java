package de.cubbossa.pathfinder.core.node;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Node extends Navigable, Comparable<Node> {

	int getNodeId();

	NamespacedKey getRoadMapKey();

	NodeType<? extends Node> getType();

	Location getLocation();

	void setLocation(Location location);

	Collection<Edge> getEdges();

	@Nullable Double getCurveLength();

	void setCurveLength(Double value);

	Edge connect(Node target);

	void disconnect(Node target);
}
