package de.bossascrew.pathfinder.module.maze;

import de.bossascrew.pathfinder.core.node.Node;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class RoadMapMazePattern extends MazePattern {

	private int spacing = 2;
	private RoadMap roadMap;
	private Map<Integer, Node> xOpenQueue = new HashMap<>();
	private Map<Integer, Node> yOpenQueue = new HashMap<>();

	public RoadMapMazePattern(RoadMap roadMap) {
		this.roadMap = roadMap;
	}

	void place(Location location, boolean north, boolean east, boolean south, boolean west) {
		location = location.getBlock().getLocation().add(.5, 1.5, .5);

		Node node = roadMap.createNode(RoadMapHandler.WAYPOINT_TYPE, location.toVector());

		Node xOpen = xOpenQueue.get(location.getBlockZ());
		if (xOpen != null && east) {
			roadMap.connectNodes(xOpen, node, false);
			xOpenQueue.remove(location.getBlockZ());
		}
		Node yOpen = yOpenQueue.get(location.getBlockX());
		if (yOpen != null && south) {
			roadMap.connectNodes(yOpen, node, false);
			yOpenQueue.remove(location.getBlockX());
		}
		if (west) {
			xOpenQueue.put(location.getBlockZ(), node);
		}
		if (north) {
			yOpenQueue.put(location.getBlockX(), node);
		}
	}

	@Override
	void placeNorth(Location location) {
		place(location, false, true, true, true);
	}

	@Override
	void placeNorthEast(Location location) {
		place(location, false, false, true, true);
	}

	@Override
	void placeNorthEastSouth(Location location) {
		place(location, false, false, false, true);
	}

	@Override
	void placeNorthEastSouthWest(Location location) {
		place(location, false, false, false, false);
	}

	@Override
	void placeNorthEastWest(Location location) {
		place(location, false, false, true, false);
	}

	@Override
	void placeNorthSouth(Location location) {
		// straight
	}

	@Override
	void placeNorthSouthWest(Location location) {
		place(location, false, true, false, false);
	}

	@Override
	void placeNorthWest(Location location) {
		place(location, false, true, true, false);
	}

	@Override
	void placeEast(Location location) {
		place(location, true, false, true, true);
	}

	@Override
	void placeEastSouth(Location location) {
		place(location, true, false, false, true);
	}

	@Override
	void placeEastSouthWest(Location location) {
		place(location, true, false, false, false);
	}

	@Override
	void placeEastWest(Location location) {
		//straight
	}

	@Override
	void placeSouth(Location location) {
		place(location, true, true, false, true);
	}

	@Override
	void placeSouthWest(Location location) {
		place(location, true, true, false, false);
	}

	@Override
	void placeWest(Location location) {
		place(location, true, true, true, false);
	}
}
