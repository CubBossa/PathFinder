package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.roadmap.RoadMap;
import net.kyori.adventure.text.Component;
import org.apache.commons.codec.language.bm.Rule;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class EmptyNode implements Node {

	private final RoadMap roadMap;

	public EmptyNode(RoadMap roadMap) {
		this.roadMap = roadMap;
	}

	@Override
	public String getNameFormat() {
		return "empty";
	}

	@Override
	public void setNameFormat(String name) {

	}

	@Override
	public Component getDisplayName() {
		return Component.text("empty");
	}

	@Override
	public Collection<String> getSearchTerms() {
		return new HashSet<>();
	}

	@Override
	public Collection<Node> getGroup() {
		return new HashSet<>();
	}

	@Override
	public int getNodeId() {
		return -1;
	}

	@Override
	public NamespacedKey getRoadMapKey() {
		return roadMap.getKey();
	}

	@Override
	public Vector getPosition() {
		return new Vector(0, 0, 0);
	}

	@Override
	public void setPosition(Vector position) {

	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public Collection<Edge> getEdges() {
		return new HashSet<>();
	}

	@Override
	public @Nullable NamespacedKey getGroupKey() {
		return null;
	}

	@Override
	public void setGroupKey(@Nullable NamespacedKey key) {

	}

	@Override
	public @Nullable String getPermission() {
		return null;
	}

	@Override
	public void setPermission(@Nullable String permission) {

	}

	@Override
	public @Nullable Double getBezierTangentLength() {
		return null;
	}

	@Override
	public void setBezierTangentLength(Double value) {

	}

	@Override
	public Edge connect(Node target) {
		return null;
	}

	@Override
	public void disconnect(Node target) {

	}

	@Override
	public int compareTo(@NotNull Node o) {
		return 0;
	}
}
