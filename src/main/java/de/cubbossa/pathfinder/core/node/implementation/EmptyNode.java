package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class EmptyNode implements Node {

	private final RoadMap roadMap;
	@Getter
	private final Location location;

	public EmptyNode(RoadMap roadMap, World world) {
		this.roadMap = roadMap;
		this.location = new Location(world, 0, 0, 0);
	}

	@Override
	public NodeType<Node> getType() {
		return null;
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
	public void setLocation(Location location) {

	}

	@Override
	public Collection<Edge> getEdges() {
		return new HashSet<>();
	}

	@Override
	public @Nullable Double getCurveLength() {
		return null;
	}

	@Override
	public void setCurveLength(Double value) {

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

	@Override
	public String toString() {
		return "EmptyNode{}";
	}
}
