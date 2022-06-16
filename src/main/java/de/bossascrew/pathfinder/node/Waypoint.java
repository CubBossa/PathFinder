package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.roadmap.RoadMap;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class Waypoint implements Node {

	protected final int nodeId;
	protected final NamespacedKey roadMapKey;
	protected final RoadMap roadMap;
	protected final List<Integer> edges;

	protected Vector position;
	protected String nameFormat;
	protected Component displayName;
	@Nullable
	protected NamespacedKey groupKey = null;
	@Nullable
	protected String permission = null;
	@Nullable
	protected Double bezierTangentLength = null;
	protected Collection<String> searchTerms;

	public Waypoint(int databaseId, RoadMap roadMap, @Nullable String nameFormat) {
		this.nodeId = databaseId;
		this.roadMap = roadMap;
		this.roadMapKey = roadMap.getKey();
		this.nameFormat = nameFormat;

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
	public int compareTo(@NotNull Node o) {
		return Integer.compare(nodeId, o.getNodeId());
	}
}
