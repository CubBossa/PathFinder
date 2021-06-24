package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Node implements Findable {

    public static final int NO_GROUP_ID = -1;

    private final int databaseId;
    private final int roadMapId;
    private final RoadMap roadMap;
    private final List<Integer> edges;
    private int nodeGroupId = NO_GROUP_ID;
    private Vector vector;

    private String name;
    private String permission = "none";
    private Double bezierTangentLength = null;


    public Node(int databaseId, RoadMap roadMap, String name, Vector vector) {
        this.databaseId = databaseId;
        this.roadMap = roadMap;
        this.roadMapId = roadMap.getDatabaseId();
        this.name = name;
        this.vector = vector;

        edges = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name;
    }

    public void setGroup(int nodeGroupId) {
        this.nodeGroupId = nodeGroupId;
        updateData();
    }

    public void setGroup(FindableGroup nodeGroup) {
        setGroup(nodeGroup.getDatabaseId());
    }

    public void removeFindableGroup() {
        nodeGroupId = NO_GROUP_ID;
        updateData();
    }

    public @Nullable
    Double getBezierTangentLength() {
        return bezierTangentLength;
    }

    /**
     * @return Gibt die Bezierwichtung zurück, und falls diese nicht gesetzt ist den vorgegebenen Defaultwert der Roadmap.
     */
    public double getBezierTangentLengthOrDefault() {
        if (bezierTangentLength == null) {
            return roadMap.getDefaultBezierTangentLength();
        }
        return bezierTangentLength;
    }

    public void setName(String name) {
        this.name = name;
        roadMap.updateArmorStandDisplay(this);
        updateData();
    }

    public void setNodeGroupId(int groupId) {
        this.nodeGroupId = groupId;
        roadMap.updateArmorStandDisplay(this);
        updateData();
    }

    public void setBezierTangentLength(@Nullable Double bezierTangentLength) {
        this.bezierTangentLength = bezierTangentLength;
        updateData();
    }

    public void setVector(Vector vector) {
        this.vector = vector;
        roadMap.updateArmorStandPosition(this);
        roadMap.updateEditModeParticles();
        updateData();
    }

    public void setPermission(String permission) {
        this.permission = permission;
        updateData();
    }

    private void updateData() {
        PluginUtils.getInstance().runAsync(() -> {
            DatabaseModel.getInstance().updateNode(this);
        });
    }

    public Location getLocation() {
        return vector.toLocation(roadMap.getWorld());
    }

    public FindableGroup getFindableGroup() {
        return roadMap.getFindableGroup(this);
    }
}
