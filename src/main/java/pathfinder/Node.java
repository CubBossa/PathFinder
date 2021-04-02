package pathfinder;

import de.bossascrew.core.util.PluginUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.Vector;
import pathfinder.data.DatabaseModel;
import pathfinder.handler.RoadMapHandler;
import pathfinder.old.system.Edge;
import pathfinder.util.AStarNode;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Node {

    public static int NO_GROUP_ID = -1;

    private final int databaseId;
    private int roadMapId;
    private RoadMap roadMap;
    private int nodeGroupId = NO_GROUP_ID;
    private Vector vector;
    private List<Integer> edges;

    private String name;
    private String permission = "none";
    private Double bezierTangentLength = null;


    public Node(int databaseId, int roadMapId, String name, Vector vector){
        this.databaseId = databaseId;
        this.roadMapId = roadMapId;
        this.roadMap = RoadMapHandler.getInstance().getRoadMap(roadMapId);
        this.name = name;
        this.vector = vector;

        edges = new ArrayList<Integer>();
    }

    @Override
    public String toString(){
        return name;
    }

    public void setGroup(int nodeGroupId) {
        this.nodeGroupId = nodeGroupId;
        updateData();
    }

    public void setGroup(NodeGroup nodeGroup) {
        setGroup(nodeGroup.getDatabaseId());
    }

    public void removeGroup() {
        nodeGroupId = NO_GROUP_ID;
        updateData();
    }

    public Double getBezierTangentLength() {
        return bezierTangentLength;
    }

    public double getEffectiveBezierTangentLength() {
        if(bezierTangentLength == null) {
            return roadMap.getDefaultBezierTangentLength();
        }
        return bezierTangentLength;
    }

    public AStarNode getAStarNode(Vector startPoint) {
        return new AStarNode(databaseId, startPoint.distance(vector));
    }

    public void setName(String name) {
        this.name = name;
        updateData();
    }

    public void setNodeGroupId(int groupId) {
        this.nodeGroupId = groupId;
        updateData();
    }

    public void setBezierTangentLength(@Nullable Double bezierTangentLength) {
        this.bezierTangentLength = bezierTangentLength;
        updateData();
    }

    public void setVector(Vector vector) {
        //TODO teleportiere zugehörigen Armorstand mit
        this.vector = vector;
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
}
