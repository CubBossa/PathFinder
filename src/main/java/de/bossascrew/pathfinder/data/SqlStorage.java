package de.bossascrew.pathfinder.data;

import com.google.common.collect.Maps;
import de.bossascrew.core.sql.MySQL;
import de.bossascrew.core.util.Pair;
import de.bossascrew.core.util.SQLUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SqlStorage implements DataStorage {

    private final PathPlugin plugin;

    public SqlStorage(PathPlugin plugin) {
        instance = this;
        this.plugin = plugin;

        createPathVisualizerTable();
        createEditModeVisualizerTable();
        createRoadMapsTable();
        createNodesTable();
        createFindableGroupTable();
        createEdgesTable();
        createFoundNodesTable();
        createFoundGroupsTable();
        createStyleTable();
        createPlayerVisualizerTable();
        createRoadMapStylesTable();
    }

    public void createPathVisualizerTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_path_visualizer` (" +
                    "`path_visualizer_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`name` VARCHAR(36) NOT NULL , " +
                    "`parent_id` INT , " +
                    "`particle` VARCHAR(24) , " +
                    "`particle_limit` INT , " +
                    "`particle_distance` DOUBLE , " +
                    "`particle_steps` INT , " +
                    "`scheduler_period` INT )")) {
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
                    "`parent_id` INT , " +
                    "`particle` VARCHAR(24) , " +
                    "`particle_limit` INT , " +
                    "`particle_distance` DOUBLE , " +
                    "`scheduler_period` INT , " +
                    "`node_head_id` INT , " +
                    "`edge_head_id` INT )")) {
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
                    "`scope` VARCHAR(12) NOT NULL , " +
                    "`group_id` INT NULL , " +
                    "`x` DOUBLE NUll , " +
                    "`y` DOUBLE NUll , " +
                    "`z` DOUBLE NUll , " +
                    "`name` VARCHAR(24) , " +
                    "`tangent_length` DOUBLE NULL , " +
                    "`permission` VARCHAR(30) NULL , " +
                    "FOREIGN KEY (roadmap_id) REFERENCES pathfinder_roadmaps(roadmap_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Nodes-Tabelle", e);
        }
    }

    public void createFindableGroupTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_node_groups` (" +
                    "`group_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "`roadmap_id` INT NOT NULL , " +
                    "`name` VARCHAR(24) NOT NULL , " +
                    "`findable` BOOLEAN NOT NUll , " +
                    "FOREIGN KEY (roadmap_id) REFERENCES pathfinder_roadmaps(roadmap_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Nodegroup-Tabelle", e);
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
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_found_nodes` (" +
                    "`player_id` INT NOT NULL , " +
                    "`found_id` INT NOT NULL , " +
                    "`date` TIMESTAMP NULL , " +
                    "PRIMARY KEY (`player_id`, `found_id`) , " +
                    "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE , " +
                    "FOREIGN KEY (found_id) REFERENCES pathfinder_nodes(node_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der found-nodes Tabelle", e);
        }
    }

    public void createFoundGroupsTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_found_groups` (" +
                    "`player_id` INT NOT NULL , " +
                    "`found_id` INT NOT NULL , " +
                    "`date` TIMESTAMP NULL , " +
                    "PRIMARY KEY (`player_id`, `found_id`) , " +
                    "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE , " +
                    "FOREIGN KEY (found_id) REFERENCES pathfinder_node_groups(group_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der found-groups Tabelle", e);
        }
    }

    public void createStyleTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_path_styles` (" +
                    "`visualizer_id` INT NOT NULL PRIMARY KEY , " +
                    "`name` TEXT NULL , " +
                    "`material` VARCHAR(36) NULL , " +
                    "`permission` VARCHAR(64) NULL , " +
                    "FOREIGN KEY (visualizer_id) REFERENCES pathfinder_path_visualizer(path_visualizer_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der path-styles Tabelle", e);
        }
    }

    public void createPlayerVisualizerTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_player_visualizers` (" +
                    "`player_id` INT NOT NULL , " +
                    "`roadmap_id` INT NOT NULL , " +
                    "`visualizer_id` INT NOT NULL , " +
                    "PRIMARY KEY (`player_id`, `roadmap_id`) , " +
                    "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE , " +
                    "FOREIGN KEY (roadmap_id) REFERENCES pathfinder_roadmaps(roadmap_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der player-visualizer Tabelle", e);
        }
    }

    public void createRoadMapStylesTable() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_roadmap_visualizers` (" +
                    "`roadmap_id` INT NOT NULL , " +
                    "`visualizer_id` INT NOT NULL , " +
                    "PRIMARY KEY (`roadmap_id`, `visualizer_id`) , " +
                    "FOREIGN KEY (visualizer_id) REFERENCES pathfinder_path_visualizer(path_visualizer_id) ON DELETE CASCADE , " +
                    "FOREIGN KEY (roadmap_id) REFERENCES pathfinder_roadmaps(roadmap_id) ON DELETE CASCADE )")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der roadmap-visualizer Tabelle", e);
        }
    }

    public @Nullable
	RoadMap createRoadMap(String name, World world, boolean findableNodes) {
        return createRoadMap(name, world, findableNodes, VisualizerHandler.getInstance().getDefaultPathVisualizer().getDatabaseId(),
                VisualizerHandler.getInstance().getDefaultEditModeVisualizer().getDatabaseId(), 3, 3);
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
                    EditModeVisualizer editModeVisualizer = VisualizerHandler.getInstance().getEditModeVisualizer(editModeVis);

                    return new RoadMap(databaseId, name, world, findableNodes,
                            pathVisualizer, editModeVisualizer, findDist, tangentLength);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Roadmap in der Pathfinder Datenbank", e);
        }
        return null;
    }

    public @Nullable
    Map<NamespacedKey, RoadMap> loadRoadMaps() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_roadmaps`")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, RoadMap> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {

                        int databaseId = SQLUtils.getInt(resultSet, "roadmap_id");
                        String name = SQLUtils.getString(resultSet, "name");
                        String worldName = SQLUtils.getString(resultSet, "world");
                        if (Bukkit.getWorlds().stream().map(World::getName).noneMatch(s -> s.equalsIgnoreCase(worldName))) {
                            continue;
                        }
                        boolean findable = SQLUtils.getBoolean(resultSet, "findable");
                        Integer pathVisId = SQLUtils.getInt(resultSet, "path_visualizer_id");
                        Integer editModeVisId = SQLUtils.getInt(resultSet, "editmode_visualizer_id");
                        double findDistance = SQLUtils.getDouble(resultSet, "node_find_distance");
                        double tangentLength = SQLUtils.getDouble(resultSet, "default_tangent_length");

                        World world = Bukkit.getWorld(worldName);
                        PathVisualizer pathVisualizer = pathVisId == null ? null : VisualizerHandler.getInstance().getPathVisualizer(pathVisId);
                        EditModeVisualizer editModeVisualizer = editModeVisId == null ? null : VisualizerHandler.getInstance().getEditModeVisualizer(editModeVisId);
                        if (world == null) {
                            continue;
                        }
                        if (pathVisualizer == null) {
                            pathVisualizer = VisualizerHandler.getInstance().getDefaultPathVisualizer();
                        }
                        if (editModeVisualizer == null) {
                            editModeVisualizer = VisualizerHandler.getInstance().getDefaultEditModeVisualizer();
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
                    "`path_visualizer_id` = ?, " +
                    "`editmode_visualizer_id` = ?, " +
                    "`node_find_distance` = ?, " +
                    "`default_tangent_length` = ? " +
                    "WHERE `roadmap_id` = ?")) {
                SQLUtils.setString(stmt, 1, roadMap.getNameFormat());
                SQLUtils.setString(stmt, 2, roadMap.getWorld().getName());
                SQLUtils.setBoolean(stmt, 3, roadMap.isFindableNodes());
                SQLUtils.setInt(stmt, 4, roadMap.getPathVisualizer() == null ? null : roadMap.getPathVisualizer().getDatabaseId());
                SQLUtils.setInt(stmt, 5, roadMap.getEditModeVisualizer() == null ? null : roadMap.getEditModeVisualizer().getDatabaseId());
                SQLUtils.setDouble(stmt, 6, roadMap.getNodeFindDistance());
                SQLUtils.setDouble(stmt, 7, roadMap.getDefaultBezierTangentLength());
                SQLUtils.setInt(stmt, 8, roadMap.getKey());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren der RoadMap: " + roadMap.getNameFormat(), e);
        }
    }

    public boolean deleteRoadMap(RoadMap roadMap) {
        return deleteRoadMap(roadMap.getKey());
    }

    public boolean deleteRoadMap(int roadMapId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_roadmaps` WHERE `roadmap_id` = ?")) {
                SQLUtils.setInt(stmt, 1, roadMapId);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen der RoadMap: " + roadMapId, e);
        }
        return false;
    }

    public void newEdge(Waypoint nodeA, Waypoint nodeB) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_edges` " +
                    "(node_a_id, node_b_id) VALUES (?, ?)")) {
                SQLUtils.setInt(stmt, 1, nodeA.getNodeId());
                SQLUtils.setInt(stmt, 2, nodeB.getNodeId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Edge in der Pathfinder Datenbank", e);
        }
    }

    public @Nullable
    Collection<Pair<Integer, Integer>> loadEdges(RoadMap roadMap) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_edges` LEFT JOIN "
                    + "`pathfinder_nodes` ON `pathfinder_edges`.`node_a_id` = `pathfinder_nodes`.`node_id` WHERE `pathfinder_nodes`.`roadmap_id` = ? ")) {
                SQLUtils.setInt(stmt, 1, roadMap.getKey());

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
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Edges zur Roadmap: " + roadMap.getNameFormat(), e);
        }
        return null;
    }

    public void deleteEdge(Pair<Waypoint, Waypoint> edge) {
        deleteEdge(edge.first, edge.second);
    }

    public void deleteEdge(Waypoint a, Waypoint b) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_edges` WHERE ( `node_a_id` = ? AND `node_b_id` = ? ) OR ( `node_a_id` = ? AND `node_b_id` = ? )")) {
                SQLUtils.setInt(stmt, 1, a.getNodeId());
                SQLUtils.setInt(stmt, 2, b.getNodeId());
                SQLUtils.setInt(stmt, 3, b.getNodeId());
                SQLUtils.setInt(stmt, 4, a.getNodeId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen einer Edge", e);
        }
    }

    public @Nullable
    QuestFindable newQuestFindable(RoadMap roadMap, Integer groupId, int npcId, @Nullable String name, Double tangentLength, String permission) {
        return (QuestFindable) createNode(roadMap, QuestFindable.SCOPE, groupId, (double) npcId, null, null, name, tangentLength, permission);
    }

    public @Nullable
    TraderFindable newTraderFindable(RoadMap roadMap, Integer groupId, int npcId, @Nullable String name, Double tangentLength, String permission) {
        return (TraderFindable) createNode(roadMap, TraderFindable.SCOPE, groupId, (double) npcId, null, null, name, tangentLength, permission);
    }

    public @Nullable
	Waypoint createNode(RoadMap roadMap, String scope, Integer groupId, Double x, Double y, Double z, @Nullable String name, Double tangentLength, String permission) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_nodes` " +
                    "(roadmap_id, scope, group_id, x, y, z, name, tangent_length, permission) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setInt(stmt, 1, roadMap.getKey());
                SQLUtils.setString(stmt, 2, scope);
                SQLUtils.setInt(stmt, 3, groupId);
                SQLUtils.setDouble(stmt, 4, x);
                SQLUtils.setDouble(stmt, 5, y);
                SQLUtils.setDouble(stmt, 6, z);
                SQLUtils.setString(stmt, 7, name);
                SQLUtils.setDouble(stmt, 8, tangentLength);
                SQLUtils.setString(stmt, 9, permission);

                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();
                    int databaseId = resultSet.getInt(1);

                    Waypoint ret = null;
                    switch (scope) {
                        case Waypoint.SCOPE:
                            ret = new Waypoint(databaseId, roadMap, name, new Vector(x, y, z));
                            break;
                        case TraderFindable.SCOPE:
                            ret = new TraderFindable(databaseId, roadMap, (int) (x.doubleValue()), name);
                            break;
                        case QuestFindable.SCOPE:
                            ret = new QuestFindable(databaseId, roadMap, (int) (x.doubleValue()), name);
                            break;
                    }
                    ret.setGroup(groupId, false, false);
                    ret.setBezierTangentLength(tangentLength);
                    ret.setPermission(permission);
                    return ret;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Node in der Pathfinder Datenbank", e);
        }
        return null;
    }

    public void deleteNode(int nodeId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_nodes` WHERE `node_id` = ?")) {
                SQLUtils.setInt(stmt, 1, nodeId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen der Node mit ID: " + nodeId, e);
        }
    }

    public void updateFindable(@NotNull Waypoint findable) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_nodes` SET " +
                    "`roadmap_id` = ?, " +
                    "`group_id` = ?, " +
                    "`x` = ?, " +
                    "`y` = ?, " +
                    "`z` = ?, " +
                    "`name` = ?, " +
                    "`tangent_length` = ?, " +
                    "`permission` = ? " +
                    "WHERE `node_id` = ?")) {
                SQLUtils.setInt(stmt, 1, findable.getRoadMapId());
                SQLUtils.setInt(stmt, 2, findable.getNodeGroupId());
                if(findable instanceof NpcFindable) {
                    SQLUtils.setDouble(stmt, 3, (double) ((NpcFindable) findable).getNpcId());
                    SQLUtils.setDouble(stmt, 4, null);
                    SQLUtils.setDouble(stmt, 5, null);
                } else {
                    SQLUtils.setDouble(stmt, 3, findable.getVector().getX());
                    SQLUtils.setDouble(stmt, 4, findable.getVector().getY());
                    SQLUtils.setDouble(stmt, 5, findable.getVector().getZ());
                }
                SQLUtils.setString(stmt, 6, findable.getNameCore());
                SQLUtils.setDouble(stmt, 7, findable.getBezierTangentLength());
                SQLUtils.setString(stmt, 8, findable.getPermission());
                SQLUtils.setInt(stmt, 9, findable.getNodeId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren der Node: " + findable.getNameFormat(), e);
        }
    }

    public Map<Integer, Waypoint> loadNodes(RoadMap roadMap) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_nodes` WHERE `roadmap_id` = ?")) {
                SQLUtils.setInt(stmt, 1, roadMap.getKey());

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, Waypoint> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "node_id");
                        String scope = SQLUtils.getString(resultSet, "scope");
                        Integer groupId = SQLUtils.getInt(resultSet, "group_id");
                        Double x = SQLUtils.getDouble(resultSet, "x");
                        Double y = SQLUtils.getDouble(resultSet, "y");
                        Double z = SQLUtils.getDouble(resultSet, "z");
                        String name = SQLUtils.getString(resultSet, "name");
                        Double tangentLength = SQLUtils.getDouble(resultSet, "tangent_length");
                        String permission = SQLUtils.getString(resultSet, "permission");

                        if(x == null) {
                            continue;
                        }

                        Waypoint ret = null;
                        switch (scope) {
                            case Waypoint.SCOPE:
                                ret = new Waypoint(id, roadMap, name, new Vector(x, y, z));
                                break;
                            case TraderFindable.SCOPE:
                                ret = new TraderFindable(id, roadMap, (int) (x.doubleValue()), name);
                                break;
                            case QuestFindable.SCOPE:
                                ret = new QuestFindable(id, roadMap, (int) (x.doubleValue()), name);
                                break;
                        }
                        ret.setGroup(groupId, false, false);
                        ret.setBezierTangentLength(tangentLength, false);
                        ret.setPermission(permission, false);
                        result.put(id, ret);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Nodes zur Roadmap: " + roadMap.getNameFormat(), e);
        }
        return null;
    }

    public @Nullable
    NodeGroup createNodeGroup(RoadMap roadMap, String name, boolean findable) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_node_groups` " +
                    "(roadmap_id, name, findable) VALUES " +
                    "(?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setInt(stmt, 1, roadMap.getKey());
                SQLUtils.setString(stmt, 2, name);
                SQLUtils.setBoolean(stmt, 3, findable);

                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();
                    int databaseId = resultSet.getInt(1);
                    NodeGroup group = new NodeGroup(databaseId, roadMap, name);
                    group.setFindable(findable);
                    return group;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Ertellen einer Nodegruppe in der Pathfinder Datenbank", e);
        }
        return null;
    }

    public void deleteNodeGroup(NodeGroup group) {
        deleteFindableGroup(group.getGroupId());
    }

    public void deleteNodeGroup(int groupId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_node_groups` WHERE `group_id` = ?")) {
                SQLUtils.setInt(stmt, 1, groupId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen der Nodegruppe mit ID: " + groupId, e);
        }
    }

    public Map<Integer, NodeGroup> loadNodeGroups(RoadMap roadMap) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_node_groups` WHERE `roadmap_id` = ?")) {
                SQLUtils.setInt(stmt, 1, roadMap.getKey());

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, NodeGroup> result = new HashMap<>();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "group_id");
                        String name = SQLUtils.getString(resultSet, "name");
                        boolean findable = SQLUtils.getBoolean(resultSet, "findable");

                        NodeGroup group = new NodeGroup(id, roadMap, name);
                        group.setFindable(findable);
                        result.put(id, group);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Nodegroups zur Roadmap: " + roadMap.getNameFormat(), e);
        }
        return null;
    }

    public void updateNodeGroup(NodeGroup group) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_node_groups` SET " +
                    "`name` = ?, " +
                    "`findable` = ? " +
                    "WHERE `group_id` = ?")) {
                SQLUtils.setString(stmt, 1, group.getNameFormat());
                SQLUtils.setBoolean(stmt, 2, group.isFindable());
                SQLUtils.setInt(stmt, 3, group.getGroupId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren der Nodegroup: " + group.getGroupId(), e);
        }
    }

    public @Nullable
    FoundInfo newFoundInfo(int globalPlayerId, int foundId, boolean group, Date foundDate) {
        return group ?
                newFoundInfo(globalPlayerId, foundId, foundDate, "INSERT INTO `pathfinder_found_groups` " +
                        "(player_id, found_id, date) VALUES (?, ?, ?)") :
                newFoundInfo(globalPlayerId, foundId, foundDate, "INSERT INTO `pathfinder_found_nodes` " +
                        "(player_id, found_id, date) VALUES (?, ?, ?)");
    }

    private @Nullable
    FoundInfo newFoundInfo(int globalPlayerId, int foundId, Date foundDate, String statement) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(statement)) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);
                SQLUtils.setInt(stmt, 2, foundId);
                SQLUtils.setDate(stmt, 3, foundDate);
                stmt.executeUpdate();
                return new FoundInfo(globalPlayerId, foundId, foundDate);
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
    Map<Integer, FoundInfo> loadFoundNodes(int globalPlayerId, boolean group) {
        return group ?
                loadFoundNodes(globalPlayerId, "SELECT * FROM `pathfinder_found_groups` WHERE `player_id` = ?") :
                loadFoundNodes(globalPlayerId, "SELECT * FROM `pathfinder_found_nodes` WHERE `player_id` = ?");
    }

    private @Nullable
    Map<Integer, FoundInfo> loadFoundNodes(int globalPlayerId, String statement) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(statement)) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);

                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, FoundInfo> result = new ConcurrentHashMap<>();
                    while (resultSet.next()) {
                        int player = SQLUtils.getInt(resultSet, "player_id");
                        int node = SQLUtils.getInt(resultSet, "found_id");
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

    public void deleteFoundNode(int globalPlayerId, int nodeId, boolean group) {
        if(group) {
            deleteFoundNode(globalPlayerId, nodeId, "DELETE FROM `pathfinder_found_groups` WHERE `player_id` = ? AND `found_id` = ?");
        } else {
            deleteFoundNode(globalPlayerId, nodeId, "DELETE FROM `pathfinder_found_nodes` WHERE `player_id` = ? AND `found_id` = ?");
        }

    }

    private void deleteFoundNode(int globalPlayerId, int nodeId, String statement) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(statement)) {
                SQLUtils.setInt(stmt, 1, globalPlayerId);
                SQLUtils.setInt(stmt, 2, nodeId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen der Foundinfo für Spieler mit ID: " + globalPlayerId, e);
        }
    }


    public @Nullable
    EditModeVisualizer newEditModeVisualizer(String name, @Nullable EditModeVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit,
                                             @Nullable Integer schedulerPeriod, @Nullable Integer nodeHeadId, @Nullable Integer edgeHeadId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_editmode_visualizer` " +
                    "(name, parent_id, particle, particle_limit, particle_distance, scheduler_period" +
                    ", node_head_id, edge_head_id) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setString(stmt, 1, name);
                SQLUtils.setInt(stmt, 2, parent == null ? null : parent.getDatabaseId());
                SQLUtils.setString(stmt, 3, particle == null ? null : particle.name());
                SQLUtils.setInt(stmt, 4, particleLimit);
                SQLUtils.setDouble(stmt, 5, particleDistance);
                SQLUtils.setInt(stmt, 6, schedulerPeriod);
                SQLUtils.setInt(stmt, 7, nodeHeadId);
                SQLUtils.setInt(stmt, 8, edgeHeadId);

                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();

                    int databaseId = resultSet.getInt(1);
                    EditModeVisualizer vis = new EditModeVisualizer(databaseId, name, parent == null ? null : parent.getDatabaseId());
                    vis.setParent(parent);
                    vis.setParticle(particle);
                    vis.setParticleLimit(particleLimit);
                    vis.setParticleDistance(particleDistance);
                    vis.setSchedulerPeriod(schedulerPeriod);
                    vis.setNodeHeadId(nodeHeadId);
                    vis.setEdgeHeadId(edgeHeadId);
                    return vis;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen des EditModeVisualizers", e);
        }
        return null;
    }

    public @Nullable
    Map<Integer, EditModeVisualizer> loadEditModeVisualizer() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_editmode_visualizer`")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, EditModeVisualizer> result = Maps.newHashMap();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "editmode_visualizer_id");
                        String name = SQLUtils.getString(resultSet, "name");
                        Integer parentId = SQLUtils.getInt(resultSet, "parent_id");
                        String particleName = SQLUtils.getString(resultSet, "particle");
                        Integer particleLimit = SQLUtils.getInt(resultSet, "particle_limit");
                        Double particleDistance = SQLUtils.getDouble(resultSet, "particle_distance");
                        Integer schedulerPeriod = SQLUtils.getInt(resultSet, "scheduler_period");
                        Integer nodeHeadId = SQLUtils.getInt(resultSet, "node_head_id");
                        Integer edgeHeadId = SQLUtils.getInt(resultSet, "edge_head_id");

                        Particle particle = null;
                        if (particleName != null) {
                            try {
                                particle = Particle.valueOf(particleName);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                        EditModeVisualizer vis = new EditModeVisualizer(id, name, parentId);
                        vis.setParticle(particle);
                        vis.setParticleLimit(particleLimit);
                        vis.setParticleDistance(particleDistance);
                        vis.setSchedulerPeriod(schedulerPeriod);
                        vis.setNodeHeadId(nodeHeadId);
                        vis.setEdgeHeadId(edgeHeadId);
                        result.put(id, vis);
                    }
                    for (EditModeVisualizer vis : result.values()) {
                        vis.setParent(result.get(vis.getParentId()));
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
    PathVisualizer newPathVisualizer(String name, @Nullable PathVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit,
                                     @Nullable Integer particleSteps, @Nullable Integer schedulerPeriod) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_path_visualizer` " +
                    "(name, parent_id, particle, particle_limit, particle_distance, particle_steps, scheduler_period) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                SQLUtils.setString(stmt, 1, name);
                SQLUtils.setInt(stmt, 2, parent == null ? null : parent.getDatabaseId());
                SQLUtils.setString(stmt, 3, particle == null ? null : particle.name());
                SQLUtils.setInt(stmt, 4, particleLimit);
                SQLUtils.setDouble(stmt, 5, particleDistance);
                SQLUtils.setInt(stmt, 6, particleSteps);
                SQLUtils.setInt(stmt, 7, schedulerPeriod);

                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();

                    int databaseId = resultSet.getInt(1);
                    PathVisualizer vis = new PathVisualizer(databaseId, name, parent == null ? null : parent.getDatabaseId());
                    vis.setParent(parent);
                    vis.setParticle(particle);
                    vis.setParticleLimit(particleLimit);
                    vis.setParticleDistance(particleDistance);
                    vis.setParticleSteps(particleSteps);
                    vis.setSchedulerPeriod(schedulerPeriod);
                    return vis;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen des PathVisualizers.", e);
        }
        return null;
    }

    public Map<Integer, PathVisualizer> loadPathVisualizer() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_path_visualizer`")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, PathVisualizer> result = Maps.newHashMap();
                    while (resultSet.next()) {
                        int id = SQLUtils.getInt(resultSet, "path_visualizer_id");
                        String name = SQLUtils.getString(resultSet, "name");
                        Integer parentId = SQLUtils.getInt(resultSet, "parent_id");
                        String particleName = SQLUtils.getString(resultSet, "particle");
                        Integer particleLimit = SQLUtils.getInt(resultSet, "particle_limit");
                        Double particleDistance = SQLUtils.getDouble(resultSet, "particle_distance");
                        Integer particleSteps = SQLUtils.getInt(resultSet, "particle_steps");
                        Integer schedulerPeriod = SQLUtils.getInt(resultSet, "scheduler_period");

                        Particle particle = null;
                        if (particleName != null) {
                            try {
                                particle = Particle.valueOf(particleName);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                        PathVisualizer vis = new PathVisualizer(id, name, parentId);
                        vis.setParticle(particle);
                        vis.setParticleLimit(particleLimit);
                        vis.setParticleDistance(particleDistance);
                        vis.setParticleSteps(particleSteps);
                        vis.setSchedulerPeriod(schedulerPeriod);
                        result.put(id, vis);
                    }
                    for (PathVisualizer vis : result.values()) {
                        if (vis.getParentId() == null) {
                            continue;
                        }
                        vis.setParent(result.get(vis.getParentId()));
                    }
                    loadVisualizerStyles(result.values());
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der PathVisualizer", e);
        }
        return null;
    }

    public void updateEditModeVisualizer(EditModeVisualizer visualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_editmode_visualizer` SET " +
                    "`name` = ?, " +
                    "`parent_id` = ?, " +
                    "`particle` = ?, " +
                    "`particle_limit` = ?, " +
                    "`particle_distance` = ?, " +
                    "`scheduler_period` = ?, " +
                    "`node_head_id` = ?, " +
                    "`edge_head_id` = ? " +
                    "WHERE `editmode_visualizer_id` = ?")) {
                SQLUtils.setString(stmt, 1, visualizer.getName());
                SQLUtils.setInt(stmt, 2, visualizer.getParentId());
                SQLUtils.setString(stmt, 3, visualizer.getUnsafeParticle() == null ? null : visualizer.getUnsafeParticle().name());
                SQLUtils.setInt(stmt, 4, visualizer.getUnsafeParticleLimit());
                SQLUtils.setDouble(stmt, 5, visualizer.getUnsafeParticleDistance());
                SQLUtils.setInt(stmt, 6, visualizer.getUnsafeSchedulerPeriod());
                SQLUtils.setInt(stmt, 7, visualizer.getUnsafeNodeHeadId());
                SQLUtils.setInt(stmt, 8, visualizer.getUnsafeEdgeHeadId());
                SQLUtils.setInt(stmt, 9, visualizer.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren des Editmode-Visualizers: " + visualizer.getName(), e);
        }
    }

    public void updatePathVisualizer(PathVisualizer visualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_path_visualizer` SET " +
                    "`name` = ?, " +
                    "`parent_id` = ?, " +
                    "`particle` = ?, " +
                    "`particle_limit` = ?, " +
                    "`particle_distance` = ?, " +
                    "`particle_steps` = ?, " +
                    "`scheduler_period` = ? " +
                    "WHERE `path_visualizer_id` = ?")) {
                SQLUtils.setString(stmt, 1, visualizer.getName());
                SQLUtils.setInt(stmt, 2, visualizer.getParentId());
                SQLUtils.setString(stmt, 3, visualizer.getUnsafeParticle() == null ? null : visualizer.getUnsafeParticle().name());
                SQLUtils.setInt(stmt, 4, visualizer.getUnsafeParticleLimit());
                SQLUtils.setDouble(stmt, 5, visualizer.getUnsafeParticleDistance());
                SQLUtils.setInt(stmt, 6, visualizer.getUnsafeParticleSteps());
                SQLUtils.setInt(stmt, 7, visualizer.getUnsafeSchedulerPeriod());
                SQLUtils.setInt(stmt, 8, visualizer.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren des Path-Visualizers: " + visualizer.getName(), e);
        }
    }

    public void deletePathVisualizer(PathVisualizer visualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_path_visualizer` WHERE `path_visualizer_id` = ?")) {
                SQLUtils.setInt(stmt, 1, visualizer.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen des Path-Visualizers: " + visualizer.getName(), e);
        }
    }

    public void deleteEditModeVisualizer(EditModeVisualizer visualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_editmode_visualizer` WHERE `editmode_visualizer_id` = ?")) {
                SQLUtils.setInt(stmt, 1, visualizer.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen des EditMode-Visualizers: " + visualizer.getName(), e);
        }
    }

    public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_player_visualizers`")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, Map<Integer, Integer>> result = Maps.newHashMap();
                    while (resultSet.next()) {
                        int playerId = SQLUtils.getInt(resultSet, "player_id");
                        int roadmapId = SQLUtils.getInt(resultSet, "roadmap_id");
                        int visualizerId = SQLUtils.getInt(resultSet, "visualizer_id");

                        Map<Integer, Integer> map = result.getOrDefault(playerId, new HashMap<Integer, Integer>());
                        map.put(roadmapId, visualizerId);
                        result.putIfAbsent(playerId, map);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der PathVisualizer", e);
        }
        return null;
    }

    public void createPlayerVisualizer(int playerId, RoadMap roadMap, PathVisualizer visualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_player_visualizers` " +
                    "(player_id, roadmap_id, visualizer_id) VALUES (?, ?, ?)")) {
                SQLUtils.setInt(stmt, 1, playerId);
                SQLUtils.setInt(stmt, 2, roadMap.getKey());
                SQLUtils.setInt(stmt, 3, visualizer.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Eintragen eines Playerstyles in die Datenbank", e);
        }
    }

    public void updatePlayerVisualizer(int playerId, RoadMap roadMap, PathVisualizer visualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_player_visualizers` SET " +
                    "`visualizer_id` = ? " +
                    "WHERE `player_id` = ? AND `roadmap_id` = ?")) {
                SQLUtils.setInt(stmt, 1, visualizer.getDatabaseId());
                SQLUtils.setInt(stmt, 2, playerId);
                SQLUtils.setInt(stmt, 3, roadMap.getKey());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Eintragen eines Playerstyles in die Datenbank", e);
        }
    }

    public void loadVisualizerStyles(Collection<PathVisualizer> visualizers) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_path_styles`")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        Integer databaseId = SQLUtils.getInt(resultSet, "visualizer_id");
                        String name = SQLUtils.getString(resultSet, "name");
                        String type = SQLUtils.getString(resultSet, "material");
                        String perm = SQLUtils.getString(resultSet, "permission");

                        Material m;
                        try {
                            m = Material.valueOf(type);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().log(Level.SEVERE, "Typ nicht gefunden: " + type, e);
                            continue;
                        }
                        PathVisualizer visualizer = visualizers.stream().filter(v -> v.getDatabaseId() == databaseId)
                                .findFirst().orElse(null);
                        if (visualizer == null) {
                            plugin.getLogger().log(Level.SEVERE, "Visualizer nicht gefunden: " + databaseId);
                            continue;
                        }
                        visualizer.setupPickable(perm, name, m, false);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Styles", e);
        }
    }

    public void newVisualizerStyle(PathVisualizer visualizer, @Nullable String permission,
                                   @Nullable Material iconType, @Nullable String miniDisplayName) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_path_styles` " +
                    "(visualizer_id, name, material, permission) VALUES (?, ?, ?, ?)")) {
                SQLUtils.setInt(stmt, 1, visualizer.getDatabaseId());
                SQLUtils.setString(stmt, 2, miniDisplayName);
                SQLUtils.setString(stmt, 3, iconType == null ? null : iconType.toString());
                SQLUtils.setString(stmt, 4, permission);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Eintragen eines Styles in die Datenbank", e);
        }
    }

    public void updateVisualizerStyle(PathVisualizer visualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_path_styles` SET " +
                    "`name` = ?, " +
                    "`material` = ?, " +
                    "`permission` = ? " +
                    "WHERE `visualizer_id` = ?")) {
                SQLUtils.setString(stmt, 1, MiniMessage.get().serialize(visualizer.getDisplayName()));
                SQLUtils.setString(stmt, 2, visualizer.getIconType() == null ? null : visualizer.getIconType().toString());
                SQLUtils.setString(stmt, 3, visualizer.getPickPermission());
                SQLUtils.setInt(stmt, 4, visualizer.getDatabaseId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren des Path-Styles: " + visualizer.getName(), e);
        }
    }

    public void deleteStyleVisualizer(int visualizerId) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_path_styles` WHERE `visualizer_id` = ?")) {
                SQLUtils.setInt(stmt, 1, visualizerId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen des Styles mit ID: " + visualizerId, e);
        }
    }

    public Map<Integer, Collection<PathVisualizer>> loadStyleRoadmapMap(Collection<PathVisualizer> visualizers) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `pathfinder_roadmap_visualizers`")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    Map<Integer, Collection<PathVisualizer>> result = new HashMap<>();
                    while (resultSet.next()) {
                        int visualizerId = SQLUtils.getInt(resultSet, "visualizer_id");
                        int roadmapId = SQLUtils.getInt(resultSet, "roadmap_id");

                        PathVisualizer visualizer = visualizers.stream().filter(v -> v.getDatabaseId() == visualizerId)
                                .findFirst().orElse(null);
                        if (visualizer == null) {
                            plugin.getLogger().log(Level.SEVERE, "Visualizer für Style nicht gefunden: " + visualizerId);
                            continue;
                        }
                        Collection<PathVisualizer> list = result.getOrDefault(roadmapId, new ArrayList<>());
                        list.add(visualizer);
                        result.putIfAbsent(roadmapId, list);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Roadmapstyles", e);
        }
        return null;
    }

    public void addStyleToRoadMap(RoadMap roadMap, PathVisualizer pathVisualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_roadmap_visualizers` " +
                    "(visualizer_id, roadmap_id) VALUES (?, ?)")) {
                SQLUtils.setInt(stmt, 1, pathVisualizer.getDatabaseId());
                SQLUtils.setInt(stmt, 2, roadMap.getKey());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Mappen eines Styles in die Datenbank", e);
        }
    }

    public void removeStyleFromRoadMap(RoadMap roadMap, PathVisualizer pathVisualizer) {
        try (Connection connection = MySQL.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM `pathfinder_roadmap_visualizers` " +
                    "WHERE `visualizer_id` = ? AND `roadmap_id` = ?")) {
                SQLUtils.setInt(stmt, 1, pathVisualizer.getDatabaseId());
                SQLUtils.setInt(stmt, 2, roadMap.getKey());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen der Styleverknüpfung mit ID: " + pathVisualizer, e);
        }
    }
}
