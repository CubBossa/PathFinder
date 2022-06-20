package de.bossascrew.pathfinder.roadmap;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.events.node.EdgeCreatedEvent;
import de.bossascrew.pathfinder.events.node.EdgeDeletedEvent;
import de.bossascrew.pathfinder.events.node.NodeCreatedEvent;
import de.bossascrew.pathfinder.events.node.NodeDeletedEvent;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.util.HashedRegistry;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.StringUtils;
import de.bossascrew.pathfinder.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class RoadMap implements Keyed {

	private final NamespacedKey key;
	private String nameFormat;
	private Component displayName;
	private World world;

	private boolean findableNodes;
	private double nodeFindDistance;
	private double defaultBezierTangentLength;

	private final Map<Integer, Node> nodes;
	private final Collection<Edge> edges;
	private final HashedRegistry<NodeGroup> groups;

	private PathVisualizer visualizer;

	public RoadMap(NamespacedKey key, String name, World world, boolean findableNodes, PathVisualizer visualizer,
				   double nodeFindDistance, double defaultBezierTangentLength) {

		this.key = key;
		this.setNameFormat(name);
		this.world = world;
		this.findableNodes = findableNodes;
		this.nodeFindDistance = nodeFindDistance;
		this.defaultBezierTangentLength = defaultBezierTangentLength;

		this.groups = new HashedRegistry<>();
		this.nodes = new TreeMap<>();
		this.edges = new HashSet<>();

		setVisualizer(visualizer);
	}

	public void setNameFormat(String nameFormat) {
		this.nameFormat = nameFormat;
		this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
	}

	public void loadGroups() {
		groups.clear();
		groups.putAll(PathPlugin.getInstance().getDatabase().loadNodeGroups(this));
	}

	public void loadNodesAndEdges() {
		nodes.clear();
		nodes.putAll(PathPlugin.getInstance().getDatabase().loadNodes(this));
		edges.clear();
		edges.addAll(PathPlugin.getInstance().getDatabase().loadEdges(this));
	}

	public Graph<Node, Edge> toGraph() {
		return new DefaultDirectedGraph<>(Edge.class);
	}

	public Waypoint createNode(Vector vector, String name) {
		return createNode(vector, name, null, null);
	}

	public @Nullable
	Waypoint createNode(Vector vector, String name, @Nullable Double bezierTangentLength, String permission) {

		Waypoint node = PathPlugin.getInstance().getDatabase().createNode(this, Waypoint.class, null,
				vector.getX(), vector.getY(), vector.getZ(), name, bezierTangentLength, permission);

		addNode(node);
		Bukkit.getPluginManager().callEvent(new NodeCreatedEvent(node));

		return node;
	}

	public void removeNodes(NodeSelection selection) {
		for (Node node : selection) {
			removeNode(node);
		}
	}

	public void removeNode(int id) {
		Node node = getNode(id);
		if (node != null) {
			removeNode(node);
		}
	}

	public void removeNode(Node node) {
		for (Edge edge : new ArrayList<>(edges)) {
			if (edge.getEnd().equals(node)) {
				edge.getEnd().getEdges().remove(edge);
				edges.remove(edge);
				PathPlugin.getInstance().getDatabase().deleteEdge(edge);
			} else if (edge.getStart().equals(node)) {
				edges.remove(edge);
				PathPlugin.getInstance().getDatabase().deleteEdge(edge);
			}
		}

		nodes.remove(node.getNodeId());
		PathPlugin.getInstance().getDatabase().deleteNode(node.getNodeId());

		Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(new NodeDeletedEvent(node)));
	}

	public void addNode(Node node) {
		nodes.put(node.getNodeId(), node);
	}

	public void setNodes(Map<Integer, Waypoint> nodes) {
		this.nodes.clear();
		this.nodes.putAll(nodes);
	}

	public void addFindables(Map<Integer, Waypoint> findables) {
		this.nodes.putAll(findables);
	}

	public @Nullable
	Node getNode(int nodeId) {
		for (Node node : nodes.values()) {
			if (node.getNodeId() == nodeId) {
				return node;
			}
		}
		return null;
	}

	public @Nullable
	NodeGroup getNodeGroup(Node node) {
		return getNodeGroup(node.getGroupKey());
	}

	public @Nullable
	NodeGroup getNodeGroup(NamespacedKey key) {
		if (key == null) {
			return null;
		}
		return groups.get(key);
	}

	public void removeNodeGroup(NodeGroup group) {
		this.groups.remove(group.getKey());
	}

	public NodeGroup createNodeGroup(NamespacedKey key, boolean findable) {

		NodeGroup group = PathPlugin.getInstance().getDatabase().createNodeGroup(this, key, StringUtils.getRandHexString() + "A Group", findable);
		groups.put(group);
		return group;
	}


	public Edge getEdge(Node start, Node end) {
		return edges.stream().filter(edge -> edge.getStart().equals(start) && edge.getEnd().equals(end)).findFirst().orElse(null);
	}

	/**
	 * erstellt neue Edge in der Datenbank
	 */
	public Edge connectNodes(Node start, Node end) {
		return connectNodes(start, end, 1);
	}

	public Edge connectNodes(Node start, Node end, float weight) {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Cannot connect node with itself.");
		}
		Edge edge = PathPlugin.getInstance().getDatabase().createEdge(start, end, weight);

		start.getEdges().add(edge);
		edges.add(edge);

		Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(new EdgeCreatedEvent(edge)));

		return edge;
	}

	public void disconnectNodes(Node start, Node end) {
		disconnectNodes(getEdge(start, end));
	}

	public void disconnectNode(Node node) {

	}

	public void disconnectNodes(Edge edge) {
		if (edge == null) {
			return;
		}
		edge.getStart().getEdges().remove(edge);
		edge.getEnd().getEdges().remove(edge);
		edges.remove(edge);

		Bukkit.getPluginManager().callEvent(new EdgeDeletedEvent(edge));
	}

	public void delete() {
		PathPlugin.getInstance().getDatabase().deleteRoadMap(this);
	}

	private Collection<Edge> getEdgesFrom(Node node) {
		Collection<Edge> ret = new ArrayList<>();
		for (Edge edge : edges) {
			if (edge.getStart().equals(node)) {
				ret.add(edge);
			}
		}
		return ret;
	}

	private Collection<Edge> getEdgesTo(Node node) {
		Collection<Edge> ret = new ArrayList<>();
		for (Edge edge : edges) {
			if (edge.getEnd().equals(node)) {
				ret.add(edge);
			}
		}
		return ret;
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	public Collection<Node> getNodes(PathPlayer player) {
		if (!findableNodes) {
			return getNodes();
		}
		Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
		if (bukkitPlayer == null) {
			return new ArrayList<>();
		}
		return getNodes().stream()
				.filter(node -> node instanceof Findable)
				.filter(node -> {
					NodeGroup g = node.getGroupKey() == null ? null : getNodeGroup(node);
					return g != null && g.isFindable() || player.hasFound((Findable) node);
				})
				.filter(node -> node.getPermission() == null || bukkitPlayer.hasPermission(node.getPermission()))
				.collect(Collectors.toSet());
	}

	public @Nullable
	Edge getEdge(int aId, int bId) {
		return edges.stream()
				.filter(edge -> edge.getStart().getNodeId() == aId && edge.getEnd().getNodeId() == bId)
				.findAny()
				.orElse(null);
	}

	public int getMaxFoundSize() {
		List<Integer> sizes = groups.values().stream().filter(NodeGroup::isFindable).map(HashSet::size).collect(Collectors.toList());
		sizes.add((int) nodes.values().stream().filter(f -> f.getGroupKey() == null).count());

		int size = 0;
		for (int i : sizes) {
			size += i;
		}
		return size;
	}
}
