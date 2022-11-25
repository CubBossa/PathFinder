package de.cubbossa.pathfinder.core.node.implementation;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.query.SearchQueryAttribute;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
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
	public NodeType<Waypoint> getType() {
		return RoadMapHandler.WAYPOINT_TYPE;
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
	public Collection<SearchTerm> getSearchTerms() {
		return groups.stream().flatMap(group -> group.getSearchTerms().stream()).collect(Collectors.toSet());
	}

	@Override
	public boolean matches(SearchTerm searchTerm) {
		return groups.stream().anyMatch(g -> g.matches(searchTerm));
	}

	@Override
	public boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes) {
		return groups.stream().anyMatch(g -> g.matches(searchTerm, attributes));
	}

	@Override
	public boolean matches(String term) {
		return groups.stream().anyMatch(g -> g.matches(term));
	}

	@Override
	public boolean matches(String term, Collection<SearchQueryAttribute> attributes) {
		return groups.stream().anyMatch(g -> g.matches(term, attributes));
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
