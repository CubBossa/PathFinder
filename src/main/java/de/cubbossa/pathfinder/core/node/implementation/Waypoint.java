package de.cubbossa.pathfinder.core.node.implementation;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Waypoint implements Node, Groupable {

	private final int nodeId;
	private final NamespacedKey roadMapKey;
	private final RoadMap roadMap;
	private final List<Edge> edges;
	private final Collection<NodeGroup> groups;

	private Location location;
	@Nullable
	private Double curveLength = null;

	public Waypoint(int databaseId, RoadMap roadMap) {
		this.nodeId = databaseId;
		this.roadMap = roadMap;
		this.roadMapKey = roadMap.getKey();
		this.groups = new HashSet<>();

		edges = new ArrayList<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Waypoint waypoint)) {
			return false;
		}

		return nodeId == waypoint.nodeId;
	}

	@Override
	public int hashCode() {
		return nodeId;
	}

	@Override
	public Edge connect(Node target) {
		return roadMap.connectNodes(this, target);
	}

	@Override
	public void disconnect(Node target) {
		roadMap.disconnectNodes(this, target);
	}

	@Override
	public int compareTo(@NotNull Node o) {
		return Integer.compare(nodeId, o.getNodeId());
	}

	@Override
	public Collection<String> getSearchTerms() {
		return groups.stream().flatMap(group -> group.getSearchTerms().stream()).collect(Collectors.toSet());
	}

	@Override
	public Collection<Node> getGroup() {
		return Lists.newArrayList(this);
	}

	public Collection<NodeGroup> getGroups() {
		return new HashSet<>(groups);
	}

	@Override
	public void addGroup(NodeGroup group) {
		groups.add(group);
	}

	@Override
	public void removeGroup(NodeGroup group) {
		groups.remove(group);
	}

	@Override
	public void clearGroups() {
		groups.clear();
	}

	@Override
	public String toString() {
		return "Waypoint{" +
				"nodeId=" + nodeId +
				", roadMapKey=" + roadMapKey +
				", location=" + location +
				", curveLength=" + curveLength +
				'}';
	}
}
