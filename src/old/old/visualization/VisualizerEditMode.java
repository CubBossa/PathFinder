package de.bossascrew.pathfinder.old.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import de.tr7zw.nbtapi.NBTItem;
import main.de.bossascrew.pathfinder.PathSystem;
import de.bossascrew.pathfinder.old.system.Edge;
import de.bossascrew.pathfinder.old.system.Node;

public class VisualizerEditMode extends VisualizerParent {

    public static String WAYPOINT_NAME = "�f�nWaypoint";
    public static String EDGE_NAME = "�f�nPathSystemEdge";

    public static String IDENTIFIER_KEY = "identifier";
    public static String ROADMAP_KEY = "�f�nRoadMapName";

    private List<ArmorStand> armorstands;
    private List<EdgeDisplayObject> edges;

    boolean visualizing = false;

    int edgeSched;
    double edgeSteps;
    int delayEditmode;
    Particle typeEditmode;
    double particleViewDistanceEditmode;
    int amount;

    public VisualizerEditMode(RoadMap rm) {
        super(rm);

        edgeSteps = PathSystem.getInstance().getConfigFile().getDistance();
        typeEditmode = PathSystem.getInstance().getConfigFile().getType();
        delayEditmode = PathSystem.getInstance().getConfigFile().getFrequence();
        particleViewDistanceEditmode = PathSystem.getInstance().getConfigFile().getParticleViewDistance();
        amount = PathSystem.getInstance().getConfigFile().getAmount();

        armorstands = new ArrayList<ArmorStand>();
        edges = new ArrayList<EdgeDisplayObject>();
        refresh();
    }

    public void refresh() {
        edges.clear();
        for (Node n : rm.getFile().waypoints) {
            for (Edge e : n.adjacencies) {
                boolean isSet = false;
                for (EdgeDisplayObject ed : edges) {
                    if (ed.n1.loc.equals(e.target.loc) && ed.n2.loc.equals(n.loc)
                            || ed.n1.loc.equals(e.target.loc) && ed.n1.loc.equals(n.loc)) {
                        isSet = true;
                    }
                }
                if (!isSet) {
                    edges.add(new EdgeDisplayObject(rm, n, e.target));
                }
            }
        }
        if (visualizing) {
            hide();
            visualize();
        }
    }

    public void visualize() {
        if (visualizing) {
            return;
        }

        showEdges();
        showWaypoints();
        visualizing = true;
    }

    public void hide() {
        if (!visualizing) {
            return;
        }

        hideEdges();
        hideWaypoints();
        visualizing = false;
    }

    public void showEdges() {
        edgeParticleScheduler();
        World w = rm.getWorld();
        for (EdgeDisplayObject ed : edges) {
            Vector center = ed.n1.loc.clone().getMidpoint(ed.n2.loc);
            ArmorStand as = (ArmorStand) w.spawn(center.toLocation(w).add(new Vector(0, -0.5, 0)), ArmorStand.class);
            as.setGravity(false);
            as.setCustomName(EDGE_NAME);
            as.setVisible(false);
            as.setSmall(true);

            armorstands.add(as);

            ItemStack head = getPlayerHead(EDGE_NAME, ed.n1.id + "###" + ed.n2.id, "MHF_pumpkin");
            as.getEquipment().setHelmet(head);
        }
    }

    public void hideEdges() {
        Bukkit.getScheduler().cancelTask(edgeSched);
    }

    private void edgeParticleScheduler() {
        edgeSched = Bukkit.getScheduler().scheduleSyncRepeatingTask(PathSystem.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (EdgeDisplayObject ed : edges) {
                    Vector direction = ed.n2.loc.clone().subtract(ed.n1.loc).normalize();
                    double length = ed.n1.loc.clone().distance(ed.n2.loc);
                    for (int i = 0; i < length / edgeSteps; i++) {
                        for (UUID uuid : rm.getEditMode()) {
                            spawnVisualizerParticles(Bukkit.getPlayer(uuid), ed.n1.loc.toLocation(rm.getWorld()).clone().add(direction.clone().multiply(edgeSteps * i)));
                        }
                    }
                }
            }
        }, 0, delayEditmode);
    }

    //TODO visualizer objekt nur f�r partikel
    public void spawnVisualizerParticles(Player p, Location pos) {
        if (p == null) {
            return;
        }
        if (p.getLocation().distance(pos) < particleViewDistanceEditmode) {
            p.spawnParticle(typeEditmode, pos, amount, 0.01, 0.01, 0.01, 0);
        }
    }

    public void showWaypoints() {
        World w = rm.getWorld();
        for (Node n : rm.getFile().waypoints) {
            ArmorStand as = (ArmorStand) w.spawn(n.loc.toLocation(w).add(new Vector(0, -0.5, 0)), ArmorStand.class);
            as.setGravity(false);
            as.setCustomName(WAYPOINT_NAME + " �a" + n.value);
            as.setCustomNameVisible(true);
            as.setVisible(false);
            as.setSmall(true);

            armorstands.add(as);

            ItemStack head = getPlayerHead(WAYPOINT_NAME, n.id + "", "MHF_Melon");
            as.getEquipment().setHelmet(head);
        }
    }

    @SuppressWarnings("deprecation")
    private ItemStack getPlayerHead(String displayname, String id, String skull) {
        ItemStack Item_Skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta Meta_Skull = (SkullMeta) Item_Skull.getItemMeta();
        Meta_Skull.setOwningPlayer(Bukkit.getOfflinePlayer(skull));
        Meta_Skull.setDisplayName(WAYPOINT_NAME);

        Item_Skull.setItemMeta(Meta_Skull);

        NBTItem nbt = new NBTItem(Item_Skull);
        nbt.setString(IDENTIFIER_KEY, id);
        nbt.setString(ROADMAP_KEY, rm.getKey());

        return nbt.getItem();
    }

    public void hideWaypoints() {
        for (ArmorStand as : armorstands) {
            as.remove();
        }
        armorstands.clear();
    }

    public boolean isVisualizing() {
        return visualizing;
    }
}
