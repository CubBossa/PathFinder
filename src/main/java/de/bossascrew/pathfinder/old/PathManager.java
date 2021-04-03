package de.bossascrew.pathfinder.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.bossascrew.pathfinder.old.system.AStar;
import de.bossascrew.pathfinder.old.system.Node;

public class PathManager {

	private RoadMap rm;
	
	private Particle type;
	private int particleAmount;
	private double pathRandomness;
	private Vector pathDisplacement;
	private int delayInTicks;
	private int steps;
	private double viewDistance;
	private Particle.DustOptions data = new Particle.DustOptions(Color.AQUA, 1);
	
	double radian;
	double stepsInDegree;
	
	List<Location> circleLocs;
	
	private HashMap<UUID, PlayerSearchInfo> playerSearching;
	private List<UUID> editmode = new ArrayList<UUID>();

	boolean running;
	
	public PathManager(RoadMap rm) {
		this.rm = rm;
		playerSearching = new HashMap<UUID, PlayerSearchInfo>();
		
		type = rm.getFile().getType();
		particleAmount = rm.getFile().getAmount();
		pathRandomness = rm.getFile().getPathRandomness();
		pathDisplacement = rm.getFile().getOffset();
		delayInTicks = rm.getFile().getDelayInTicks();
		steps = rm.getFile().getSteps();
		viewDistance = rm.getFile().getParticleViewDistance();
		radian = rm.getFile().getRadian();
		stepsInDegree = rm.getFile().getStepsInDegree();
	}
	
	public void showPath(RoadMap rm, UUID ownerUUID, Location pLoc, Node ziel, World w) {
		List<Node> path = new AStar().AStaraufruf(rm, pLoc, ziel);
		calcCircularParticle(ziel.loc.toLocation(pLoc.getWorld()));
		playerSearching.put(ownerUUID, new PlayerSearchInfo(ownerUUID, path, pLoc.getWorld()));
		if(!running) runParticleTimer();
	}
	
	public void showPath(RoadMap rm, UUID ownerUUID, Location pLoc, Node ziel, World w, Node...startpoints) {
		List<Node> path = new AStar().AStaraufruf(rm, pLoc, ziel, startpoints);
		calcCircularParticle(ziel.loc.toLocation(pLoc.getWorld()));
		playerSearching.put(ownerUUID, new PlayerSearchInfo(ownerUUID, path, pLoc.getWorld()));
		if(!running) runParticleTimer();
	}
	
	private List<Integer> particleSchedulers;
	public void runParticleTimer() {
		particleSchedulers = new ArrayList<Integer>();
		for(int i = 0; i < steps; i++) {
			final int iFinal = i;
			int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PathSystem.getInstance(), new Runnable() {
				@Override
				public void run() {
					getParticleLocations(iFinal);
				}
			}, i*delayInTicks, delayInTicks*steps);
			particleSchedulers.add(taskID);
		}
		running = true;
	}
	private void getParticleLocations(int iStart) {
		for(PlayerSearchInfo psi : playerSearching.values()) {
			Player p = Bukkit.getPlayer(psi.getUUID());
			if(p == null || !p.isOnline()) {
				playerSearching.remove(psi.getUUID());
				return;
			}
			for(int i = iStart; i < psi.getPathParticles().size(); i += steps) {
				double rand = Math.random();
				if(psi.getPathParticles().get(i).distance(p.getLocation()) <= viewDistance) {
					if(type == Particle.REDSTONE) {
						p.spawnParticle(type, psi.getPathParticles().get(i).clone().add(pathDisplacement).add(new Vector(1,1,1).multiply(rand * pathRandomness)), particleAmount, 0, 0, 0, data);
					} else {
						p.spawnParticle(type, psi.getPathParticles().get(i).clone().add(pathDisplacement).add(new Vector(1,1,1).multiply(rand * pathRandomness)), particleAmount, 0, 0, 0, 0);
					}
				}
			}
			for(Location loc : circleLocs) {
				double rand = Math.random();
				if(loc.distance(p.getLocation()) <= viewDistance) {
					if(type == Particle.REDSTONE) {
						p.spawnParticle(type, loc.clone().add(pathDisplacement).add(new Vector(1,1,1).multiply(rand * pathRandomness)), particleAmount, 0, 0, 0, data);
					} else {
						p.spawnParticle(type, loc.clone().add(pathDisplacement).add(new Vector(1,1,1).multiply(rand * pathRandomness)), particleAmount, 0, 0, 0, 0);
					}
				}
			}
		}
	}
	
	public void calcCircularParticle(Location loc) {
		circleLocs = new ArrayList<Location>();
		float degreeTemp = 0;
		for(int i = (int) (360 / stepsInDegree); i > 0; i--) {
			circleLocs.add(new Location(loc.getWorld(), loc.getX() + radian * Math.sin(degreeTemp), loc.getY(), loc.getZ() + radian * Math.cos(degreeTemp)));
			degreeTemp += stepsInDegree;
		}
	}
	
	public boolean checkPlayer(Player p, double distance) {
		PlayerSearchInfo psi = playerSearching.get(p.getUniqueId());
		if(psi == null) return false;
		Location goal = psi.getPath().get(psi.getPath().size()-1).loc.toLocation(p.getWorld());
		return p.getLocation().distance(goal) <= distance;
	}
	
	public void stopPath(UUID uuid) {
		if(!playerSearching.containsKey(uuid)) return;
			
		playerSearching.remove(uuid);
		if(playerSearching.size() < 1) {
			running = false;
			for(int i : particleSchedulers) {
				Bukkit.getScheduler().cancelTask(i);
			}
		}
	}
	
	public void stopAllPaths() {
		for(UUID uuid : playerSearching.keySet()) {
			stopPath(uuid);
		}
	}
}
