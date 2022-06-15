package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.node.Edge;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.cubbossa.menuframework.util.Pair;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class RoadMap {

	private final int roadmapId;
	private String nameFormat;
	private Component displayName;
	private World world;

	private boolean findableNodes;
	private double nodeFindDistance;
	private double defaultBezierTangentLength;

	private final Map<Integer, Node> nodes;
	private final Collection<Edge> edges;
	private final Map<Integer, NodeGroup> groups;

	private PathVisualizer pathVisualizer;
	private EditModeVisualizer editModeVisualizer;


	public RoadMap(int databaseId, String name, World world, boolean findableNodes, PathVisualizer pathVisualizer,
				   EditModeVisualizer editModeVisualizer, double nodeFindDistance, double defaultBezierTangentLength) {

		this.roadmapId = databaseId;
		this.nameFormat = name;
		this.world = world;
		this.findableNodes = findableNodes;
		this.nodeFindDistance = nodeFindDistance;
		this.defaultBezierTangentLength = defaultBezierTangentLength;

		this.groups = SqlStorage.getInstance().loadFindableGroups(this);
		this.nodes = new TreeMap<>();
		this.nodes.putAll(SqlStorage.getInstance().loadFindables(this));
		this.edges = loadEdgesFromIds(PathPlugin.getInstance().getDatabase().loadEdges(this));

		setPathVisualizer(pathVisualizer);
		setEditModeVisualizer(editModeVisualizer);
	}

	public Graph<Node, Edge> toGraph() {
		return new DefaultDirectedGraph<>(Edge.class);
	}

	//TODO auslagern
	public Waypoint createNode(Vector vector, String name) {
		return createNode(vector, name, null, null);
	}

	public Waypoint createNode(Vector vector, String name, @Nullable Double bezierTangentLength, String permission) {
		Waypoint node = (Waypoint) SqlStorage.getInstance().newFindable(this, Waypoint.SCOPE, null,
				vector.getX(), vector.getY(), vector.getZ(), name, bezierTangentLength, permission);
		if (node != null) {
			addNode(node);
		}
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
		for (int edge : new ArrayList<>(findable.getEdges())) {
			Waypoint target = getNode(edge);
			if (target == null) {
				continue;
			}
			disconnectNodes(findable, target);
		}
		SqlStorage.getInstance().deleteFindable(findable.getNodeId());
		nodes.remove(findable.getNodeId());

		if (isEdited()) {
			updateEditModeParticles();
			editModeNodeArmorStands.get(findable).remove();
			editModeNodeArmorStands.remove(findable);
		}
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
		return getNodeGroup(node.getGroupId());
	}

	public @Nullable
	NodeGroup getNodeGroup(Integer groupId) {
		if (groupId == null) {
			return null;
		}
		for (NodeGroup group : groups.values()) {
			if (group.getGroupId() == groupId) {
				return group;
			}
		}
		return null;
	}

	public void removeNodeGroup(NodeGroup nodeGroup) {
		this.groups.remove(nodeGroup.getGroupId());
	}

	public @Nullable
	NodeGroup createNodeGroup(String name, boolean findable) {

		NodeGroup group = SqlStorage.getInstance().newFindableGroup(this, name, findable);
		if (group == null) {
			return null;
		}
		groups.put(group.getGroupId(), group);
		return group;
	}


	/**
	 * erstellt neue Edge in der Datenbank
	 */
	public void connectNodes(Node a, Node b) {
		if (a.equals(b)) {
			return;
		}
		SqlStorage.getInstance().newEdge(a, b);
		a.getEdges().add(b.getNodeId());
		b.getEdges().add(a.getNodeId());
		Pair<Waypoint, Waypoint> edge = new Pair<>(a, b);
		edges.add(edge);

		if (isEdited()) {
			updateEditModeParticles();
			this.editModeEdgeArmorStands.put(edge, getEdgeArmorStand(edge));
		}
	}

	public Edge getEdge(Node start, Node end) {
		return edges.stream().filter(edge -> edge.getStart().equals(start) && edge.getEnd().equals(end)).findFirst().orElse(null);
	}

	public void disconnectNodes(Node a, Node b) {
		disconnectNodes(getEdge(a, b));
	}

	public void disconnectNode(Waypoint f) {
		for (int edge : new HashSet<>(f.getEdges())) {
			disconnectNodes(f, getNode(edge));
		}
	}

	public void disconnectNodes(Edge edge) {
		Node a = edge.getStart();
		Node b = edge.getEnd();

		if (a.equals(b)) {
			return;
		}
		a.getEdges().remove((Integer) b.getNodeId());
		b.getEdges().remove((Integer) a.getNodeId());

		edges.remove(edge);
		if (isEdited()) {
			updateEditModeParticles();
			ArmorStand edgeArmorStand = editModeEdgeArmorStands.get(edge);
			if (edgeArmorStand != null) {
				edgeArmorStand.remove();
				editModeEdgeArmorStands.remove(edge);
			}
		}
	}

	private Collection<Pair<Waypoint, Waypoint>> loadEdgesFromIds(Collection<Pair<Integer, Integer>> edgesById) {
		Collection<Pair<Waypoint, Waypoint>> result = new ArrayList<>();
		for (Pair<Integer, Integer> pair : edgesById) {
			Waypoint a = getFindable(pair.first);
			Waypoint b = getFindable(pair.second);

			if (a == null || b == null) {
				continue;
			}
			a.getEdges().add(b.getNodeId());
			b.getEdges().add(a.getNodeId());

			result.add(new Pair<>(a, b));
		}
		return result;
	}

/*TODO weg hier
	public void delete() {
		cancelEditModes();
		for (UUID uuid : editingPlayers.keySet()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Die Straßenkarte, die du gerade bearbeitet hast, wurde gelöscht.");
		}

		for (PathPlayer player : PathPlayerHandler.getInstance().getPlayers()) {
			player.deselectRoadMap(this.getRoadmapId());
			player.cancelPath(this);
		}
		SqlStorage.getInstance().deleteRoadMap(this);
	}
*/






	public void setPathVisualizer(PathVisualizer pathVisualizer) {
		if (this.pathVisualizer != null) {
			this.pathVisualizer.getUpdateParticle().unsubscribe(this.getRoadmapId());
		}
		this.pathVisualizer = pathVisualizer;
		updateData();

		this.pathVisualizer.getUpdateParticle().subscribe(this.getRoadmapId(), integer -> this.updateActivePaths());
		updateActivePaths();
	}

	public void updateActivePaths() {
		if (PathPlayerHandler.getInstance() == null) {
			return;
		}
		//Jeder spieler kann pro Roadmap nur einen aktiven Pfad haben, weshalb man PathPlayer auf ParticlePath mappen kann
		PathPlayerHandler.getInstance().getPlayers().stream()
				.map(player -> player.getActivePaths().stream()
						.filter(particlePath -> particlePath.getRoadMap().getRoadmapId() == this.getRoadmapId())
						.findFirst().orElse(null))
				.filter(Objects::nonNull)
				.forEach(ParticlePath::run);
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
				.filter(node -> node.getGroupId() != NodeGroup.NO_GROUP && !getNodeGroup(node.getGroupId()).isFindable() || player.hasFound(node))
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
		List<Integer> sizes = groups.values().stream().filter(NodeGroup::isFindable).map(g -> g.getNodes().size()).collect(Collectors.toList());
		sizes.add((int) nodes.values().stream().filter(f -> f.getGroupId() == NodeGroup.NO_GROUP).count());

		int size = 0;
		for (int i : sizes) {
			size += i;
		}
		return size;
	}
}
