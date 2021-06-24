package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import org.bukkit.util.Vector;

public class Spot extends Node {

    public Spot(int databaseId, RoadMap roadMap, String name, Vector vector) {
        super(databaseId, roadMap, name, vector);
    }
}
