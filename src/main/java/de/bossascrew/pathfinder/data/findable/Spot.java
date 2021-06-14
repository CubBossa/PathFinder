package de.bossascrew.pathfinder.data.findable;

import org.bukkit.util.Vector;

public class Spot extends Node implements Findable {

    public Spot(int databaseId, int roadMapId, String name, Vector vector) {
        super(databaseId, roadMapId, name, vector);
    }
}
