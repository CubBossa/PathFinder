package de.bossascrew.pathfinder.old;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.bossascrew.pathfinder.old.data.Message;
import de.bossascrew.pathfinder.old.data.files.FileRoadMap;
import de.bossascrew.pathfinder.old.system.Edge;
import de.bossascrew.pathfinder.old.system.Node;
import de.bossascrew.pathfinder.old.visualization.EdgeManager;
import de.bossascrew.pathfinder.old.visualization.VisualizerEditMode;

public class RoadMap {

    static List<RoadMap> roadmaps = new ArrayList<RoadMap>();

    private List<UUID> editMode;
    private String key;
    private World w = null;
    private FileRoadMap fileWegpunkte;
    private VisualizerEditMode visualizer;
    private EdgeManager edgeManager;
    private PathManager pathManager;

    public RoadMap(String key, World w) {
        this.key = key;
        this.w = w;
        this.fileWegpunkte = new FileRoadMap(PathSystem.getInstance().getDataFolder().getPath() + "/roadmaps/", key + ".yml", w.getName());
        this.fileWegpunkte.lateSetup();

        editMode = new ArrayList<UUID>();
        visualizer = new VisualizerEditMode(this);
        edgeManager = new EdgeManager();
        pathManager = new PathManager(this);

        roadmaps.add(this);
    }

    public void delete() {
        visualizer.hide();
        editMode.clear();
        fileWegpunkte.deleteFile();
        for (int i = 0; i < roadmaps.size(); i++) {
            if (roadmaps.get(i).key.equals(key)) {
                roadmaps.remove(i);
            }
        }
    }

    public void toggleEdit(Player p) {
        if (editMode.contains(p.getUniqueId())) {
            editMode.remove(p.getUniqueId());
            p.sendMessage(Message.EDIT_MODE_OFF);
            if (editMode.size() < 1) {
                getVisualizer().hide();
            }
        } else {
            editMode.add(p.getUniqueId());
            p.sendMessage(Message.EDIT_MODE_ON);
            if (!getVisualizer().isVisualizing()) {
                getVisualizer().visualize();
            }
        }
    }

    public static RoadMap getRoadMap(String key) {
        for (RoadMap rm : roadmaps) {
            if (rm.key.equalsIgnoreCase(key)) {
                return rm;
            }
        }
        return null;
    }

    public static void loadRoadmaps() {
        roadmaps.clear();
        File dr = new File(PathSystem.getInstance().getDataFolder() + "/roadmaps/");
        dr.mkdirs();
        for (File file : dr.listFiles()) {
            String worldname = YamlConfiguration.loadConfiguration(file).getString("world");
            new RoadMap(file.getName().replace(".yml", ""), Bukkit.getWorld(worldname));
        }
    }

    public void saveWaypoint(Node key) {
        fileWegpunkte.addWaypoint(key);
        visualizer.refresh();
    }

//	public void saveEdge(String key1, String key2) {
//		fileWegpunkte.removeWaypoint(key);
//		visualizer.refresh();
//	}

    public void removeWaypoint(int key) {
        fileWegpunkte.removeWaypoint(key);
        //fileWegpunkte.setupFileWaypoints();
        //fileWegpunkte.loadFileWaypoints();
        visualizer.refresh();
    }

    public void removeEdge(int key1, int key2) {
        for (Node n : fileWegpunkte.waypoints) {
            List<Edge> edges = new ArrayList<Edge>();
            if (n.id == key1) {
                for (Edge e : n.adjacencies) {
                    if (e.target.id != key2) {
                        edges.add(e);
                    }
                }
                n.adjacencies = edges.toArray(new Edge[0]);
            } else if (n.id == key2) {
                for (Edge e : n.adjacencies) {
                    if (e.target.id != key1) {
                        edges.add(e);
                    }
                }
                n.adjacencies = edges.toArray(new Edge[0]);
            }
        }
        visualizer.refresh();
    }

    public void save() {
        visualizer.hide();
        fileWegpunkte.saveToFile();
    }

    public static List<RoadMap> getRoadMaps() {
        return roadmaps;
    }

    public String getKey() {
        return this.key;
    }

    public FileRoadMap getFile() {
        return fileWegpunkte;
    }

    public World getWorld() {
        return w;
    }

    public VisualizerEditMode getVisualizer() {
        return visualizer;
    }

    public EdgeManager getEdgeManager() {
        return edgeManager;
    }

    public PathManager getPathFinder() {
        return pathManager;
    }

    public List<UUID> getEditMode() {
        return editMode;
    }
}
