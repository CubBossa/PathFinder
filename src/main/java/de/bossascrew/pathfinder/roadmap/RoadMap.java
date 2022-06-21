package de.bossascrew.pathfinder.roadmap;

import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.events.node.*;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.util.HashedRegistry;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.StringUtils;
import de.bossascrew.pathfinder.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

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
	private final Collection<Navigable> navigables;
	private final Collection<Findable> findables;

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
		this.navigables = new HashSet<>();
		this.findables = new HashSet<>();

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
		PathPlugin.getInstance().getDatabase().loadNodes(this).values().forEach(node -> {
			nodes.put(node.getNodeId(), node);
			navigables.add(node);
			if (node instanceof Findable findable) {
				findables.add(findable);
			}
		});
		edges.clear();
		edges.addAll(PathPlugin.getInstance().getDatabase().loadEdges(this));
	}

	public Graph<Node, Edge> toGraph(PlayerNode player) {
		Graph<Node, Edge> graph = new SimpleDirectedWeightedGraph<>(Edge.class);
		nodes.values().forEach(graph::addVertex);
		edges.forEach(e -> graph.addEdge(e.getStart(), e.getEnd(), e));
		edges.forEach(e -> graph.setEdgeWeight(e, e.getWeightedLength()));

		Vector pos = player.getPosition();
		Node bestEdge = nodes.values().stream()
				.map(node -> new AbstractMap.SimpleEntry<>(node, node.getPosition().distance(pos)))
				.sorted(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
				.limit(10)
				.map(e -> {
					Node n = e.getKey();
					Vector dir = n.getPosition().clone().subtract(pos);
					Location loc = n.getLocation();
					loc.setDirection(dir);
					BlockIterator i = new BlockIterator(loc, 1.5, (int) dir.length());
					int count = 1;
					while (i.hasNext()) {
						if (i.next().getType().isSolid()) {
							count++;
						}
					}
					return new AbstractMap.SimpleEntry<>(e.getValue() * count, n);
				})
				.min(Comparator.comparingDouble(AbstractMap.SimpleEntry::getKey))
				.map(AbstractMap.SimpleEntry::getValue)
				.orElse(null);

		if (bestEdge != null) {
			graph.addVertex(player);
			graph.addEdge(player, bestEdge, new Edge(player, bestEdge, 1));
		}

		return graph;
	}

	public NavigateSelection getNavigables() {
		return new NavigateSelection(this, navigables);
	}

	public NavigateSelection getNavigables(String... keywords) {
		return getNavigables(Lists.newArrayList(keywords));
	}

	public NavigateSelection getNavigables(Collection<String> keywords) {
		return getNavigables().stream()
				.filter(node -> {
					for (String keyword : keywords) {
						if (node.getSearchTerms().contains(keyword)) {
							return true;
						}
					}
					return false;
				})
				.collect(Collectors.toCollection(() -> new NavigateSelection(this)));
	}

	public Waypoint createNode(Vector vector) {
		return createNode(vector, null, null);
	}

	public @Nullable
	Waypoint createNode(Vector vector, @Nullable Double bezierTangentLength, String permission) {

		Waypoint node = PathPlugin.getInstance().getDatabase().createNode(this, RoadMapHandler.WAYPOINT_TYPE, null,
				vector.getX(), vector.getY(), vector.getZ(), bezierTangentLength, permission);

		addNode(node);
		Bukkit.getPluginManager().callEvent(new NodeCreatedEvent(node));

		return node;
	}

	/**
	 * This method changes the position of the node and calls the corresponding event.
	 * If the event is not cancelled, the change will be updated to the database.
	 * Don't call this method asynchronous, events can only be called in the main thread.
	 * <p>
	 * TO only modify the position without event or database update, simply call {@link Node#setPosition(Vector)}
	 *
	 * @param node     The node to change the position for.
	 * @param position The position to set. No world attribute is required, the roadmap attribute is used. Use {@link Location#toVector()}
	 *                 to set a location.
	 * @return true if the position was successfully set, false if the event was cancelled
	 */
	public boolean setNodeLocation(Node node, Vector position) {

		NodeTeleportEvent event = new NodeTeleportEvent(node, position);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}
		node.setPosition(event.getNewPositionModified());
		PathPlugin.getInstance().getDatabase().updateNode(node);
		return true;
	}

	public boolean setGroupFindable(NodeGroup group, boolean findable) {
		//TODO implement. Refreshes the findable collection.
		return true;
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
		navigables.add(node);
		if (node instanceof Findable findable) {
			findables.add(findable);
		}
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
	NodeGroup getNodeGroup(NamespacedKey key) {
		if (key == null) {
			return null;
		}
		return groups.get(key);
	}

	public void removeNodeGroup(NodeGroup group) {
		groups.remove(group.getKey());
		navigables.remove(group);
		findables.remove(group);
		nodes.values().stream()
				.filter(node -> node instanceof Groupable)
				.map(node -> (Groupable) node)
				.forEach(node -> node.removeGroup(group));
	}

	public NodeGroup createNodeGroup(NamespacedKey key, boolean findable) {

		NodeGroup group = PathPlugin.getInstance().getDatabase().createNodeGroup(this, key, StringUtils.getRandHexString() + "A Group", findable);
		groups.put(group);
		navigables.add(group);
		if (group.isFindable()) {
			findables.add(group);
		}
		return group;
	}


	public Edge getEdge(Node start, Node end) {
		return edges.stream().filter(edge -> edge.getStart().equals(start) && edge.getEnd().equals(end)).findFirst().orElse(null);
	}

	/**
	 * erstellt neue Edge in der Datenbank
	 */
	public Edge connectNodes(Node start, Node end) {
		return connectNodes(start, end, false, 1, 1);
	}

	/**
	 * Connects two nodes with an edge. Edges are stored directed, therefore it must be stated if the new node should
	 * only be from start to end or also from end to start.
	 * <p>
	 * This method calls the corresponding {@link EdgesCreatedEvent}. This will always be called sync, so that the
	 * method can be called async.
	 *
	 * @param start      The node to start the edge from.
	 * @param end        The node to end the edge at.
	 * @param directed   If another edge should be created from end to start. Prefer this method against calling it twice,
	 *                   as the edit mode particle setup has to be recalculated for each edge change.
	 * @param weight     The weight modifier for this edge. This will be taken into account when calculating the shortest path.
	 *                   The actual length of the edge will be multiplied with the modifier, so when players have to crouch along some
	 *                   edges of your roadmap, you might want to change the modifier to 2.
	 * @param weightBack Same as weight, but for the directed edge in the opposite direction.
	 * @return the created edge from start to end.
	 */
	public Edge connectNodes(Node start, Node end, boolean directed, float weight, float weightBack) {
		if (start.equals(end)) {
			throw new IllegalArgumentException("Cannot connect node with itself.");
		}
		Edge edge = PathPlugin.getInstance().getDatabase().createEdge(start, end, weight);

		start.getEdges().add(edge);
		edges.add(edge);

		Edge other = edge;
		if (!directed) {
			Edge existing = getEdge(end, start);
			if (existing != null) {
				other = PathPlugin.getInstance().getDatabase().createEdge(end, start, weightBack);
				end.getEdges().add(other);
				edges.add(edge);
			}
		}

		Edge finalOther = other;
		Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(new EdgesCreatedEvent(edge, finalOther)));

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

	public Collection<Node> getFoundFindables(PathPlayer player) {
		if (!findableNodes) {
			return getNodes();
		}
		return findables.stream()
				.filter(player::hasFound)
				.flatMap(findable -> findable.getGroup().stream())
				.collect(Collectors.toList());
	}

	public @Nullable
	Edge getEdge(int aId, int bId) {
		return edges.stream()
				.filter(edge -> edge.getStart().getNodeId() == aId && edge.getEnd().getNodeId() == bId)
				.findAny()
				.orElse(null);
	}

	public int getMaxFoundSize() {
		return findables.size();
	}
}
