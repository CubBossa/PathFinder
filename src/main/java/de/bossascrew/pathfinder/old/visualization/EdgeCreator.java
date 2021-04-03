package de.bossascrew.pathfinder.old.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import main.de.bossascrew.pathfinder.PathSystem;
import de.bossascrew.pathfinder.old.system.Edge;
import de.bossascrew.pathfinder.old.system.Node;

public class EdgeCreator {

	RoadMap rm;
	
	Player p;
	Node start;
	Node end;

	Particle typeEditmode;
	double particleViewDistanceEditmode;
	double steplength;
	int amount;
	
	public EdgeCreator(RoadMap rm, UUID uuid) {
		this.rm = rm;
		p = Bukkit.getPlayer(uuid);

		typeEditmode = PathSystem.getInstance().getConfigFile().getType();
		particleViewDistanceEditmode = PathSystem.getInstance().getConfigFile().getParticleViewDistance();
		steplength = PathSystem.getInstance().getConfigFile().getDistance();
		amount = PathSystem.getInstance().getConfigFile().getAmount();
	}
	
	public void playLine() {
		if(start == null || p == null) return;
		Vector vecLine = p.getLocation().toVector().add(new Vector(0,1,0)).subtract(start.loc);
		
		int steps = (int) (vecLine.length() / steplength);
		
		for(int i = 0; i < steps; i++) {
			Location pos = start.loc.clone().add(new Vector(0, 0.5, 0)).add(vecLine.clone().normalize().multiply(i * steplength)).toLocation(rm.getWorld());
			for(UUID uuid : rm.getEditMode()) {
				Player pp = Bukkit.getPlayer(uuid);
				spawnVisualizerParticles(pp, pos);
			}
		}
	}
	
	//TODO hier auch visualizer
	public void spawnVisualizerParticles(Player p, Location pos) {
		if(p.getLocation().distance(pos) < particleViewDistanceEditmode)
			p.spawnParticle(typeEditmode, pos, amount, 0.01, 0.01, 0.01, 0);
	}
	
	public void setStart(Node n) {
		start = n;
	}
	
	public void setEnd(Node n) {
		end = n;
		createEdge();
		rm.getVisualizer().refresh();
		rm.getEdgeManager().removeCreator(p.getUniqueId());
	}
	
	public void createEdge() {
		if(start == null || end == null) return;
		
		for(Node n : rm.getFile().waypoints) {
			if(n.value.equalsIgnoreCase(start.value)) {
				n.adjacencies = addTarget(start, end);
			} else if(n.value.equalsIgnoreCase(end.value)) {
				n.adjacencies = addTarget(end, start);
			}
		}
	}
	
	private Edge[] addTarget(Node n, Node target) {
		if(containsTarget(n, target)) return n.adjacencies;
		
		List<Edge> newEdges = new ArrayList<Edge>();
		for(Edge e : n.adjacencies) {
			newEdges.add(e);
		}
		newEdges.add(new Edge(target, n.loc.distance(target.loc)));
		return newEdges.toArray(new Edge[0]);
	}
	
	private boolean containsTarget(Node n, Node target) {
		for(Edge e : n.adjacencies) {
			if(e.target.equals(target)) return true;
		}
		return false;
	}
}
