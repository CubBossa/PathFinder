package pathfinder;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
    private String name;
    private World world;
    private boolean findableNodes = false;

    private Collection<Node> nodes;
    private Collection<NodeGroup> groups;
    private Collection<UUID> editingPlayers;

    private int visualizerId;
    private double nodeFindDistance;

    public RoadMap(String name, World world, boolean findableNodes) {
        this.name = name;
        this.world = world;
        this.findableNodes = findableNodes;

        this.nodes = new ArrayList<Node>();
        groups = new ArrayList<NodeGroup>();
        this.editingPlayers = new ArrayList<UUID>();
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

    public void cancelAllEditModes() {
        //TODO informiere alle Spieler im Editmode, dass roadmap gelöscht wurde
    }

    public void delete() {
        //TODO lösche alle visualisierungen
    }

    public void toggleEditMode(Player player) {
        setEditMode(player, !isEditing(player));
    }

    public void setEditMode(Player player, boolean editing) {
        if(editing) {
            editingPlayers.add(player.getUniqueId());
            openEditMode(player);
        } else {
            if(editingPlayers.contains(player.getUniqueId()))
                editingPlayers.remove(player.getUniqueId());
            closeEditMode(player);
        }
    }

    public boolean isEditing(Player player) {
        return editingPlayers.contains(player.getUniqueId());
    }

    private void openEditMode(Player player) {
        //TODO equippe mit wichtigen Items für Editmode
    }

    private void closeEditMode(Player player) {
        //TODO entferne Editmode items
    }
}
