package de.cubbossa.pathfinder.api.misc;

import java.util.UUID;

public interface WorldLocation {

	UUID getWorld();
	double getX();
	double getY();
	double getZ();

}
