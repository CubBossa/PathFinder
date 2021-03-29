package pathfinder.old.system;

public class Edge{
    public final double cost;
    public final Node target;
    
    public Edge(Node targetNode, double costVal){
        target = targetNode;
        cost = costVal;
    }
}
