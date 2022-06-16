package de.bossascrew.pathfinder.node;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Node extends Navigable, Comparable<Node> {

	int getNodeId();

	NamespacedKey getRoadMapKey();

	String getNameFormat();

	void setNameFormat(String format);

	Component getDisplayName();

	Vector getPosition();

	void setPosition(Vector position);

	Collection<Edge> getEdges();

	@Nullable NamespacedKey getGroupKey();

	void setGroupKey(@Nullable NamespacedKey key);

	@Nullable String getPermission();

	void setPermission(@Nullable String permission);

	@Nullable Double getBezierTangentLength();

	void setBezierTangentLength(Double value);

	Edge connect(Node target);

	void disconnect(Node target);
}
