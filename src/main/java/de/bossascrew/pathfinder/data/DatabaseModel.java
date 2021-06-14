package de.bossascrew.pathfinder.data;

import de.bossascrew.core.sql.MySQL;
import de.bossascrew.core.util.SQLUtils;
import de.bossascrew.pathfinder.Node;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.RoadMap;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.visualisation.PathVisualizer;
import jdk.internal.net.http.common.Pair;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DatabaseModel {

    @Getter
    private static DatabaseModel instance;

    private PathPlugin plugin;

    public DatabaseModel(PathPlugin plugin) {
        this.plugin = plugin;

        createVisualizerTable();
        createEditModeVisualizerTable();
        createRoadMapsTable();
        createNodesTable();
        createEdgesTable();
        createFoundNodesTable();
    }

    public void createVisualizerTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_path_visualizer` (" +
                    "`path_visualizer_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`name` VARCHAR(36) NOT NULL , " +
                    "`particle` VARCHAR(24) NOT NULL , " +
                    "`particle_distance` DOUBLE NOT NUll , " +
                    "`scheduler_start_delay` INT NOT NUll , " +
                    "`scheduler_period` INT NOT NULL )")) { //TODO später mehr werte ändern, tabelle löschen und neu generieren
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Path-Visualizer-Tabelle", e);
        }
    }

    public void createEditModeVisualizerTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_editmode_visualizer` (" +
                    "`editmode_visualizer_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`name` VARCHAR(36) NOT NULL , " +
                    "`particle` VARCHAR(24) NOT NULL , " +
                    "`particle_limit` INT NOT NUll , " +
                    "`particle_distance` DOUBLE NOT NUll , " +
                    "`scheduler_start_delay` INT NOT NUll , " +
                    "`scheduler_period` INT NOT NULL , " +
                    "`node_head_id` INT NOT NULL , " +
                    "`edge_head_id` INT NOT NULL )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Editmode-Visualizer-Tabelle", e);
        }
    }

    public void createRoadMapsTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_roadmaps` (" +
                    "`roadmap_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`name` VARCHAR(36) NOT NULL , " +
                    "`world` VARCHAR(24) NOT NULL , " +
                    "`findable` BOOLEAN , " +
                    "`path_visualizer_id` INT NULL , " +
                    "`editmode_visualizer_id` INT NULL , " +
                    "`node_find_distance` DOUBLE NOT NUll , " +
                    "`default_tangent_length` DOUBLE NOT NUll , " +
                    "FOREIGN KEY (path_visualizer_id) REFERENCES pathfinder_path_visualizer(path_visualizer_id) ON DELETE SET NULL , " +
                    "FOREIGN KEY (editmode_visualizer_id) REFERENCES pathfinder_editmode_visualizer(editmode_visualizer_id) ON DELETE SET NULL )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Roadmap-Tabelle", e);
        }
    }

    public void createNodesTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_nodes` (" +
                    "`node_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`roadmap_id` INT NOT NULL , " +
                    "`group_id` INT NOT NULL , " +
                    "`x` DOUBLE NOT NUll , " +
                    "`y` DOUBLE NOT NUll , " +
                    "`z` DOUBLE NOT NUll , " +
                    "`name` VARCHAR(24) NOT NULL , " +
                    "`tangent_length` DOUBLE NULL , " +
                    "`permission` VARCHAR(30) NULL , " +
                    "FOREIGN KEY (roadmap_id) REFERENCES pathfinder_roadmaps(roadmap_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Nodes-Tabelle", e);
        }
    }

    public void createEdgesTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_edges` (" +
                    "`node_a_id` INT NOT NULL , " +
                    "`node_b_id` INT NOT NULL , " +
                    "FOREIGN KEY (node_a_id) REFERENCES pathfinder_nodes(node_id) ON DELETE CASCADE , " +
                    "FOREIGN KEY (node_b_id) REFERENCES pathfinder_nodes(node_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Edges-Tabelle", e);
        }
    }

    public void createFoundNodesTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_found` (" +
                    "`player_id` INT NOT NULL , " +
                    "`node_id` INT NOT NULL , " +
                    "`date` TIMESTAMP NULL , " +
                    "PRIMARY KEY (`player_id`, `node_id`) , " +
                    "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE , " +
                    "FOREIGN KEY (node_id) REFERENCES pathfinder_nodes(node_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der found-nodes Tabelle", e);
        }
    }

    public @Nullable
    RoadMap createRoadMap(String name, World world, boolean findableNodes) {
        return createRoadMap(name, world, findableNodes, 0, 0, 3, 3);
    }

    public @Nullable
    RoadMap createRoadMap(String name, World world, boolean findableNodes, int pathVis, int editModeVis, double findDist, double tangentLength) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_roadmaps` " +
                    "(name, world, findable, path_visualizer_id, editmode_visualizer_id, node_find_distance, default_tangent_length) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setString(stmt, 1, name);
                SQLUtils.setString(stmt, 2, world.getName());
                SQLUtils.setBoolean(stmt, 3, findableNodes);
                SQLUtils.setInt(stmt, 4, pathVis);
                SQLUtils.setInt(stmt, 5, editModeVis);
                SQLUtils.setDouble(stmt, 6, findDist);
                SQLUtils.setDouble(stmt, 7, tangentLength);

                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();
                    int databaseId = resultSet.getInt(1);

                    PathVisualizer pathVisualizer = VisualizerHandler.getInstance().getPathVisualizer(pathVis);
                    EditModeVisualizer editModeVisualizer = VisualizerHandler.getInstance().getEditVisualizer(editModeVis);

                    RoadMap roadMap = new RoadMap(databaseId, name, world, findableNodes,
                            pathVisualizer, editModeVisualizer, findDist, tangentLength);
                    return roadMap;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Roadmap in der Pathfinder Datenbank", e);
        }
        return null;
    }

    public @Nullable
    Map<Integer, RoadMap> loadRoadMaps() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_roadmaps`")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, RoadMap> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {

                        int databaseId = SQLUtils.getInt(resultSet, "roadmap_id");
                        String name = SQLUtils.getString(resultSet, "name");
                        String worldName = SQLUtils.getString(resultSet, "world");
                        boolean findable = SQLUtils.getBoolean(resultSet, "findable");
                        int pathVisId = SQLUtils.getInt(resultSet, "path_visualizer_id");
                        int editModeVisId = SQLUtils.getInt(resultSet, "editmode_visualizer_id");
                        double findDistance = SQLUtils.getDouble(resultSet, "node_find_distance");
                        double tangentLength = SQLUtils.getDouble(resultSet, "default_tangent_length");

                        World world = Bukkit.getWorld(worldName);
                        PathVisualizer pathVisualizer = VisualizerHandler.getInstance().getPathVisualizer(pathVisId);
                        EditModeVisualizer editModeVisualizer = VisualizerHandler.getInstance().getEditVisualizer(editModeVisId);
                        assert world != null;
                        //TODO nach schönerem weg gucken, die defaults zu laden
                        if (pathVisualizer == null) {
                            pathVisualizer = VisualizerHandler.getInstance().getPathVisualizer(0);
                        }
                        if (editModeVisualizer == null) {
                            editModeVisualizer = VisualizerHandler.getInstance().getEditVisualizer(0);
                        }

                        RoadMap rm = new RoadMap(databaseId, name, world, findable, pathVisualizer, editModeVisualizer, findDistance, tangentLength);
                        result.put(databaseId, rm);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Roadmaps", e);
        }
        return null;
    }

    public void updateRoadMap(RoadMap roadMap) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_roadmaps` SET " +
                    "`name` = ?, " +
                    "`world` = ?, " +
                    "`findable` = ?, " +
                    "`path_visualizer` = ?, " +
                    "`editmode_visualizer` = ?, " +
                    "`node_find_distance` = ?, " +
                    "`default_tangent_length` = ?, " +
                    "WHERE `roadmap_id` = ?")) {
                SQLUtils.setString(stmt, 1, roadMap.getName());
                SQLUtils.setString(stmt, 2, roadMap.getWorld().getName());
                SQLUtils.setBoolean(stmt, 3, roadMap.isFindableNodes());
                SQLUtils.setInt(stmt, 4, roadMap.getVisualizer().getDatabaseId());
                SQLUtils.setInt(stmt, 5, roadMap.getEditModeVisualizer().getDatabaseId());
                SQLUtils.setDouble(stmt, 6, roadMap.getNodeFindDistance());
                SQLUtils.setDouble(stmt, 7, roadMap.getDefaultBezierTangentLength());
                SQLUtils.setInt(stmt, 8, roadMap.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren der RoadMap: " + roadMap.getName(), e);
        }
    }

    public boolean deleteRoadMap(RoadMap roadMap) {
        return deleteRoadMap(roadMap.getDatabaseId());
    }

    public boolean deleteRoadMap(int roadMapId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE * FROM `pathfinder_roadmaps` WHERE `roadmap_id` = ?")) {
                SQLUtils.setInt(stmt, 1, roadMapId);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim löschen der RoadMap: " + roadMapId, e);
        }
        return false;
    }

    public void newEdge(Node nodeA, Node nodeB) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_edges` " +
                    "(node_a_id, node_b_id) VALUES (?, ?)")) {
                SQLUtils.setInt(stmt, 1, nodeA.getDatabaseId());
                SQLUtils.setInt(stmt, 2, nodeB.getDatabaseId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Edge in der Pathfinder Datenbank", e);
        }
    }

    public @Nullable
    Collection<Pair<Integer, Integer>> loadEdges(RoadMap roadMap) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_nodes` WHERE `roadmap_id` = ? "
                    + "AND `pathfinder_nodes`.`node_id` = `pathfinder_edges`.`node_a_id`")) {
                SQLUtils.setInt(stmt, 1, roadMap.getDatabaseId());

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Collection<Pair<Integer, Integer>> result = new ArrayList<>();
                    while (resultSet.next()) {

                        int nodeA = SQLUtils.getInt(resultSet, "node_a_id");
                        int nodeB = SQLUtils.getInt(resultSet, "node_b_id");
                        result.add(new Pair<Integer, Integer>(nodeA, nodeB));
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Edges zur Roadmap: " + roadMap.getName(), e);
        }
        return null;
    }

    public @Nullable
    Node newNode(int roadMapId, int groupId, Vector vector, String name, Double tangentLength, String permission) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_nodes` " +
                    "(roadmap_id, group_id, x, y, z, name, tangent_length, permission) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setInt(stmt, 1, roadMapId);
                SQLUtils.setInt(stmt, 2, groupId);
                SQLUtils.setDouble(stmt, 3, vector.getX());
                SQLUtils.setDouble(stmt, 4, vector.getY());
                SQLUtils.setDouble(stmt, 5, vector.getZ());
                SQLUtils.setString(stmt, 6, name);
                SQLUtils.setDouble(stmt, 7, tangentLength);
                SQLUtils.setString(stmt, 8, permission);

                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();
                    int databaseId = resultSet.getInt(1);
                    Node n = new Node(databaseId, roadMapId, name, vector);
                    n.setGroup(groupId);
                    n.setBezierTangentLength(tangentLength);
                    n.setPermission(permission);
                    return n;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Node in der Pathfinder Datenbank", e);
        }
        return null;
    }

    public void deleteNode(int nodeId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE * FROM `pathfinder_nodes` WHERE `node_id` = ?")) {
                SQLUtils.setInt(stmt, 1, nodeId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen der Node mit ID: " + nodeId, e);
        }
    }

    public void updateNode(Node node) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_nodes` SET " +
                    "`roadmap_id` = ?, " +
                    "`group_id` = ?, " +
                    "`x` = ?, " +
                    "`y` = ?, " +
                    "`z` = ?, " +
                    "`name` = ?, " +
                    "`tangent_length` = ?, " +
                    "`permission` = ?, " +
                    "WHERE `node_id` = ?")) {
                SQLUtils.setInt(stmt, 1, node.getRoadMapId());
                SQLUtils.setInt(stmt, 2, node.getNodeGroupId());
                SQLUtils.setDouble(stmt, 3, node.getVector().getX());
                SQLUtils.setDouble(stmt, 4, node.getVector().getY());
                SQLUtils.setDouble(stmt, 5, node.getVector().getZ());
                SQLUtils.setString(stmt, 6, node.getName());
                SQLUtils.setDouble(stmt, 7, node.getBezierTangentLength());
                SQLUtils.setString(stmt, 8, node.getPermission());
                SQLUtils.setInt(stmt, 9, node.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren der Node: " + node.getName(), e);
        }
    }

    public @Nullable
    Map<Integer, Node> loadNodes(RoadMap roadMap) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_nodes` WHERE `roadmap_id` = ?")) {
                SQLUtils.setInt(stmt, 1, roadMap.getDatabaseId());

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, Node> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "node_id");
                        int groupId = SQLUtils.getInt(resultSet, "group_id");
                        double x = SQLUtils.getDouble(resultSet, "x");
                        double y = SQLUtils.getDouble(resultSet, "y");
                        double z = SQLUtils.getDouble(resultSet, "z");
                        String name = SQLUtils.getString(resultSet, "name");
                        Double tangentLength = SQLUtils.getDouble(resultSet, "tangent_length");
                        String permission = SQLUtils.getString(resultSet, "permission");

                        Node node = new Node(id, roadMap.getDatabaseId(), name, new Vector(x, y, z));
                        node.setPermission(permission);
                        node.setBezierTangentLength(tangentLength);
                        node.setGroup(groupId);
                        result.put(id, node);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Nodes zur Roadmap: " + roadMap.getName(), e);
        }
        return null;
    }

    public @Nullable
    FoundInfo newFoundInfo(int globalPlayerId, int nodeId, Date foundDate) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_found` " +
                    "(player_id, node_id, date) VALUES (?, ?, ?)")) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);
                SQLUtils.setInt(stmt, 2, nodeId);
                SQLUtils.setDate(stmt, 3, foundDate);
                stmt.executeUpdate();
                return new FoundInfo(globalPlayerId, nodeId, foundDate);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Eintragen eines gefundenen Nodes in die Datenbank", e);
        }
        return null;
    }

    /**
     * @return Map mit NodeID als Key und FoundInfo Objekt als Value für den angegebenen Spieler
     */
    public @Nullable
    Map<Integer, FoundInfo> loadFoundNodes(int globalPlayerId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_found` WHERE `player_id` = ?")) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, FoundInfo> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {
                        int player = SQLUtils.getInt(resultSet, "player_id");
                        int node = SQLUtils.getInt(resultSet, "node_id");
                        Date date = SQLUtils.getDate(resultSet, "date");

                        FoundInfo info = new FoundInfo(player, node, date);
                        result.put(node, info);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Pathfinder-Spielerdaten von Spieler-ID: " + globalPlayerId, e);
        }
        return null;
    }

    public void deleteFoundNode(int globalPlayerId, int nodeId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE * FROM `pathfinder_found` WHERE `player_id` = ? AND `node_id` = ?")) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);
                SQLUtils.setInt(stmt, 2, nodeId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen der Foundinfo für Spieler mit ID: " + globalPlayerId, e);
        }
    }


    public @Nullable
    EditModeVisualizer newEditModeVisualizer(String name, Particle particle, double particleDistance, int particleLimit,
                                             int schedulerStartDelay, int schedulerPeriod, int nodeHeadId, int edgeHeadId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_visualizer_editmode` " +
                    "(name, particle, particle_distance, particle_limit, scheduler_start_delay, scheduler_period" +
                    ", node_head_id, edge_head_id) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setString(stmt, 1, name);
                SQLUtils.setString(stmt, 2, particle.name());
                SQLUtils.setDouble(stmt, 3, particleDistance);
                SQLUtils.setInt(stmt, 4, particleLimit);
                SQLUtils.setInt(stmt, 5, schedulerStartDelay);
                SQLUtils.setInt(stmt, 6, schedulerPeriod);
                SQLUtils.setInt(stmt, 7, nodeHeadId);
                SQLUtils.setInt(stmt, 8, edgeHeadId);

                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();

                    int databaseId = resultSet.getInt(1);
                    EditModeVisualizer vis = new EditModeVisualizer(databaseId, name);
                    vis.setParticle(particle);
                    vis.setParticleLimit(particleLimit);
                    vis.setParticleDistance(particleDistance);
                    vis.setSchedulerPeriod(schedulerPeriod);
                    vis.setSchedulerStartDelay(schedulerStartDelay);
                    vis.setNodeHeadId(nodeHeadId);
                    vis.setEdgeHeadId(edgeHeadId);
                    return vis;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Node in der Pathfinder Datenbank", e);
        }
        return null;
    }

    public @Nullable
    Map<Integer, EditModeVisualizer> loadEditModeVisualizer() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_editmode_visualizer`")) {

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, EditModeVisualizer> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "visualizer_id");
                        String name = SQLUtils.getString(resultSet, "name");
                        String particleName = SQLUtils.getString(resultSet, "particle");
                        int particleLimit = SQLUtils.getInt(resultSet, "particle_limit");
                        double particleDistance = SQLUtils.getDouble(resultSet, "particle_distance");
                        int schedulerStartDelay = SQLUtils.getInt(resultSet, "scheduler_start_delay");
                        int schedulerPeriod = SQLUtils.getInt(resultSet, "scheduler_period");
                        int nodeHeadId = SQLUtils.getInt(resultSet, "node_head_id");
                        int edgeHeadId = SQLUtils.getInt(resultSet, "edge_head_id");

                        Particle particle;
                        try {
                            particle = Particle.valueOf(particleName);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        EditModeVisualizer vis = new EditModeVisualizer(id, name);
                        vis.setParticle(particle);
                        vis.setParticleLimit(particleLimit);
                        vis.setParticleDistance(particleDistance);
                        vis.setSchedulerStartDelay(schedulerStartDelay);
                        vis.setSchedulerPeriod(schedulerPeriod);
                        vis.setNodeHeadId(nodeHeadId);
                        vis.setEdgeHeadId(edgeHeadId);
                        result.put(id, vis);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der EditModeVisualizer", e);
        }
        return null;
    }

    public @Nullable
    PathVisualizer newPathVisualizer(String name, Particle particle) {
        return null;
    }

    public Map<Integer, PathVisualizer> loadVisualizer() {
        return new ConcurrentHashMap<>();
    }

    //visualizerprofile speichern
}
