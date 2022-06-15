package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.Waypoint;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.HashSet;

@Getter
@Setter
public class NodeGroup extends HashSet<Node> {

    public static final int NO_GROUP = -1;

    private final int groupId;
    private final RoadMap roadMap;
    private String nameFormat;
    private Component displayName;
    private boolean findable;

    public NodeGroup(int groupId, RoadMap roadMap, String nameFormat) {
        this(groupId, roadMap, nameFormat, null);
    }

    public NodeGroup(int groupId, RoadMap roadMap, String nameFormat, Collection<Waypoint> nodes) {
        super(nodes);
        this.groupId = groupId;
        this.roadMap = roadMap;
        this.nameFormat = nameFormat;
        this.findable = false;
    }
}
