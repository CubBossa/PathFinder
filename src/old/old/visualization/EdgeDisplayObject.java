package de.bossascrew.pathfinder.old.visualization;

import de.bossascrew.pathfinder.old.RoadMap;
import de.bossascrew.pathfinder.old.system.Node;

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
