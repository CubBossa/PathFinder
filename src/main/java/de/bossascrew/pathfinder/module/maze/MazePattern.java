package de.bossascrew.pathfinder.module.maze;

import org.bukkit.Location;

public abstract class MazePattern {

	abstract int getSpacing();
	abstract void placeNorth(Location location);
	abstract void placeNorthEast(Location location);
	abstract void placeNorthEastSouth(Location location);
	abstract void placeNorthEastSouthWest(Location location);
	abstract void placeNorthEastWest(Location location);
	abstract void placeNorthSouth(Location location);
	abstract void placeNorthSouthWest(Location location);
	abstract void placeNorthWest(Location location);
	abstract void placeEast(Location location);
	abstract void placeEastSouth(Location location);
	abstract void placeEastSouthWest(Location location);
	abstract void placeEastWest(Location location);
	abstract void placeSouth(Location location);
	abstract void placeSouthWest(Location location);
	abstract void placeWest(Location location);
}
