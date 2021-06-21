package de.bossascrew.pathfinder.data;

import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.HotbarMenu;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.inventory.EditmodeUtils;
import de.bossascrew.pathfinder.util.Pair;
import de.bossascrew.pathfinder.util.PathTask;
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

    private final int databaseId;
    private String name;
    private World world;
    private boolean findableNodes;

    private Collection<Findable> findables;
    private final Collection<Pair<Findable, Findable>> edges;
    private final Collection<FindableGroup> groups;
    private final Map<UUID, HotbarMenu> editingPlayers;

    private PathVisualizer visualizer;
    private EditModeVisualizer editModeVisualizer;
    private double nodeFindDistance;
    private double defaultBezierTangentLength;

    private final Map<Findable, ArmorStand> editModeNodeArmorStands;
    private final Map<Pair<Findable, Findable>, ArmorStand> editModeEdgeArmorStands;
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

        this.findables = new ArrayList<Findable>(); //TODO aus Datenbank laden
        this.edges = loadEdgesFromIds(Objects.requireNonNull(DatabaseModel.getInstance().loadEdges(this)));
        groups = new ArrayList<FindableGroup>(); //TODO aus datenbank laden

        this.editingPlayers = new HashMap<UUID, HotbarMenu>();
        this.editModeNodeArmorStands = new ConcurrentHashMap<Findable, ArmorStand>();
        this.editModeEdgeArmorStands = new ConcurrentHashMap<Pair<Findable, Findable>, ArmorStand>();
    }

    public void setName(String name) {
        if (RoadMapHandler.getInstance().isNameUnique(name)) {
            this.name = name;
        }
        updateData();
    }

    public boolean isNodeNameUnique(String name) {
        return findables.stream().map(Findable::getName).anyMatch(context -> context.equalsIgnoreCase("name"));
    }

    public void deleteFindable(int findableId) {
        deleteFindable(Objects.requireNonNull(getFindable(findableId)));
    }

    public void deleteFindable(Findable findable) {
        DatabaseModel.getInstance().deleteFindable(findable.getDatabaseId());
        findables.remove(findable);
    }

    public void createNode(Vector vector, String name) {
        createNode(vector, name, defaultBezierTangentLength, "none");
    }

    public void createNode(Vector vector, String name, double bezierTangentLength, String permission) {
        Node node = DatabaseModel.getInstance().newNode(databaseId, Node.NO_GROUP_ID, vector, name, bezierTangentLength, permission);
        if (node != null) {
            addFindable(node);
        }
    }

    public void addFindable(Findable findable) {
        findables.add(findable);
    }

    public void setFindables(Collection<Findable> findables) {
        this.findables = findables;
    }

    public void addFindables(Collection<Findable> findables) {
        this.findables.addAll(findables);
    }

    public @Nullable
    Findable getFindable(String name) {
        for (Findable findable : findables) {
            if (findable.getName().equalsIgnoreCase(name)) {
                return findable;
            }
        }
        return null;
    }

    public @Nullable
    Findable getFindable(int findableId) {
        for (Findable findable : findables) {
            if (findable.getDatabaseId() == findableId) {
                return findable;
            }
        }
        return null;
    }

    public @Nullable
    FindableGroup getFindableGroup(String name) {
        for (FindableGroup nodeGroup : groups) {
            if (nodeGroup.getName().equalsIgnoreCase(name)) {
                return nodeGroup;
            }
        }
        return null;
    }

    public @Nullable
    FindableGroup getFindableGroup(Findable findable) {
        return getFindableGroup(findable.getNodeGroupId());
    }

    public @Nullable
    FindableGroup getFindableGroup(int groupId) {
        for (FindableGroup nodeGroup : groups) {
            if (nodeGroup.getDatabaseId() == groupId) {
                return nodeGroup;
            }
        }
        return null;
    }

    public void deleteFindableGroup(int nodeGroupId) {
        //TODO database gruppe löschen
        DatabaseModel.getInstance();
    }

    public @Nullable
    FindableGroup addFindableGroup(String name) {
        if (isGroupNameUnique(name)) {
            DatabaseModel.getInstance();
            //TODO neue Gruppe im DatabaseModel erstellen und laden.
        }
        return null;
    }

    public boolean isGroupNameUnique(String name) {
        for (FindableGroup group : groups) {
            if (group.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * erstellt neue Edge in der Datenbank
     */
    public void connectNodes(Findable a, Findable b) {
        DatabaseModel.getInstance().newEdge(a, b);
        a.getEdges().add(b.getDatabaseId());
        b.getEdges().add(a.getDatabaseId());
        edges.add(new Pair<>(a, b));
    }

    private Collection<Pair<Findable, Findable>> loadEdgesFromIds(Collection<Pair<Integer, Integer>> edgesById) {
        Collection<Pair<Findable, Findable>> result = new ArrayList<>();
        for (Pair<Integer, Integer> pair : edgesById) {
            Findable a = getFindable(pair.first);
            Findable b = getFindable(pair.second);

            if (a == null || b == null) {
                continue;
            }
            a.getEdges().add(b.getDatabaseId());
            b.getEdges().add(a.getDatabaseId());

            result.add(new Pair<>(a, b));
        }
        return result;
    }

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
            player.deselectRoadMap(getDatabaseId());
            player.cancelPath(this);
        }

        //TODO lösche alle visualisierungen
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

    /**
     * Setzt den Bearbeitungsmodus für einen Spieler, wobei auch Hotbarmenü etc gesetzt werden => nicht threadsafe
     *
     * @param uuid    des Spielers, dessen Modus gesetzt wird
     * @param editing ob der Modus aktiviert oder deaktiviert wird
     */
    public void setEditMode(UUID uuid, boolean editing) {
        Player player = Bukkit.getPlayer(uuid);
        PathPlayer editor = PathPlayerHandler.getInstance().getPlayer(uuid);
        if (editor == null) {
            return;
        }

        if (editing) {
            if (player == null) {
                return;
            }
            if (!isEdited()) {
                startEditModeVisualizer();
            }

            editor.setEditMode(databaseId);
            HotbarMenu menu = getHotbarMenu();
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

    private HotbarMenu getHotbarMenu() { //TODO
        //wegpunkt werkzeug: rechtsklick setzen, linksklick löschen
        //kantenwerkzeug: rechtsklick kante aufspannen, linksklick alle kanten eines nodes löschen. Linksklick auf kante = löschen
        //kompass: tp zum nächsten Node
        //Slimeball: Rundung der Tangenten einstellen
        //nametag: Permissionnode setzen
        //Kiste: GruppenGUI: erstes item barriere = keine gruppe. dann alle gruppen als nametags. unten rechts emerald für neue gruppe.
        //rechtsklick auf gruppe = zuweisen. Linksklick mit Confirm = gruppe löschen.
        //Gruppenicons haben in der Lore eine Liste aller Nodes, die Teil der Gruppe sind.
        return new HotbarMenu();
    }

    public boolean isEditing(UUID uuid) {
        return editingPlayers.containsKey(uuid);
    }

    public boolean isEditing(Player player) {
        return isEditing(player.getUniqueId());
    }

    public void updateArmorStandPosition(Findable findable) {
        ArmorStand as = editModeNodeArmorStands.get(findable);
        if (as == null) {
            return;
        }
        as.teleport(findable.getVector().toLocation(world).add(ARMORSTAND_OFFSET));

        for (Pair<Findable, Findable> edge : getEdges(findable)) {
            ArmorStand asEdge = editModeEdgeArmorStands.get(edge);
            if (asEdge == null) {
                return;
            }
            asEdge.teleport(getEdgeCenter(edge).add(ARMORSTAND_OFFSET));
        }
    }

    public void startEditModeVisualizer() {

        for (Findable findable : findables) {
            Location nodeLocation = findable.getLocation().add(ARMORSTAND_OFFSET);
            ArmorStand nodeArmorStand = EditmodeUtils.getNewArmorStand(nodeLocation, findable.getName(),
                    editModeVisualizer.getNodeHeadId());
            editModeNodeArmorStands.put(findable, nodeArmorStand);
        }
        for (Pair<Findable, Findable> edge : edges) {
            Location center = getEdgeCenter(edge).add(ARMORSTAND_OFFSET);
            ArmorStand edgeArmorStand = EditmodeUtils.getNewArmorStand(center, null,
                    editModeVisualizer.getEdgeHeadId());
            editModeEdgeArmorStands.put(edge, edgeArmorStand);
        }
        editModeTask.cancel();
        editModeTask = (PathTask) Bukkit.getScheduler().runTaskTimerAsynchronously(PathPlugin.getInstance(), () -> {

            List<Player> players = new ArrayList<>();
            for (UUID uuid : editingPlayers.keySet()) {
                players.add(Bukkit.getPlayer(uuid));
            }
            int particlesSpawned = 0;
            for (Pair<Findable, Findable> edge : edges) {
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
                                Math.pow(editModeVisualizer.getParticleDistance(), 2)) {
                            continue;
                        }

                        player.spawnParticle(
                                editModeVisualizer.getParticle(),
                                particleLocation, 1);
                        particlesSpawned++;
                    }
                }
            }
        }, 0, editModeVisualizer.getSchedulerPeriod());
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
    }

    public void setDefaultBezierTangentLength(double length) {
        this.defaultBezierTangentLength = length;
        updateData();
    }

    public void setWorld(World world) {
        if (world == null) {
            return;
        }
        this.world = world;
        updateData();
    }

    private void updateData() {
        PluginUtils.getInstance().runAsync(() -> {
            DatabaseModel.getInstance().updateRoadMap(this);
        });
    }

    private Location getEdgeCenter(Pair<Findable, Findable> edge) {
        Findable a = edge.first;
        Findable b = edge.second;
        if (a == null || b == null) {
            return null;
        }

        Vector va = a.getVector().clone();
        Vector vb = b.getVector().clone();
        return va.add(vb.subtract(va)).toLocation(world);
    }

    private Collection<Pair<Findable, Findable>> getEdges(Findable findable) {
        Collection<Pair<Findable, Findable>> ret = new ArrayList<>();
        for (Pair<Findable, Findable> edge : edges) {
            if (edge.first.equals(findable) || edge.second.equals(findable)) {
                ret.add(edge);
            }
        }
        return ret;
    }

    public Collection<Findable> getFindables(PathPlayer player) {
        Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
        if (bukkitPlayer == null) {
            return new ArrayList<>();
        }
        return getFindables().stream()
                .filter(node -> !player.hasFound(node.getDatabaseId()))
                .filter(node -> bukkitPlayer.hasPermission(node.getPermission()))
                .collect(Collectors.toSet());
    }
}
