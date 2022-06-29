package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.NodeType;
import de.bossascrew.pathfinder.node.Edge;
import de.bossascrew.pathfinder.node.Findable;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.HashedRegistry;
import de.bossascrew.pathfinder.visualizer.SimpleCurveVisualizer;
import org.bukkit.*;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public abstract class SqlDatabase implements DataStorage {

	abstract Connection getConnection();

	@Override
	public void connect() {
		createRoadMapTable();
		createNodeTable();
		createNodeGroupTable();
		createNodeGroupSearchTermsTable();
		createNodeGroupNodesTable();
		createEdgeTable();
	}

	private void createRoadMapTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_roadmaps` (" +
					"`key` VARCHAR(64) NOT NULL PRIMARY KEY ," +
					"`name_format` TEXT NOT NULL ," +
					"`world` VARCHAR(36) NOT NULL ," +
					"`nodes_findable` TINYINT(1) NULL ," +
					"`path_visualizer` VARCHAR(64) NOT NULL ," + //TODO foreign key
					"`nodes_find_distance` DOUBLE NOT NULL DEFAULT 3 ," +
					"`path_curve_length` DOUBLE NOT NULL DEFAULT 3 )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create roadmap table.", e);
		}
	}

	private void createNodeTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_nodes` (" +
					"`id` INT NOT NULL PRIMARY KEY ," +
					"`type` VARCHAR(64) NOT NULL ," +
					"`roadmap_key` VARCHAR(64) NOT NULL ," +
					"`x` DOUBLE NOT NULL ," +
					"`y` DOUBLE NOT NULL ," +
					"`z` DOUBLE NOT NULL ," +
					"`permission` VARCHAR(64) NULL ," +
					"`path_curve_length` DOUBLE NULL )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create node table.", e);
		}
	}

	private void createEdgeTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_edges` (" +
					"`start_id` INT NOT NULL ," +
					"`end_id` INT NOT NULL ," +
					"`weight_modifier` DOUBLE NOT NULL DEFAULT 1 ," +
					"FOREIGN KEY (start_id) REFERENCES pathfinder_nodes(id) ON DELETE CASCADE ," +
					"FOREIGN KEY (end_id) REFERENCES pathfinder_nodes(id) ON DELETE CASCADE ," +
					"PRIMARY KEY (start_id ,end_id) )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create edge mapping.", e);
		}
	}

	private void createNodeGroupTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_nodegroups` (" +
					"`key` VARCHAR(64) NOT NULL PRIMARY KEY ," +
					"`roadmap_key` VARCHAR(64) NOT NULL ," +
					"`name_format` TEXT NOT NULL ," +
					"`findable` TINYINT(1) NULL ," +
					"FOREIGN KEY (roadmap_key) REFERENCES pathfinder_roadmaps(key) ON DELETE CASCADE )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create node group mapping.", e);
		}
	}

	private void createNodeGroupSearchTermsTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_search_terms` (" +
					"`group_key` VARCHAR(64) NOT NULL ," +
					"`search_term` VARCHAR(64) NOT NULL ," +
					"PRIMARY KEY (group_key, search_term) ," +
					"FOREIGN KEY (group_key) REFERENCES pathfinder_nodegroups(key) ON DELETE CASCADE )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create \"node group <-> search term\" mapping.", e);
		}
	}

	private void createNodeGroupNodesTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_nodegroups_nodes` (" +
					"`group_key` VARCHAR(64) NOT NULL ," +
					"`node_id` INT NOT NULL ," +
					"PRIMARY KEY (group_key, node_id) , " +
					"FOREIGN KEY (group_key) REFERENCES pathfinder_nodegroups(key) ON DELETE CASCADE ," +
					"FOREIGN KEY (node_id) REFERENCES pathfinder_nodes(id) ON DELETE CASCADE )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create \"node group <-> node\" mapping.", e);
		}
	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes) {
		return createRoadMap(key, nameFormat, world, findableNodes, null, 3, 3);
	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, SimpleCurveVisualizer pathVis, double findDist, double tangentLength) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_roadmaps` " +
					"(`key`, `name_format`, `world`, `nodes_findable`, `path_visualizer`, `nodes_find_distance`, `path_curve_length`) VALUES " +
					"(?, ?, ?, ?, ?, ?, ?)")) {
				stmt.setString(1, key.toString());
				stmt.setString(2, nameFormat);
				stmt.setString(3, world.getUID().toString());
				stmt.setBoolean(4, findableNodes);
				stmt.setString(5, pathVis.getKey().toString());
				stmt.setDouble(6, 3);
				stmt.setDouble(7, 3);

				return new RoadMap(key, nameFormat, world, findableNodes, null, 3, 3);
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create new roadmap.", e);
		}
	}

	@Override
	public Map<NamespacedKey, RoadMap> loadRoadMaps() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_roadmaps`")) {
				try (ResultSet resultSet = stmt.executeQuery()) {
					HashedRegistry<RoadMap> registry = new HashedRegistry<>();
					while (resultSet.next()) {
						String keyString = resultSet.getString("key");
						String nameFormat = resultSet.getString("name_format");
						String worldUUIDString = resultSet.getString("world");
						boolean nodesFindable = resultSet.getBoolean("nodes_findable");
						String pathVisualizerKeyString = resultSet.getString("path_visualizer");
						double nodeFindDistance = resultSet.getDouble("nodes_find_distance");
						double pathCurveLength = resultSet.getDouble("path_curve_length");

						registry.put(new RoadMap(NamespacedKey.fromString(keyString),
								nameFormat,
								Bukkit.getWorld(UUID.fromString(worldUUIDString)),
								nodesFindable, null, nodeFindDistance, pathCurveLength));
					}
					return registry;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load roadmaps", e);
		}
	}

	@Override
	public void updateRoadMap(RoadMap roadMap) {
		try (Connection connection = getConnection()) {
			try (PreparedStatement stmt = connection.prepareStatement("UPDATE `pathfinder_roadmaps` SET " +
					"`name_format` = ?," +
					"`world` = ?," +
					"`nodes_findable` = ?," +
					"`path_visualizer` = ?," +
					"`nodes_find_distance` = ?," +
					"`path_curve_length` = ?" +
					"WHERE `key` = ?")) {
				stmt.setString(1, roadMap.getNameFormat());
				stmt.setString(2, roadMap.getWorld().getUID().toString());
				stmt.setBoolean(3, roadMap.isFindableNodes());
				stmt.setString(4, roadMap.getVisualizer().getKey().toString());
				stmt.setDouble(5, roadMap.getNodeFindDistance());
				stmt.setDouble(6, roadMap.getDefaultBezierTangentLength());
				stmt.setString(7, roadMap.getKey().toString());
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DataStorageException("Could not update roadmap.", e);
		}
	}

	@Override
	public boolean deleteRoadMap(RoadMap roadMap) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_roadmaps` WHERE `key` = ?")) {
				stmt.setString(1, roadMap.getKey().toString());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete roadmap.", e);
		}
		return true;
	}

	@Override
	public boolean deleteRoadMap(NamespacedKey key) {
		return false;
	}

	@Override
	public Edge createEdge(Node start, Node end, float weight) {
		return null;
	}

	@Override
	public Collection<Edge> loadEdges(RoadMap roadMap) {
		return null;
	}

	@Override
	public void deleteEdge(Edge edge) {

	}

	@Override
	public void deleteEdge(Node start, Node end) {

	}

	@Override
	public <T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, Double x, Double y, Double z, Double tangentLength, String permission) {
		return null;
	}

	@Override
	public Map<Integer, Node> loadNodes(RoadMap roadMap) {
		return null;
	}

	@Override
	public void updateNode(Node node) {

	}

	@Override
	public void deleteNode(int nodeId) {

	}

	@Override
	public NodeGroup createNodeGroup(RoadMap roadMap, NamespacedKey key, String nameFormat, boolean findable) {
		return null;
	}

	@Override
	public Map<NamespacedKey, NodeGroup> loadNodeGroups(RoadMap roadMap) {
		return null;
	}

	@Override
	public void updateNodeGroup(NodeGroup group) {

	}

	@Override
	public void deleteNodeGroup(NodeGroup group) {

	}

	@Override
	public void deleteNodeGroup(NamespacedKey key) {

	}

	@Override
	public FoundInfo createFoundInfo(UUID player, Findable findable, Date foundDate) {
		return null;
	}

	@Override
	public Map<Integer, FoundInfo> loadFoundInfo(int globalPlayerId, boolean group) {
		return null;
	}

	@Override
	public void deleteFoundInfo(int globalPlayerId, int nodeId, boolean group) {

	}

	@Override
	public SimpleCurveVisualizer newPathVisualizer(NamespacedKey key, String nameFormat, Particle particle, Double particleDistance, Integer particleSteps, Integer schedulerPeriod) {
		return null;
	}

	@Override
	public Map<Integer, SimpleCurveVisualizer> loadPathVisualizer() {
		return null;
	}

	@Override
	public void updatePathVisualizer(SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void deletePathVisualizer(SimpleCurveVisualizer visualizer) {

	}

	@Override
	public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
		return null;
	}

	@Override
	public void createPlayerVisualizer(int playerId, RoadMap roadMap, SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void updatePlayerVisualizer(int playerId, RoadMap roadMap, SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void loadVisualizerStyles(Collection<SimpleCurveVisualizer> visualizers) {

	}

	@Override
	public void newVisualizerStyle(SimpleCurveVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName) {

	}

	@Override
	public void updateVisualizerStyle(SimpleCurveVisualizer visualizer) {

	}

	@Override
	public void deleteStyleVisualizer(int visualizerId) {

	}

	@Override
	public Map<Integer, Collection<SimpleCurveVisualizer>> loadStyleRoadmapMap(Collection<SimpleCurveVisualizer> visualizers) {
		return null;
	}

	@Override
	public void addStyleToRoadMap(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer) {

	}

	@Override
	public void removeStyleFromRoadMap(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer) {

	}
}
