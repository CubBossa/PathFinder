package pathfinder.data;

import de.bossascrew.core.sql.MySQL;
import de.bossascrew.core.util.SQLUtils;
import jdk.internal.net.http.common.Pair;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import pathfinder.Node;
import pathfinder.PathPlugin;
import pathfinder.RoadMap;
import pathfinder.old.system.Edge;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DatabaseModel {

    @Getter
    private static DatabaseModel instance;

    private PathPlugin plugin;

    public DatabaseModel(PathPlugin plugin) {
        this.plugin = plugin;

        createNodesTable();
        createEdgesTable();
        createFoundNodesTable();
    }

    public void createRoadMapsTable() {
        //TODO wenn alle spalten festgelegt sind
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
                    "FOREIGN KEY (roadmap_id) REFERENCES pathfinder_roadmaps(roadmap_id) ON DELETE CASCADE)")) {
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
                    "FOREIGN KEY (node_a_id) REFERENCES pathfinder_nodes(node_id) ON DELETE CASCADE) , " +
                    "FOREIGN KEY (node_b_id) REFERENCES pathfinder_nodes(node_id) ON DELETE CASCADE)")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Edges-Tabelle", e);
        }
    }

    public void createFoundNodesTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_found` (" +
                    "`found_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`player_id` INT NOT NULL , " +
                    "`node_id` INT NOT NULL , " +
                    "`date` TIMESTAMP NULL " +
                    "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE) ," +
                    "FOREIGN KEY (node_id) REFERENCES pathfinder_nodes(node_id) ON DELETE CASCADE)")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der found-nodes Tabelle", e);
        }
    }

    public void createVisualizerTable() {
        //TODO
    }


    public @Nullable
    RoadMap createRoadMap(String name, World world, boolean findableNodes) {
        return null;
    }

    public Map<Integer, RoadMap> loadRoadMaps() {
        return null;
    }

    public void updateRoadMaps() {

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
                        double tangentLength = SQLUtils.getDouble(resultSet, "tangent_length");
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
                    "(player_id, node_id, date) VALUES " +
                    "(?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);
                SQLUtils.setInt(stmt, 2, nodeId);
                SQLUtils.setDate(stmt, 3, foundDate);
                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();
                    int databaseId = resultSet.getInt(1);
                    return new FoundInfo(databaseId, globalPlayerId, nodeId, foundDate);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Eintragen eines gefundenen Nodes in die Datenbank", e);
        }
        return null;
    }

    public @Nullable
    Map<Integer, FoundInfo> loadFoundNodes(int globalPlayerId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_found` WHERE `player_id` = ?")) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, FoundInfo> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "node_id");
                        int player = SQLUtils.getInt(resultSet, "player_id");
                        int node = SQLUtils.getInt(resultSet, "node_id");
                        Date date = SQLUtils.getDate(resultSet, "date");

                        FoundInfo info = new FoundInfo(id, player, node, date);
                        result.put(id, info);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Pathfinder-Spielerdaten von Spieler-ID: " + globalPlayerId, e);
        }
        return null;
    }

    public void deleteFoundNode(FoundInfo info) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE * FROM `pathfinder_found` WHERE `found_id` = ?")) {
                SQLUtils.setInt(stmt, 1, info.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim löschen der Foundinfo mit ID: " + info.getDatabaseId(), e);
        }
    }


    //visualizerprofile laden

    //visualizerprofile speichern
}
