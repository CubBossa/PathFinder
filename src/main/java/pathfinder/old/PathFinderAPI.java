package pathfinder.old;

import org.bukkit.entity.Player;

import pathfinder.old.system.Node;

/**
 * Gives access to all the pathfinder navigate methods.
 * Creating and setting up a roadmap needs to be done ingame!
 * 
 * You can also create Roadmaps and Targets by accessing code but it might mess things up.
 * @author leo
 */
public class PathFinderAPI {

	private static PathFinderAPI INSTANCE;
	
	/**
	 * 
	 * @return API instance
	 */
	public static PathFinderAPI get() {
		if(INSTANCE == null) INSTANCE = new PathFinderAPI();
		return INSTANCE;
	}
	
	/**
	 * Navigate a player
	 * @param p the player that will see the particles
	 * @param roadmap the roadmap the target is on
	 * @param waypointName the name of the waypoint (in case you have neither target id nor object reference)
	 */
	public void navigatePlayer(Player p, String roadmap, String waypointName) {
		RoadMap rm = RoadMap.getRoadMap(roadmap);
		if(rm == null) {
			System.out.println("That Roadmap does not exist: " + roadmap);
			return;
		}
		Node target = rm.getFile().getNode(waypointName);
		if(target == null) {
			System.out.println("That targetpoint does not exist: " + waypointName);
			return;
		}
		navigatePlayer(p, rm, target);
	}
	
	/**
	 * Navigate a player
	 * @param p the player that will see the particles
	 * @param roadmap the roadmap the target is on
	 * @param waypointID the id of the waypoint (in case it is stored somewhere and you have no access to Node object reference)
	 */
	public void navigatePlayer(Player p, String roadmap, int waypointID) {
		RoadMap rm = RoadMap.getRoadMap(roadmap);
		if(rm == null) return;
		Node target = rm.getFile().getNode(waypointID);
		if(target == null) return;
		navigatePlayer(p, rm, target);
	}
	
	/**
	 * Navigate a player
	 * @param p the player that will see the particles
	 * @param roadmap the roadmap the target is on
	 * @param target the target point on the roadmap
	 */
	private void navigatePlayer(Player p, RoadMap roadmap, Node target) {
		roadmap.getPathFinder().showPath(roadmap, p.getUniqueId(), p.getLocation(), target, p.getWorld());
	}
	
	/**
	 * Cancelling path for all roadmaps the player navigates on
	 * @param p the player to cancel all paths from
	 */
	public void cancelPath(Player p) {
		if(p == null) return;
		for(RoadMap rm : RoadMap.getRoadMaps()) {
			rm.getPathFinder().stopPath(p.getUniqueId());
		}
	}
	
	/**
	 * Cancelling the path of a player on a roadmap
	 * @param p the player to cancel the path from
	 * @param roadmapName the roadmap to cancel the path from
	 */
	public void cancelPath(Player p, String roadmapName) {
		if(p == null) return;
		RoadMap rm = RoadMap.getRoadMap(roadmapName);
		if(rm == null) return;
		rm.getPathFinder().stopPath(p.getUniqueId());
	}
	
	/**
	 * Cancelling all paths that are active
	 * @param roadmapName the roadmap to cancell all paths from
	 */
	public void cancelPath(String roadmapName) {
		RoadMap rm = RoadMap.getRoadMap(roadmapName);
		if(rm == null) return;
		rm.getPathFinder().stopAllPaths();
	}
}
