package pathfinder.data;

import de.bossascrew.core.sql.MySQL;
import de.bossascrew.core.util.SQLUtils;
import lombok.Getter;
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

        //erstelle tabellen
        createFoundNodesTable();
    }

    public void createFoundNodesTable() {
        //TODO möglicherweise foreign key festlegen, falls roadmap oder player oder node gelöscht wird, dass auch datensatz gelöscht wird
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_found` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`player` INT NOT NULL UNIQUE , " +
                    "`node` INT NOT NULL , " +
                    "`date` TIMESTAMP NULL )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der settings-Relation", e);
        }
    }

    //roadmaps laden

    //roadmaps speichern

    //edges tabelle

    public void newEdge(Node nodeA, Node nodeB) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_edges` " +
                    "(node_a, node_b) VALUES (?, ?)")) {
                SQLUtils.setInt(stmt, 1, nodeA.getDatabaseId());
                SQLUtils.setInt(stmt, 2, nodeB.getDatabaseId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Edge in der Pathfinder Datenbank", e);
        }
    }

    public @Nullable
    Map<Integer, Integer> loadEdges

    public @Nullable
    Node newNode(int roadMapId, int groupId, Vector vector, String name, double tangentLength, String permission) {
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
                    n.setNodeGroupId(groupId);
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

                        int id = SQLUtils.getInt(resultSet, "id");
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
                    "(player, node, date) VALUES " +
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
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_found` WHERE `player` = ?")) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, FoundInfo> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "id");
                        int player = SQLUtils.getInt(resultSet, "player");
                        int node = SQLUtils.getInt(resultSet, "node");
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
            try (PreparedStatement stmt = connection.prepareStatement("DELETE * FROM `pathfinder_found` WHERE `id` = ?")) {
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
