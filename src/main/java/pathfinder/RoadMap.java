package pathfinder;

import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.PluginUtils;
import jdk.internal.net.http.common.Pair;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import pathfinder.data.DatabaseModel;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;
import pathfinder.inventory.EditmodeUtils;
import pathfinder.inventory.HotbarMenu;
import pathfinder.inventory.HotbarMenuHandler;
import pathfinder.util.PathTask;
import pathfinder.visualisation.EditModeVisualizer;
import pathfinder.visualisation.PathVisualizer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Eine Straßenkarte, die verschiedene Wegpunkte enthält und
 * durch die mithifle des AStar Algorithmus ein Pfad gefunden werden kann.
 */
@Getter
public class RoadMap {

    private int databaseId;
    private String name;
    @Setter
    private World world;
    private boolean findableNodes = false;

    private Collection<Node> nodes;
    private Collection<Pair<Node, Node>> edges;
    private Collection<NodeGroup> groups;
    private Map<UUID, HotbarMenu> editingPlayers;

    private PathVisualizer visualizer;
    private EditModeVisualizer editModeVisualizer;
    private double nodeFindDistance;
    @Setter
    private double defaultBezierTangentLength = 3;

    private Collection<ArmorStand> editModeArmorStands;
    private PathTask editModeTask = null;

    public RoadMap(String name, World world, boolean findableNodes) {
        setName(name);
        this.world = world;
        this.findableNodes = findableNodes;

        this.nodes = new ArrayList<Node>();
        this.edges = loadEdgesFromIds(Objects.requireNonNull(DatabaseModel.getInstance().loadEdges(this)));
        groups = new ArrayList<NodeGroup>();

        this.editingPlayers = new HashMap<UUID, HotbarMenu>();
        this.editModeArmorStands = new ArrayList<ArmorStand>();
    }

    public void setName(String name) {
        if(RoadMapHandler.getInstance().isNameUnique(name)) {
            this.name = name;
        }
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
        if(node != null) addNode(node);
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
        for(Node node : nodes) {
            if(node.getName().equalsIgnoreCase(name))
                return node;
        }
        return null;
    }

    public @Nullable
    Node getNode(int nodeId) {
        for(Node node : nodes) {
            if(node.getDatabaseId() == nodeId) return node;
        }
        return null;
    }

    public @Nullable
    NodeGroup getNodeGroup(String name) {
        for(NodeGroup nodeGroup : groups) {
            if(nodeGroup.getName().equalsIgnoreCase(name))
                return nodeGroup;
        }
        return null;
    }

    public @Nullable
    NodeGroup getNodeGroup(Node node) {
        return getNodeGroup(node.getNodeGroupId());
    }

    public @Nullable
    NodeGroup getNodeGroup(int groupId) {
        for(NodeGroup nodeGroup : groups) {
            if(nodeGroup.getDatabaseId() == groupId) {
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
        if(isGroupNameUnique(name)) {
            DatabaseModel.getInstance();
            //TODO neue Gruppe im DatabaseModel erstellen und laden.
        }
        return null;
    }

    public boolean isGroupNameUnique(String name) {
        for(NodeGroup group : groups) {
            if(group.getName().equals(name)) return false;
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
        for(Pair<Integer, Integer> pair : edgesById) {
            Node a = getNode(pair.first);
            Node b = getNode(pair.second);

            if(a == null || b == null) continue;
            a.getEdges().add(b.getDatabaseId());
            b.getEdges().add(a.getDatabaseId());

            Pair<Node, Node> newPair = new Pair<Node, Node>(a, b);
            result.add(newPair);
        }
        return result;
    }

    public void delete() {
        for(UUID uuid : editingPlayers.keySet()) {
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
        for(UUID uuid : editingPlayers.keySet()) {
            setEditMode(uuid, false);
        }
    }

    public void setEditMode(UUID uuid, boolean editing) {
        Player player = Bukkit.getPlayer(uuid);
        PathPlayer editor = PlayerHandler.getInstance().getPlayer(uuid);
        assert editor != null;

        if(editing) {
            assert player != null;
            if(!isEdited()) startEditModeVisualizer();

            editor.setEditMode(databaseId);
            HotbarMenu menu = EditmodeUtils.getNewMenu();
            editingPlayers.put(uuid, menu);
            menu.openInventory(player);
        } else {
            editor.clearEditMode();
            editingPlayers.remove(uuid);

            if(isEdited()) stopEditModeVisualizer();
            if(player != null) {
                editingPlayers.get(uuid).handleInventoryClose(player);
            }
        }
    }

    public boolean isEditing(UUID uuid) {
        return editingPlayers.containsKey(uuid);
    }

    public boolean isEditing(Player player) {
        return isEditing(player.getUniqueId());
    }

    public void startEditModeVisualizer() {

        for(Node node : nodes) {
            Location nodeLocation = node.getVector().toLocation(world);
            ArmorStand nodeArmorStand = EditmodeUtils.getNewArmorStand(nodeLocation, node.getName(),
                    editModeVisualizer.getNodeHeadId());
            editModeArmorStands.add(nodeArmorStand);
        }
        for(Pair<Node, Node> edge : edges) {
            Vector a = edge.first.getVector().clone();
            Vector b = edge.second.getVector().clone();
            Location center = a.add(b.subtract(a)).toLocation(world);
            ArmorStand edgeArmorStand = EditmodeUtils.getNewArmorStand(center, null,
                    editModeVisualizer.getEdgeHeadId());
            editModeArmorStands.add(edgeArmorStand);
        }
        assert editModeTask.isCancelled();
        editModeTask = (PathTask) Bukkit.getScheduler().runTaskTimerAsynchronously(PathPlugin.getInstance(), () -> {

            List<Player> players = new ArrayList<>();
            for(UUID uuid : editingPlayers.keySet()) {
                players.add(Bukkit.getPlayer(uuid));
            }
            int particlesSpawned = 0;
            for(Pair<Node, Node> edge : edges) {
                Location a = edge.first.getVector().toLocation(world);
                Location b = edge.second.getVector().toLocation(world);

                double dist = a.distance(b);
                double step = editModeVisualizer.getParticleDistance();
                @NotNull Location dir = b.clone().subtract(a);
                for(double i = step; i < dist; i += step){
                    for(Player player : players) {
                        Location particleLocation = a.clone().add(dir.clone().multiply(i));
                        if(player == null || particlesSpawned >= editModeVisualizer.getParticleLimit()) continue;
                        if(player.getLocation().distanceSquared(particleLocation) >
                                editModeVisualizer.getParticleDistanceSquared()) continue;

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
        for(ArmorStand armorStand : editModeArmorStands) {
            armorStand.remove();
        }
        editModeArmorStands.clear();
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

    private void updateData() {
        PluginUtils.getInstance().runAsync(() -> {
            DatabaseModel.getInstance().updateRoadMap(this);
        });
    }
}
