package pathfinder.old.system;

import java.util.ArrayList;
import java.util.List;

public class NodeGroup {

	private static List<NodeGroup> groups = new ArrayList<NodeGroup>();
	
	String name;
	List<Node> nodes;
	
	public NodeGroup(String name) {
		this.name = name;
		this.nodes = new ArrayList<Node>();
	}
	
	public boolean registerNode(Node n) {
		if(getGroup(n) == null) {
			this.nodes.add(n);
			return true;
		} else return false;
	}
	
	public static NodeGroup getGroup(String name) {
		for(NodeGroup group : groups) {
			if(group.name.equals(name)) return group;
		}
		return null;
	}
	
	public static NodeGroup getGroup(Node n) {
		for(NodeGroup group : groups) {
			if(group.nodes.contains(n)) return group;
		}
		return null;
	}
}
