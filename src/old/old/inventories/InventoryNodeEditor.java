package de.bossascrew.pathfinder.old.inventories;

import java.util.ArrayList;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

import de.bossascrew.pathfinder.old.system.Edge;
import de.bossascrew.pathfinder.old.system.Node;

public class InventoryNodeEditor extends InventoryManager {

    public static List<InventoryNodeEditor> invs = new ArrayList<InventoryNodeEditor>();

    public static final String GUI_TITLE = "�f�nNode-Editor";
    public static final String NODE_ACTION_KEY = "node_inventory_action";
    public static final String NODE_ID_KEY = "node_id";
    public static final String NODE_ACTION_VALUE_DELETE = "delete";
    public static final String NODE_ACTION_VALUE_PERMISSION = "permission";
    public static final String NODE_ACTION_VALUE_TANGENT = "tangent";
    public static final String NODE_ACTION_VALUE_EDGE = "edge";

    RoadMap rm;
    Node n;

    public InventoryNodeEditor(RoadMap rm, Node n) {
        super(3, GUI_TITLE, InventoryType.DISPENSER);
        this.rm = rm;
        this.n = n;
        invs.add(this);
    }

    public static InventoryNodeEditor getInv(int id) {
        for (InventoryNodeEditor inv : invs) {
            if (inv.n.id == id) {
                return inv;
            }
        }
        return null;
    }

    @Override
    public void refresh() {
        super.refresh();

        List<String> edges = new ArrayList<String>();
        edges.add("�7Alle bisherigen Kanten:");
        for (Edge e : n.adjacencies) {
            edges.add("�e" + e.target.value + "�7,");
        }

        inv.setItem(1, setMetaTag(createItem(Material.BARRIER, "�cNode l�schen", 1), NODE_ACTION_KEY, NODE_ACTION_VALUE_DELETE));
        inv.setItem(3, setMetaTag(createItem(Material.LEAD, "�aTangenten st�rke", 1, "�7Aktuelle Tangentenwichtung: �a" + n.tangentReach), NODE_ACTION_KEY, NODE_ACTION_VALUE_TANGENT));
        inv.setItem(5, setMetaTag(createItem(Material.PAPER, "�9Setze Permission", 1, "�7Aktuelle Permission:", "�7" + n.permission), NODE_ACTION_KEY, NODE_ACTION_VALUE_PERMISSION));
        inv.setItem(7, setMetaTag(createItem(Material.BAMBOO, "�bKante aufspannen", 1, edges), NODE_ACTION_KEY, NODE_ACTION_VALUE_EDGE));
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR) {
                inv.setItem(i, setMetaID(inv.getItem(i), NODE_ID_KEY, n.id));
            }
        }
    }

    public Node getNode() {
        return n;
    }

    public RoadMap getRoadMap() {
        return rm;
    }
}
