package de.bossascrew.pathfinder.astar;

import de.bossascrew.pathfinder.data.findable.Findable;

public class AStarNode {

    public final Findable findable;

    public double g_scores;
    public double h_scores;
    public double f_scores = 0;
    public AStarNode parent;
    public AStarEdge[] adjacencies;

    public AStarNode(Findable findable, double h_scores) {
        this.findable = findable;
        this.h_scores = h_scores;
    }
}
