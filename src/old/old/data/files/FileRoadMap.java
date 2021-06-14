package de.bossascrew.pathfinder.old.data.files;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import main.de.bossascrew.pathfinder.PathSystem;
import de.bossascrew.pathfinder.old.system.Edge;
import de.bossascrew.pathfinder.old.system.Node;

public class FileRoadMap extends FileManager {

    public String worldname;

    Particle type;
    int amount;
    int steps;
    int delayInTicks;
    double distance;
    Vector offset;
    double particleViewDistance;
    double pathRandomness;
    boolean roundCornersEnabled;
    double roundCornersSmoothing;
    boolean checkPermission = false;

    double radian;
    double stepsInDegree;

    int highestNodeID = 0;
    public List<Node> waypoints;

    public FileRoadMap(String path, String fileName, String worldname) {
        super(path, fileName);
        this.worldname = worldname;
    }

    public void lateSetup() {
        waypoints = new ArrayList<Node>();
        System.out.println("Loading default values for RoadMap ...");
        type = Particle.DRAGON_BREATH;
        amount = 1;
        steps = 5;
        delayInTicks = 5;
        distance = 0.2;
        offset = new Vector(0, 0.7, 0);
        particleViewDistance = 50;
        pathRandomness = 0.1;
        roundCornersEnabled = true;
        roundCornersSmoothing = 3;
        checkPermission = false;
        radian = 1;
        stepsInDegree = 20;

        setupFileVaules();
        loadFileValue();
        loadFileWaypoints();
    }

    public void saveToFile() {
        loadCfg();
        setupFileWaypoints();
        setupFileVaules();
        save();
    }

    public void setupFileVaules() {
        System.out.println("Adding missing file values ...");
        if (!cfg.isSet("world")) {
            cfg.set("world", worldname);
        }
        if (!cfg.isSet("path.roundcorners.enabled")) {
            cfg.set("path.roundcorners.enabled", roundCornersEnabled);
        }
        if (!cfg.isSet("path.roundcorners.smoothing")) {
            cfg.set("path.roundcorners.smoothing", roundCornersSmoothing);
        }
        if (!cfg.isSet("path.particledisplay.type")) {
            cfg.set("path.particledisplay.type", type.toString());
        }
        if (!cfg.isSet("path.particledisplay.amount")) {
            cfg.set("path.particledisplay.amount", amount);
        }
        if (!cfg.isSet("path.particledisplay.steps")) {
            cfg.set("path.particledisplay.steps", steps);
        }
        Vector v = offset;
        if (!cfg.isSet("path.particledisplay.offset.x")) {
            cfg.set("path.particledisplay.offset.x", v.getX());
        }
        if (!cfg.isSet("path.particledisplay.offset.y")) {
            cfg.set("path.particledisplay.offset.y", v.getY());
        }
        if (!cfg.isSet("path.particledisplay.offset.z")) {
            cfg.set("path.particledisplay.offset.z", v.getZ());
        }
        if (!cfg.isSet("path.checkpermission")) {
            cfg.set("path.checkpermission", checkPermission);
        }
        if (!cfg.isSet("path.particledisplay.delayinticks")) {
            cfg.set("path.particledisplay.delayinticks", delayInTicks);
        }
        if (!cfg.isSet("path.particledisplay.distance")) {
            cfg.set("path.particledisplay.distance", distance);
        }
        if (!cfg.isSet("path.particledisplay.viewdistance")) {
            cfg.set("path.particledisplay.viewdistance", particleViewDistance);
        }
        if (!cfg.isSet("path.particledisplay.pathRandomness")) {
            cfg.set("path.particledisplay.pathRandomness", pathRandomness);
        }
        if (!cfg.isSet("path.particledisplay.endcircle.radian")) {
            cfg.set("path.particledisplay.endcircle.radian", radian);
        }
        if (!cfg.isSet("path.particledisplay.endcircle.degreeSteps")) {
            cfg.set("path.particledisplay.endcircle.degreeSteps", stepsInDegree);
        }
    }

    public void setupFileWaypoints() {
        cfg.set("waypoints", null);
        if (!waypoints.isEmpty()) {
            for (Node n : waypoints) {
                saveWaypoint(n);
            }
        }
    }

    public void loadFileValue() {
        worldname = cfg.getString("world");
        roundCornersEnabled = cfg.getBoolean("path.roundcorners.enabled");
        roundCornersSmoothing = cfg.getDouble("path.roundcorners.smoothing");
        type = Particle.valueOf(cfg.getString("path.particledisplay.type"));
        amount = cfg.getInt("path.particledisplay.amount");
        steps = cfg.getInt("path.particledisplay.steps");
        Vector v = new Vector();
        v.setX(cfg.getInt("path.particledisplay.offset.x"));
        v.setY(cfg.getInt("path.particledisplay.offset.y"));
        v.setZ(cfg.getInt("path.particledisplay.offset.z"));
        offset = v;
        checkPermission = cfg.getBoolean("path.checkpermission");
        delayInTicks = cfg.getInt("path.particledisplay.delayinticks");
        distance = cfg.getDouble("path.particledisplay.distance");
        particleViewDistance = cfg.getDouble("path.particledisplay.viewdistance");
        pathRandomness = cfg.getDouble("path.particledisplay.pathRandomness");
        radian = cfg.getDouble("path.particledisplay.endcircle.radian");
        stepsInDegree = cfg.getDouble("path.particledisplay.endcircle.degreeSteps");
    }

    public void loadFileWaypoints() {
        loadWaypoints();
    }

    public int getIncrementID() {
        this.highestNodeID++;
        return this.highestNodeID;
    }

    public void deleteFile() {
        file.delete();
    }

    public void addWaypoint(Node point) {
        if (!waypoints.contains(point)) {
            waypoints.add(point);
        }
    }

    private void saveWaypoint(Node point) {
        if (point == null) {
            PathSystem.getInstance().printToConsole("�cDie zu speichernde Node ist nicht definiert");
        }
        if (!point.permission.equals("none")) {
            cfg.set("waypoints." + point.id + ".permission", point.permission);
        }
        if (point.tangentReachSetManually) {
            cfg.set("waypoints." + point.id + ".tangentsmoothing", point.tangentReach);
        }
        cfg.set("waypoints." + point.id + ".name", point.value);
        cfg.set("waypoints." + point.id + ".loc.x", point.loc.getX());
        cfg.set("waypoints." + point.id + ".loc.y", point.loc.getY());
        cfg.set("waypoints." + point.id + ".loc.z", point.loc.getZ());
        if (point.adjacencies.length >= 1) {
            List<Integer> targets = new ArrayList<Integer>();
            for (Edge e : point.adjacencies) {
                targets.add(e.target.id);
            }
            cfg.set("waypoints." + point.id + ".targets", targets);
        }
    }

    public void removeWaypoint(int id) {
        if (cfg.isSet("waypoints." + id)) {
            cfg.set("waypoints." + id, null);
        }
        waypoints.remove(getNode(id));
        for (Node n : waypoints) {
            List<Edge> newEdges = new ArrayList<Edge>();
            for (Edge e : n.adjacencies) {
                if (e.target.id != id) {
                    newEdges.add(e);
                }
            }
            n.adjacencies = newEdges.toArray(new Edge[0]);
        }
    }

    public List<Node> getWaypoints(Location playerPos) {
        List<Node> ret = new ArrayList<Node>();
        for (Node n : waypoints) {
            n.h_scores = playerPos.toVector().distance(n.loc);
            ret.add(n);
        }
        return ret;
    }

    private List<Node> loadWaypoints() {
        List<Node> ret = new ArrayList<Node>();
        if (cfg.getConfigurationSection("waypoints") == null) {
            return null;
        }
        for (String key : cfg.getConfigurationSection("waypoints").getKeys(false)) {

            for (Node n : waypoints) {
                if ((n.id + "").equalsIgnoreCase(key)) {
                    System.out.println("Node nicht geladen, id bereits vergeben");
                    break;
                }
            }
            int id = 0;
            try {
                id = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                System.out.println("Kein g�ltiges ID format");
                break;
            }
            if (id > highestNodeID) {
                highestNodeID = id;
            }

            String value = cfg.getString("waypoints." + key + ".name");
            Vector v = new Vector(cfg.getDouble("waypoints." + key + ".loc.x"),
                    cfg.getDouble("waypoints." + key + ".loc.y"), cfg.getDouble("waypoints." + key + ".loc.z"));
            Node retNode = new Node(id, value, 0, v);
            String permission = cfg.getString("waypoints." + key + ".permission");
            if (cfg.isSet("waypoints." + key + ".permission") && !permission.equalsIgnoreCase("none")) {
                retNode.permission = permission;
            } else {
                retNode.permission = "none";
            }
            float smoothing = (float) cfg.getDouble("waypoints." + key + ".tangentsmoothing");
            if (cfg.isSet("waypoints." + key + ".tangentsmoothing")) {
                retNode.tangentReach = smoothing;
                retNode.tangentReachSetManually = true;
            } else {
                retNode.tangentReach = (float) roundCornersSmoothing;
            }
            ret.add(retNode);
        }
        for (Node n : ret) {
            List<Edge> edges = new ArrayList<Edge>();
            for (int id : cfg.getIntegerList("waypoints." + n.id + ".targets")) {
                for (Node targetNode : ret) {
                    if (targetNode.id == id) {
                        edges.add(new Edge(targetNode, targetNode.loc.distance(n.loc)));
                    }
                }
            }
            n.adjacencies = edges.toArray(new Edge[0]);
        }
        for (Node n : ret) {
            waypoints.add(n);
        }
        return ret;
    }

    public Node getNode(String value) {
        for (Node n : waypoints) {
            if (n.value.equalsIgnoreCase(value)) {
                return n;
            }
        }
        return null;
    }

    public Node getNode(int id) {
        for (Node n : waypoints) {
            if (n.id == id) {
                return n;
            }
        }
        return null;
    }

    public String getWorldname() {
        return worldname;
    }

    public void setWorldname(String name) {
        worldname = name;
    }

    public Particle getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getSteps() {
        return steps;
    }

    public int getDelayInTicks() {
        return delayInTicks;
    }

    public double getDistance() {
        return distance;
    }

    public Vector getOffset() {
        return offset;
    }

    public double getPathRandomness() {
        return pathRandomness;
    }

    public double getParticleViewDistance() {
        return particleViewDistance;
    }

    public boolean isRoundCornersEnabled() {
        return roundCornersEnabled;
    }

    public double getRoundCornersSmoothing() {
        return roundCornersSmoothing;
    }

    public boolean isCheckPermission() {
        return checkPermission;
    }

    public double getRadian() {
        return radian;
    }

    public double getStepsInDegree() {
        return stepsInDegree;
    }
}
