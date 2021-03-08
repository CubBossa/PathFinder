package main.de.bossascrew.pathfinder.visualization;

import main.de.bossascrew.pathfinder.RoadMap;
import main.de.bossascrew.pathfinder.system.Node;

public class EdgeDisplayObject {

	public RoadMap rm;
	
	public Node n1;
	public Node n2;
	
	public EdgeDisplayObject(RoadMap rm, Node n1, Node n2) {
		this.rm = rm;
		this.n1 = n1;
		this.n2 = n2;
	}
}
