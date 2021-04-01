package pathfinder;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.Vector;
import pathfinder.handler.RoadMapHandler;
import pathfinder.old.system.Edge;
import pathfinder.util.AStarNode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Node {

    public static int NO_GROUP_ID = -1;

    private int databaseId;
    private int roadMapId;
    private RoadMap roadMap;
    private int nodeGroupId = NO_GROUP_ID;
    private Vector vector;
    private List<Integer> edges;

    private String name;
    @Setter
    private String permission = "none";
    @Setter
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
    }

    public void setGroup(NodeGroup nodeGroup) {
        setGroup(nodeGroup.getDatabaseId());
    }

    public void removeGroup() {
        nodeGroupId = NO_GROUP_ID;
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
}
