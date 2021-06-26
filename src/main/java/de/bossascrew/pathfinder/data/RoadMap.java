package de.bossascrew.pathfinder.data;

import com.google.common.collect.Maps;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.bukkit.util.HeadDBUtils;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.HotbarMenu;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.util.BezierUtil;
import de.bossascrew.pathfinder.util.EditmodeUtils;
import de.bossascrew.pathfinder.util.Pair;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.task.TaskManager;

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

    private static final Vector ARMORSTAND_OFFSET = new Vector(0, -1.75, 0);
    private static final Vector ARMORSTAND_CHILD_OFFSET = new Vector(0, -1, 0);

    private final int databaseId;
    private String name;
    private World world;
    private boolean findableNodes;

    private final Map<Integer, Findable> findables = Maps.newHashMap();
    private final Collection<Pair<Findable, Findable>> edges;
    private final Collection<FindableGroup> groups;
    private final Map<UUID, HotbarMenu> editingPlayers;

    private PathVisualizer pathVisualizer;
    private EditModeVisualizer editModeVisualizer;
    private double nodeFindDistance;
    private double defaultBezierTangentLength;

    private final Map<Findable, ArmorStand> editModeNodeArmorStands;
    private final Map<Pair<Findable, Findable>, ArmorStand> editModeEdgeArmorStands;
    private int editModeTask = -1;

    public RoadMap(int databaseId, String name, World world, boolean findableNodes, PathVisualizer pathVisualizer,
                   EditModeVisualizer editModeVisualizer, double nodeFindDistance, double defaultBezierTangentLength) {
        this.databaseId = databaseId;
        this.name = name;
        this.world = world;
        this.findableNodes = findableNodes;
        this.nodeFindDistance = nodeFindDistance;
        this.defaultBezierTangentLength = defaultBezierTangentLength;

        this.findables.putAll(DatabaseModel.getInstance().loadNodes(this));
        this.edges = loadEdgesFromIds(Objects.requireNonNull(DatabaseModel.getInstance().loadEdges(this)));
        this.groups = new ArrayList<>(); //TODO aus datenbank laden

        this.editingPlayers = new HashMap<>();
        this.editModeNodeArmorStands = new ConcurrentHashMap<>();
        this.editModeEdgeArmorStands = new ConcurrentHashMap<>();

        setPathVisualizer(pathVisualizer);
        setEditModeVisualizer(editModeVisualizer);
    }

    public void setName(String name) {
        if (RoadMapHandler.getInstance().isNameUnique(name)) {
            this.name = name;
        }
        updateData();
    }

    public boolean isNodeNameUnique(String name) {
        return findables.values().stream().map(Findable::getName).noneMatch(context -> context.equalsIgnoreCase(name));
    }

    public void deleteFindable(int findableId) {
        deleteFindable(Objects.requireNonNull(getFindable(findableId)));
    }

    public void deleteFindable(Findable findable) {
        for (int edge : findable.getEdges()) {
            Findable target = getFindable(edge);
            if (target == null) {
                continue;
            }
            disconnectNodes(findable, target);
        }
        DatabaseModel.getInstance().deleteFindable(findable.getDatabaseId());
        findables.remove(findable.getDatabaseId());

        if (isEdited()) {
            updateEditModeParticles();
            editModeNodeArmorStands.get(findable).remove();
            editModeNodeArmorStands.remove(findable);
        }
    }

    public void createNode(Vector vector, String name) {
        createNode(vector, name, defaultBezierTangentLength, "none");
    }

    public void createNode(Vector vector, String name, double bezierTangentLength, String permission) {
        Node node = DatabaseModel.getInstance().newNode(this, Node.NO_GROUP_ID, vector, name, bezierTangentLength, permission);
        if (node != null) {
            addFindable(node);
        }
        if (isEdited()) {
            this.editModeNodeArmorStands.put(node, getNodeArmorStand(node));
        }
    }

    public void addFindable(Findable findable) {
        findables.put(findable.getDatabaseId(), findable);
    }

    public void setFindables(Map<Integer, Findable> findables) {
        this.findables.clear();
        this.findables.putAll(findables);
    }

    public void addFindables(Map<Integer, Findable> findables) {
        this.findables.putAll(findables);
    }

    public @Nullable
    Findable getFindable(String name) {
        for (Findable findable : findables.values()) {
            if (findable.getName().equalsIgnoreCase(name)) {
                return findable;
            }
        }
        return null;
    }

    public @Nullable
    Findable getFindable(int findableId) {
        for (Findable findable : findables.values()) {
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
        Pair<Findable, Findable> edge = new Pair<>(a, b);
        edges.add(edge);

        if (isEdited()) {
            updateEditModeParticles();
            this.editModeEdgeArmorStands.put(edge, getEdgeArmorStand(edge));
        }
    }

    public void disconnectNodes(Findable a, Findable b) {
        DatabaseModel.getInstance().deleteEdge(a, b);
        a.getEdges().remove((Integer) b.getDatabaseId());
        b.getEdges().remove((Integer) a.getDatabaseId());

        Pair<Findable, Findable> edge = edges.stream()
                .filter(pair -> (pair.first.equals(a) && pair.second.equals(b)) || pair.second.equals(a) && pair.first.equals(b))
                .findAny().orElse(null);

        if (edge == null) {
            return;
        }
        edges.remove(edge);
        if (isEdited()) {
            updateEditModeParticles();
            editModeEdgeArmorStands.get(edge).remove();
        }
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
        DatabaseModel.getInstance().deleteRoadMap(this);
    }

    /**
     * @return true sobald mindestens ein Spieler den Editmode aktiv hat
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
            editor.clearEditedRoadmap();
            editingPlayers.remove(uuid);

            if (!isEdited()) {
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

    public void startEditModeVisualizer() {

        for (Findable findable : findables.values()) {
            ArmorStand nodeArmorStand = getNodeArmorStand(findable);
            editModeNodeArmorStands.put(findable, nodeArmorStand);
        }
        List<Integer> processedFindables = new ArrayList<>();
        for (Pair<Findable, Findable> edge : edges) {
            if (processedFindables.contains(edge.second.getDatabaseId())) {
                continue;
            }
            ArmorStand edgeArmorStand = getEdgeArmorStand(edge);
            editModeEdgeArmorStands.put(edge, edgeArmorStand);
            processedFindables.add(edge.first.getDatabaseId());
        }
        updateEditModeParticles();
    }

    public void stopEditModeVisualizer() {
        for (ArmorStand armorStand : editModeNodeArmorStands.values()) {
            armorStand.remove();
        }
        for (ArmorStand armorStand : editModeEdgeArmorStands.values()) {
            armorStand.remove();
        }
        editModeNodeArmorStands.clear();
        editModeEdgeArmorStands.clear();
        Bukkit.getScheduler().cancelTask(editModeTask);
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
            asEdge.teleport(getEdgeCenter(edge).add(ARMORSTAND_CHILD_OFFSET));
        }
    }

    public void updateArmorStandNodeHeads() {
        ItemStack head = HeadDBUtils.getHeadById(editModeVisualizer.getNodeHeadId());
        for (ArmorStand armorStand : editModeNodeArmorStands.values()) {
            armorStand.getEquipment().setHelmet(head);
        }
    }

    public void updateArmorStandEdgeHeads() {
        ItemStack head = HeadDBUtils.getHeadById(editModeVisualizer.getEdgeHeadId());
        for (ArmorStand armorStand : editModeEdgeArmorStands.values()) {
            armorStand.getEquipment().setHelmet(head);
        }
    }

    public void updateArmorStandDisplay(Findable findable) {
        updateArmorStandDisplay(findable, true);
    }

    public void updateArmorStandDisplay(Findable findable, boolean considerEdges) {
        ArmorStand as = editModeNodeArmorStands.get(findable);
        getNodeArmorStand(findable, as);

        if (!considerEdges) {
            return;
        }
        for (int edge : findable.getEdges()) {
            Pair<Findable, Findable> edgePair = getEdge(findable.getDatabaseId(), edge);
            if (edgePair == null) {
                continue;
            }
            getEdgeArmorStand(edgePair, editModeEdgeArmorStands.get(edgePair));
        }
    }

    /**
     * Erstellt eine Liste aus Partikel Packets, die mithilfe eines Schedulers immerwieder an die Spieler im Editmode geschickt werden.
     * Um gelöschte und neue Kanten darstellen zu können, muss diese Liste aus Packets aktuallisiert werden.
     * Wird asynchron ausgeführt
     */
    public void updateEditModeParticles() {
        PluginUtils.getInstance().runSync(() -> {

            //Bestehenden Task cancellen
            Bukkit.getScheduler().cancelTask(editModeTask);

            //Packet List erstellen, die dem Spieler dann wieder und wieder geschickt wird. (Muss refreshed werden, wenn es Änderungen gibt.)
            List<Object> packets = new ArrayList<>();
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.valueOf(editModeVisualizer.getParticle().toString()));

            //Alle linearen Verbindungen der Waypoints errechnen und als Packet sammeln. Berücksichtigen, welche Node schon behandelt wurde, um doppelte Geraden zu vermeiden
            List<Pair<Findable, Findable>> processedFindables = new ArrayList<>();
            for (Pair<Findable, Findable> edge : edges) {
                if (processedFindables.contains(edge)) {
                    continue;
                }
                List<Vector> points = BezierUtil.getBezierCurveDistanced(editModeVisualizer.getParticleDistance(), edge.first.getVector(), edge.second.getVector());
                packets.addAll(points.stream()
                        .map(vector -> vector.toLocation(world))
                        .map(location -> particle.setLocation(location).toPacket())
                        .collect(Collectors.toSet()));
                processedFindables.add(edge);
            }
            if (packets.size() > editModeVisualizer.getParticleLimit()) {
                packets = packets.subList(0, editModeVisualizer.getParticleLimit());
            }
            final List<Object> fPackets = packets;
            editModeTask = TaskManager.startSuppliedTask(fPackets, editModeVisualizer.getSchedulerPeriod(), () -> editingPlayers.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline).collect(Collectors.toSet()));
        });
    }

    private ArmorStand getNodeArmorStand(Findable findable) {
        return getNodeArmorStand(findable, null);
    }

    private ArmorStand getNodeArmorStand(Findable findable, ArmorStand toEdit) {
        String name = findable.getName() + " #" + findable.getDatabaseId() +
                (findable.getFindableGroup() == null ? "" : " (" + findable.getFindableGroup().getName() + ")");

        if (toEdit == null) {
            toEdit = EditmodeUtils.getNewArmorStand(findable.getLocation().clone().add(ARMORSTAND_OFFSET), name, editModeVisualizer.getNodeHeadId());
        } else {
            toEdit.setCustomName(name);
            toEdit.getEquipment().setHelmet(HeadDBUtils.getHeadById(editModeVisualizer.getNodeHeadId()));
        }
        return toEdit;
    }

    private ArmorStand getEdgeArmorStand(Pair<Findable, Findable> edge) {
        return getEdgeArmorStand(edge, null);
    }

    private ArmorStand getEdgeArmorStand(Pair<Findable, Findable> edge, ArmorStand toEdit) {
        String name = edge.first.getName() + " (#" + edge.first.getDatabaseId() + ") ↔ " + edge.second.getName() + " (#" + edge.second.getDatabaseId() + ")";

        if (toEdit == null) {
            toEdit = EditmodeUtils.getNewArmorStand(getEdgeCenter(edge).add(ARMORSTAND_CHILD_OFFSET), name, editModeVisualizer.getEdgeHeadId(), true);
        } else {
            toEdit.setCustomName(name);
            toEdit.getEquipment().setHelmet(HeadDBUtils.getHeadById(editModeVisualizer.getEdgeHeadId()));
        }
        toEdit.setSmall(true);
        return toEdit;
    }

    public void setNodeFindDistance(double nodeFindDistance) {
        this.nodeFindDistance = nodeFindDistance;
        updateData();
    }

    public void setFindableNodes(boolean findableNodes) {
        this.findableNodes = findableNodes;
        updateData();
    }

    public void setEditModeVisualizer(EditModeVisualizer editModeVisualizer) {
        if (this.editModeVisualizer != null) {
            this.editModeVisualizer.getNodeHeadSubscribers().unsubscribe(this.getDatabaseId());
            this.editModeVisualizer.getEdgeHeadSubscribers().unsubscribe(this.getDatabaseId());
            this.editModeVisualizer.getUpdateParticle().unsubscribe(this.getDatabaseId());
        }
        this.editModeVisualizer = editModeVisualizer;
        updateData();

        this.editModeVisualizer.getNodeHeadSubscribers().subscribe(this.getDatabaseId(), integer -> PluginUtils.getInstance().runSync(() -> {
            if (isEdited()) {
                this.updateArmorStandNodeHeads();
            }
        }));
        this.editModeVisualizer.getEdgeHeadSubscribers().subscribe(this.getDatabaseId(), integer -> PluginUtils.getInstance().runSync(() -> {
            if (isEdited()) {
                this.updateArmorStandEdgeHeads();
            }
        }));
        this.editModeVisualizer.getUpdateParticle().subscribe(this.getDatabaseId(), obj -> {
            if (isEdited()) {
                updateEditModeParticles();
            }
        });

        if (isEdited()) {
            updateArmorStandEdgeHeads();
            updateArmorStandNodeHeads();
            updateEditModeParticles();
        }
    }

    public void setPathVisualizer(PathVisualizer pathVisualizer) {
        if (this.pathVisualizer != null) {
            this.pathVisualizer.getUpdateParticle().unsubscribe(this.getDatabaseId());
        }
        this.pathVisualizer = pathVisualizer;
        updateData();

        this.pathVisualizer.getUpdateParticle().subscribe(this.getDatabaseId(), integer -> this.updateActivePaths());
        updateActivePaths();
    }

    public void updateActivePaths() {
        if(PathPlayerHandler.getInstance() == null) {
            return;
        }
        //Jeder spieler kann pro Roadmap nur einen aktiven Pfad haben, weshalb man PathPlayer auf ParticlePath mappen kann
        PathPlayerHandler.getInstance().getPlayers().stream()
                .map(player -> player.getActivePaths().stream()
                        .filter(particlePath -> particlePath.getRoadMap().getDatabaseId() == this.getDatabaseId())
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .forEach(ParticlePath::run);
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

        Vector va = a.getVector().clone();
        Vector vb = b.getVector().clone();
        return va.add(vb.subtract(va).multiply(0.5)).toLocation(world);
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

    public Collection<Findable> getFindables() {
        return findables.values();
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

    public @Nullable
    Pair<Findable, Findable> getEdge(int aId, int bId) {
        return edges.stream()
                .filter(pair -> (pair.first.getDatabaseId() == aId && pair.second.getDatabaseId() == bId) ||
                        (pair.second.getDatabaseId() == aId && pair.first.getDatabaseId() == bId))
                .findAny()
                .orElse(null);
    }
}
