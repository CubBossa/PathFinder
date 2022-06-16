package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.roadmap.RoadMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

@Getter
public class PlayerNode extends Waypoint {

    private final String name;
    private final Location location;
    private final RoadMap roadMap;

    public PlayerNode(Player player, RoadMap roadMap) {
        super(-1, roadMap, player.getName());
        this.name = player.getName();
        this.location = player.getLocation();
        this.roadMap = roadMap;
    }

    public int getNodeId() {
        return -1;
    }

    public Vector getVector() {
        return location.toVector();
    }

    @Override
    public String getScope() {
        return "PLAYER";
    }

    public List<Integer> getEdges() {
        return null;
    }

    public String getPermission() {
        return "none";
    }

    @Override
    public @Nullable Integer getNodeGroupId() {
        return null;
    }

    @Override
    public NodeGroup getGroup() {
        return null;
    }

    public Double getBezierTangentLength() {
        return roadMap.getDefaultBezierTangentLength();
    }

    public double getBezierTangentLengthOrDefault() {
        return roadMap.getDefaultBezierTangentLength();
    }

    @Override
    void updateData() {
    }
}
