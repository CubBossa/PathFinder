package pathfinder;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;
import pathfinder.visualisation.PathVisualizer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Eine Straßenkarte, die verschiedene Wegpunkte enthält und
 * durch die mithifle des AStar Algorithmus ein Pfad gefunden werden kann.
 */
@Getter
public class RoadMap {

    private int databaseId;
    @Setter
    private String name;
    @Setter
    private World world;
    private boolean findableNodes = false;

    private Collection<Node> nodes;
    private Collection<NodeGroup> groups;
    private Collection<UUID> editingPlayers;

    private PathVisualizer visualizer;
    private double nodeFindDistance;

    public RoadMap(String name, World world, boolean findableNodes) {
        setName(name);
        this.world = world;
        this.findableNodes = findableNodes;

        this.nodes = new ArrayList<Node>();
        groups = new ArrayList<NodeGroup>();
        this.editingPlayers = new ArrayList<UUID>();
    }

    public void setName(String name) {
        if(RoadMapHandler.getInstance().isNameUnique(name)) {
            this.name = name;
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
    NodeGroup getNodeGroup(int id) {
        for(NodeGroup nodeGroup : groups) {
            if(nodeGroup.getDatabaseId() == id) {
                return nodeGroup;
            }
        }
        return null;
    }

    public @Nullable
    NodeGroup addGroup(String name) {
        if(isGroupNameUnique(name)) {
            //TODO neue Gruppe im DatabaseModel erstellen und laden.
        }
        return null;
    }

    private boolean isGroupNameUnique(String name) {
        for(NodeGroup group : groups) {
            if(group.getName().equals(name)) return false;
        }
        return true;
    }

    public void cancelAllEditModes() {

    }

    public void delete() {
        //TODO informiere alle Spieler im Editmode, dass roadmap gelöscht wurde
        //TODO evntl. lösche alle visualisierungen
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

    public void toggleEditMode(Player player) {
        toggleEditMode(player.getUniqueId());
    }

    public void setEditMode(UUID uuid, boolean editing) {

        PathPlayer editor = PlayerHandler.getInstance().getPlayer(uuid);

        if(editing) {
            editor.setEditMode(databaseId);
            editingPlayers.add(uuid);
            openEditMode(uuid);
        } else {
            editor.clearEditMode();
            if(editingPlayers.contains(uuid))
                editingPlayers.remove(uuid);
            closeEditMode(uuid);
        }
    }

    public void setEditMode(Player player, boolean editing) {
        setEditMode(player.getUniqueId(), editing);
    }

    public boolean isEditing(UUID uuid) {
        return editingPlayers.contains(uuid);
    }

    public boolean isEditing(Player player) {
        return isEditing(player.getUniqueId());
    }

    private void openEditMode(UUID uuid) {
        //TODO
    }

    private void openEditMode(Player player) {
        //TODO equippe mit wichtigen Items für Editmode

        //wegpunkt werkzeug: rechtsklick setzen, linksklick löschen
        //kantenwerkzeug: rechtsklick kante aufspannen, linksklick alle kanten eines nodes löschen. Linksklick auf kante = löschen
        //kompass: tp zum nächsten Node
        //Slimeball: Rundung der Tangenten einstellen
        //nametag: Permissionnode setzen
        //Kiste: GruppenGUI: erstes item barriere = keine gruppe. dann alle gruppen als nametags. unten rechts emerald für neue gruppe.
            //rechtsklick auf gruppe = zuweisen. Linksklick mit Confirm = gruppe löschen.
            //Gruppenicons haben in der Lore eine Liste aller Nodes, die Teil der Gruppe sind.
    }

    private void closeEditMode(UUID uuid) {
        //TODO
    }

    private void closeEditMode(Player player) {
        //TODO entferne Editmode items
    }

    public void setVisualizer(PathVisualizer visualizer) {
        this.visualizer = visualizer;
        //TODO alle aktuellen paths updaten.
    }
}
