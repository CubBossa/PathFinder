package de.bossascrew.pathfinder.old;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import de.bossascrew.pathfinder.old.system.Node;
import de.bossascrew.pathfinder.old.system.PathCornerCalculator;

public class PlayerSearchInfo {
	
	private UUID ownerUUID;
	private List<Node> path;
	private List<Location> pathParticles;
	
	public PlayerSearchInfo(UUID uuid, List<Node> path, World w) {
		this.ownerUUID = uuid;
		this.path = path;
		pathParticles = getParticleLocations(path, 0.3, w); //to be set in config
	}
	
	PathCornerCalculator cornerCalculator = null;
	private List<Location> getParticleLocations(List<Node> path, double distance, World w) {
		List<Location> particlePositions = new ArrayList<Location>();
		if(path.size() < 1) return particlePositions;
		particlePositions.add(Bukkit.getPlayer(ownerUUID).getLocation());

		for(int i = 1; i < path.size(); i++) {
			Node prev;
			try {
				prev = path.get(i-1);
			} catch(IndexOutOfBoundsException e) {
				prev = null;
			}
			Node after;
			try {
				after = path.get(i+1);
			} catch(IndexOutOfBoundsException e) {
				after = null;
			}
			PathCornerCalculator cornerCalcTemp = new PathCornerCalculator(prev, path.get(i), after, 
					particlePositions.size() < 1 ? null : particlePositions.get(particlePositions.size()-1).toVector());
			cornerCalculator = cornerCalcTemp;
			for(Vector v : cornerCalculator.getPointsByDistance(distance)) {
				particlePositions.add(v.toLocation(w));
			}
		}
		return particlePositions;
	}
	
	public UUID getUUID() {
		return ownerUUID;
	}
	public List<Node> getPath() {
		return path;
	}
	public List<Location> getPathParticles() {
		return pathParticles;
	}
}
