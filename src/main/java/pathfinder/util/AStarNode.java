package pathfinder.util;

public class AStarNode {

    public int nodeId;

    public double g_scores;
    public double h_scores;
    public double f_scores = 0;
    public AStarNode parent;
    public AStarEdge[] adjacencies;

    public AStarNode(int nodeId, double h_scores) {
        this.nodeId = nodeId;
        this.h_scores = h_scores;
    }
}
