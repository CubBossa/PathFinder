package de.bossascrew.pathfinder.old.system;

import org.bukkit.util.Vector;

public class Node{

	public final int id;
    public String value;
    public double g_scores;
    public double h_scores;
    public double f_scores = 0;
    public Edge[] adjacencies;
    public Node parent;
    public Vector loc;
    public String permission = "none";
    public boolean tangentReachSetManually = false;
    public float tangentReach;

    public Node(int id, String val, double hVal, Vector Loc){
    	this.id = id;
        value = val;
        h_scores = hVal;
        loc = Loc;
        adjacencies = new Edge[] {};
    }

    @Override
	public String toString(){
        return value;
    }
}
