package de.bossascrew.pathfinder;

import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.inventory.EditmodeUtils;
import de.bossascrew.pathfinder.inventory.HotbarMenu;
import de.bossascrew.pathfinder.util.PathTask;
import de.bossascrew.pathfinder.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.visualisation.PathVisualizer;
import jdk.internal.net.http.common.Pair;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Eine Straßenkarte, die verschiedene Wegpunkte enthält und
 * durch die mithifle des AStar Algorithmus ein Pfad gefunden werden kann.
 */
@Getter
public class RoadMap {

    private static final Vector ARMORSTAND_OFFSET = new Vector(0, -1.9, 0);

    private int databaseId;
    private String name;
    private World world;
    private boolean findableNodes = false;

    private Collection<Node> nodes;
    private final Collection<Pair<Node, Node>> edges;
    private final Collection<NodeGroup> groups;
    private final Map<UUID, HotbarMenu> editingPlayers;

    private PathVisualizer visualizer;
    private EditModeVisualizer editModeVisualizer;
    private double nodeFindDistance;
    private double defaultBezierTangentLength;

    private final Map<Node, ArmorStand> editModeNodeArmorStands;
    private final Map<Pair<Node, Node>, ArmorStand> editModeEdgeArmorStands;
    private PathTask editModeTask = null;

    public RoadMap(int databaseId, String name, World world, boolean findableNodes, PathVisualizer pathVisualizer,
                   EditModeVisualizer editModeVisualizer, double nodeFindDistance, double defaultBezierTangentLength) {
        this.databaseId = databaseId;
        this.name = name;
        this.world = world;
        this.findableNodes = findableNodes;
        this.visualizer = pathVisualizer;
        this.editModeVisualizer = editModeVisualizer;
        this.nodeFindDistance = nodeFindDistance;
        this.defaultBezierTangentLength = defaultBezierTangentLength;

        this.nodes = new ArrayList<Node>();
        this.edges = loadEdgesFromIds(Objects.requireNonNull(DatabaseModel.getInstance().loadEdges(this)));
        groups = new ArrayList<NodeGroup>();

        this.editingPlayers = new HashMap<UUID, HotbarMenu>();
        this.editModeNodeArmorStands = new ConcurrentHashMap<Node, ArmorStand>();
        this.editModeEdgeArmorStands = new ConcurrentHashMap<Pair<Node, Node>, ArmorStand>();
    }

    public void setName(String name) {
        if (RoadMapHandler.getInstance().isNameUnique(name)) {
            this.name = name;
        }
        updateData();
    }

    public boolean isNodeNameUnique(String name) {
        return nodes.stream().map(Node::getName).anyMatch(context -> context.equalsIgnoreCase("name"));
    }

    public void deleteNode(int nodeId) {
        deleteNode(getNode(nodeId));
    }

    public void deleteNode(Node node) {
        DatabaseModel.getInstance().deleteNode(node.getDatabaseId());
        nodes.remove(node);
    }

    public void createNode(Vector vector, String name) {
        createNode(vector, name, defaultBezierTangentLength, "none");
    }

    public void createNode(Vector vector, String name, double bezierTangentLength, String permission) {
        Node node = DatabaseModel.getInstance().newNode(databaseId, Node.NO_GROUP_ID, vector, name, bezierTangentLength, permission);
        if (node != null) {
            addNode(node);
        }
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void setNodes(Collection<Node> nodes) {
        this.nodes = nodes;
    }

    public void addNodes(Collection<Node> nodes) {
        this.nodes.addAll(nodes);
    }

    public @Nullable
    Node getNode(String name) {
        for (Node node : nodes) {
            if (node.getName().equalsIgnoreCase(name)) {
                return node;
            }
        }
        return null;
    }

    public @Nullable
    Node getNode(int nodeId) {
        for (Node node : nodes) {
            if (node.getDatabaseId() == nodeId) {
                return node;
            }
        }
        return null;
    }

    public @Nullable
    NodeGroup getNodeGroup(String name) {
        for (NodeGroup nodeGroup : groups) {
            if (nodeGroup.getName().equalsIgnoreCase(name)) {
                return nodeGroup;
            }
        }
        return null;
    }

    public @Nullable
    NodeGroup getNodeGroup(Node node) {
        return getNodeGroup(node.getNodeGroupId());
    }

    public @Nullable
    NodeGroup getNodeGroup(int groupId) {
        for (NodeGroup nodeGroup : groups) {
            if (nodeGroup.getDatabaseId() == groupId) {
                return nodeGroup;
            }
        }
        return null;
    }

    public void deleteNodeGroup(int nodeGroupId) {
        //TODO database gruppe löschen
        DatabaseModel.getInstance();
    }

    public @Nullable
    NodeGroup addNodeGroup(String name) {
        if (isGroupNameUnique(name)) {
            DatabaseModel.getInstance();
            //TODO neue Gruppe im DatabaseModel erstellen und laden.
        }
        return null;
    }

    public boolean isGroupNameUnique(String name) {
        for (NodeGroup group : groups) {
            if (group.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * erstellt neue Edge in der Datenbank
     */
    public void connectNodes(Node a, Node b) {
        DatabaseModel.getInstance().newEdge(a, b);
        a.getEdges().add(b.getDatabaseId());
        b.getEdges().add(a.getDatabaseId());
        edges.add(new Pair<>(a, b));
    }

    private Collection<Pair<Node, Node>> loadEdgesFromIds(Collection<Pair<Integer, Integer>> edgesById) {
        Collection<Pair<Node, Node>> result = new ArrayList<>();
        for (Pair<Integer, Integer> pair : edgesById) {
            Node a = getNode(pair.first);
            Node b = getNode(pair.second);

            if (a == null || b == null) {
                continue;
            }
            a.getEdges().add(b.getDatabaseId());
            b.getEdges().add(a.getDatabaseId());

            Pair<Node, Node> newPair = new Pair<Node, Node>(a, b);
            result.add(newPair);
        }
        return result;
    }

    public void delete() {
        for (UUID uuid : editingPlayers.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            assert p != null;
            PlayerUtils.sendMessage(p, PathPlugin.PREFIX + ChatColor.RED + "Die Straßenkarte, die du gerade bearbeitet hast, wurde gelöscht.");
            setEditMode(p.getUniqueId(), false);
            //TODO gucken ob probleme weil bearbeiten während for-schleife
        }

        //TODO evntl. lösche alle visualisierungen

        DatabaseModel.getInstance().deleteRoadMap(this);
    }

    /**
     * @return true sobald ein Spieler gerade den Editmode aktiv hat
     */
    public boolean isEdited() {
        return !editingPlayers.isEmpty();
    }

    public void toggleEditMode(UUID uuid) {
        setEditMode(uuid, !isEditing(uuid));
    }

    public void cancelEditModes() {
        for (UUID uuid : editingPlayers.keySet()) {
            setEditMode(uuid, false);
        }
    }

    public void setEditMode(UUID uuid, boolean editing) {
        Player player = Bukkit.getPlayer(uuid);
        PathPlayer editor = PathPlayerHandler.getInstance().getPlayer(uuid);
        assert editor != null;

        if (editing) {
            assert player != null;
            if (!isEdited()) {
                startEditModeVisualizer();
            }

            editor.setEditMode(databaseId);
            HotbarMenu menu = EditmodeUtils.getNewMenu();
            editingPlayers.put(uuid, menu);
            //menu.openInventory(player);
        } else {
            editor.clearEditMode();
            editingPlayers.remove(uuid);

            if (isEdited()) {
                stopEditModeVisualizer();
            }
            if (player != null) {
                //editingPlayers.get(uuid).handleInventoryClose(player);
            }
        }
    }

    public boolean isEditing(UUID uuid) {
        return editingPlayers.containsKey(uuid);
    }

    public boolean isEditing(Player player) {
        return isEditing(player.getUniqueId());
    }

    public void updateArmorStandPosition(Node node) {
        ArmorStand as = editModeNodeArmorStands.get(node);
        assert as != null;
        as.teleport(node.getVector().toLocation(world).add(ARMORSTAND_OFFSET));

        for (Pair<Node, Node> edge : getEdges(node)) {
            ArmorStand asEdge = editModeEdgeArmorStands.get(edge);
            assert asEdge != null;
            asEdge.teleport(getEdgeCenter(edge).add(ARMORSTAND_OFFSET));
        }
    }

    public void startEditModeVisualizer() {

        for (Node node : nodes) {
            Location nodeLocation = node.getVector().toLocation(world).add(ARMORSTAND_OFFSET);
            ArmorStand nodeArmorStand = EditmodeUtils.getNewArmorStand(nodeLocation, node.getName(),
                    editModeVisualizer.getNodeHeadId());
            editModeNodeArmorStands.put(node, nodeArmorStand);
        }
        for (Pair<Node, Node> edge : edges) {
            Location center = getEdgeCenter(edge).add(ARMORSTAND_OFFSET);
            ArmorStand edgeArmorStand = EditmodeUtils.getNewArmorStand(center, null,
                    editModeVisualizer.getEdgeHeadId());
            editModeEdgeArmorStands.put(edge, edgeArmorStand);
        }
        assert editModeTask.isCancelled();
        editModeTask = (PathTask) Bukkit.getScheduler().runTaskTimerAsynchronously(PathPlugin.getInstance(), () -> {

            List<Player> players = new ArrayList<>();
            for (UUID uuid : editingPlayers.keySet()) {
                players.add(Bukkit.getPlayer(uuid));
            }
            int particlesSpawned = 0;
            for (Pair<Node, Node> edge : edges) {
                Location a = edge.first.getVector().toLocation(world);
                Location b = edge.second.getVector().toLocation(world);

                double dist = a.distance(b);
                double step = editModeVisualizer.getParticleDistance();
                @NotNull Location dir = b.clone().subtract(a);
                for (double i = step; i < dist; i += step) {
                    for (Player player : players) {
                        Location particleLocation = a.clone().add(dir.clone().multiply(i));
                        if (player == null || particlesSpawned >= editModeVisualizer.getParticleLimit()) {
                            continue;
                        }
                        if (player.getLocation().distanceSquared(particleLocation) >
                                editModeVisualizer.getParticleDistanceSquared()) {
                            continue;
                        }

                        player.spawnParticle(
                                editModeVisualizer.getParticle(),
                                particleLocation, 1);
                        particlesSpawned++;
                    }
                }
            }
        }, editModeVisualizer.getSchedulerStartDelay(), editModeVisualizer.getSchedulerPeriod());
    }

    public void stopEditModeVisualizer() {
        for (ArmorStand armorStand : editModeNodeArmorStands.values()) {
            armorStand.remove();
        }
        editModeNodeArmorStands.clear();
        Bukkit.getScheduler().cancelTask(editModeTask.getTaskId());
    }

    public void setNodeFindDistance(double nodeFindDistance) {
        this.nodeFindDistance = nodeFindDistance;
        updateData();
    }

    public void setFindableNodes(boolean findableNodes) {
        this.findableNodes = findableNodes;
        updateData();
    }

    public void setVisualizer(EditModeVisualizer visualizer) {
        this.editModeVisualizer = visualizer;
        updateData();
    }

    public void setVisualizer(PathVisualizer visualizer) {
        this.visualizer = visualizer;
        updateData();
        //TODO alle aktuellen paths updaten.
    }

    public void setDefaultBezierTangentLength(double length) {
        this.defaultBezierTangentLength = length;
        updateData();
    }

    public void setWorld(World world) {
        assert world != null;
        this.world = world;
        updateData();
    }

    private void updateData() {
        PluginUtils.getInstance().runAsync(() -> {
            DatabaseModel.getInstance().updateRoadMap(this);
        });
    }

    private Location getEdgeCenter(Pair<Node, Node> edge) {
        Node a = edge.first;
        Node b = edge.second;
        assert a != null && b != null;
        Vector va = a.getVector().clone();
        Vector vb = b.getVector().clone();
        return va.add(vb.subtract(va)).toLocation(world);
    }

    private Collection<Pair<Node, Node>> getEdges(Node node) {
        Collection<Pair<Node, Node>> ret = new ArrayList<>();
        for (Pair<Node, Node> edge : edges) {
            if (edge.first.equals(node) || edge.second.equals(node)) {
                ret.add(edge);
            }
        }
        return ret;
    }

    public Collection<Node> getFindableNodes(PathPlayer player) {
        return getNodes().stream()
                .filter(node -> !player.hasFound(node.getDatabaseId()))
                .collect(Collectors.toSet());
    }
}
