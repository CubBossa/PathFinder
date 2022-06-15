package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Node implements NavigationTarget {

	protected final int nodeId;
	protected final int roadMapId;
	protected final RoadMap roadMap;
	protected final List<Integer> edges;

	protected Vector position;
	protected String nameFormat;
	protected int groupId = -1;
	protected @Nullable
	String permission = null;
	protected @Nullable
	Double bezierTangentLength = null;

	public Node(int databaseId, RoadMap roadMap, @Nullable String nameFormat) {
		this.nodeId = databaseId;
		this.roadMap = roadMap;
		this.roadMapId = roadMap.getRoadmapId();
		this.nameFormat = nameFormat;

		edges = new ArrayList<>();
	}
}
