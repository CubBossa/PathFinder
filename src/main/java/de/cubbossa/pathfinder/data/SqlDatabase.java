package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.*;

public abstract class SqlDatabase implements DataStorage {

	abstract Connection getConnection();

	@Override
	public void connect(Runnable initial) throws IOException {
		createPathVisualizerTable();
		createRoadMapTable();
		createNodeTable();
		createNodeGroupTable();
		createNodeGroupSearchTermsTable();
		createNodeGroupNodesTable();
		createEdgeTable();
		createDiscoverInfoTable();
	}

	private void createRoadMapTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_roadmaps` (" +
					"`key` VARCHAR(64) NOT NULL PRIMARY KEY ," +
					"`name_format` TEXT NOT NULL ," +
					"`path_visualizer` VARCHAR(64) NULL ," +
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
					"`id` INTEGER PRIMARY KEY AUTOINCREMENT ," +
					"`type` VARCHAR(64) NOT NULL ," +
					"`roadmap_key` VARCHAR(64) NOT NULL ," +
					"`x` DOUBLE NOT NULL ," +
					"`y` DOUBLE NOT NULL ," +
					"`z` DOUBLE NOT NULL ," +
					"`world` VARCHAR(36) NOT NULL ," +
					"`path_curve_length` DOUBLE NULL ," +
					"FOREIGN KEY (roadmap_key) REFERENCES pathfinder_roadmaps(key) ON DELETE CASCADE )")) {
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
					"`name_format` TEXT NOT NULL ," +
					"`permission` VARCHAR(64) NULL ," +
					"`navigable` BOOLEAN NOT NULL ," +
					"`discoverable` BOOLEAN NOT NULL ," +
					"`find_distance` DOUBLE NOT NULL " +
					")")) {
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

	private void createPathVisualizerTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_path_visualizer` (" +
					"`key` VARCHAR(64) NOT NULL PRIMARY KEY ," +
					"`type` VARCHAR(64) NOT NULL ," +
					"`name_format` TEXT NOT NULL ," +
					"`permission` VARCHAR(64) NULL ," +
					"`interval` INT NOT NULL ," +
					"`data` TEXT NULL )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create path visualizer table.", e);
		}
	}

	private void createDiscoverInfoTable() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_discoverings` (" +
					"`discover_key` VARCHAR(64) NOT NULL ," +
					"`player_id` VARCHAR(36) NOT NULL ," +
					"`date` TIMESTAMP NOT NULL ," +
					"PRIMARY KEY (discover_key, player_id) )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create discoverings table.", e);
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
						String pathVisualizerKeyString = resultSet.getString("path_visualizer");
						double pathCurveLength = resultSet.getDouble("path_curve_length");

						registry.put(new RoadMap(
								NamespacedKey.fromString(keyString),
								nameFormat,
								pathVisualizerKeyString == null ? null : VisualizerHandler.getInstance().getPathVisualizer(NamespacedKey.fromString(pathVisualizerKeyString)),
								pathCurveLength));
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
			try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_roadmaps`" +
					"(`key`, `name_format`, `path_visualizer`, `path_curve_length`) VALUES (?, ?, ?, ?) " +
					"ON CONFLICT(`key`) DO UPDATE SET " +
					"`name_format` = ?, " +
					"`path_visualizer` = ?, " +
					"`path_curve_length` = ?")) {
				stmt.setString(1, roadMap.getKey().toString());
				stmt.setString(2, roadMap.getNameFormat());
				if (roadMap.getVisualizer() == null) {
					stmt.setNull(3, Types.VARCHAR);
				} else {
					stmt.setString(3, roadMap.getVisualizer().getKey().toString());
				}
				stmt.setDouble(4, roadMap.getDefaultCurveLength());
				stmt.setString(5, roadMap.getNameFormat());
				if (roadMap.getVisualizer() == null) {
					stmt.setNull(6, Types.VARCHAR);
				} else {
					stmt.setString(6, roadMap.getVisualizer().getKey().toString());
				}
				stmt.setDouble(7, roadMap.getDefaultCurveLength());
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DataStorageException("Could not update roadmap.", e);
		}
	}

	@Override
	public boolean deleteRoadMap(NamespacedKey key) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_roadmaps` WHERE `key` = ?")) {
				stmt.setString(1, key.toString());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete roadmap.", e);
		}
		return true;
	}

	@Override
	public void saveEdges(Collection<Edge> edges) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);

			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_edges` " +
					"(`start_id`, `end_id`, `weight_modifier`) VALUES " +
					"(?, ?, ?)")) {
				for (var edge : edges) {
					stmt.setInt(1, edge.getStart().getNodeId());
					stmt.setInt(2, edge.getEnd().getNodeId());
					stmt.setDouble(3, edge.getWeightModifier());
					stmt.addBatch();
				}
				stmt.executeBatch();
			}

			con.commit();
			con.setAutoCommit(wasAuto);
		} catch (Exception e) {
			throw new DataStorageException("Could not create new edge.", e);
		}
	}

	@Override
	public Collection<Edge> loadEdges(RoadMap roadMap, Map<Integer, Node> scope) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_edges` pe " +
					"JOIN pathfinder_nodes pn ON pn.id = pe.start_id " +
					"WHERE `pn`.`roadmap_key` = ?")) {
				stmt.setString(1, roadMap.getKey().toString());

				try (ResultSet resultSet = stmt.executeQuery()) {
					HashSet<Edge> edges = new HashSet<>();
					while (resultSet.next()) {
						int startId = resultSet.getInt("start_id");
						int endId = resultSet.getInt("end_id");
						double weight = resultSet.getDouble("weight_modifier");

						Node start = roadMap.getNode(startId);
						Node end = roadMap.getNode(endId);

						if (start == null || end == null) {
							deleteEdge(start, end);
						}
						edges.add(new Edge(start, end, (float) weight));
					}
					return edges;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load edges.", e);
		}
	}

	@Override
	public void deleteEdgesFrom(Node start) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_edges` WHERE `start_id` = ?")) {
				stmt.setInt(1, start.getNodeId());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete edges.", e);
		}
	}

	@Override
	public void deleteEdgesTo(Node end) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_edges` WHERE `end_id` = ?")) {
				stmt.setInt(1, end.getNodeId());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete edges.", e);
		}
	}

	public void deleteEdge(Node start, Node end) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_edges` WHERE `start_id` = ? AND `end_id` = ?")) {
				stmt.setInt(1, start.getNodeId());
				stmt.setInt(2, end.getNodeId());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete edge.", e);
		}
	}

	@Override
	public void deleteEdges(Collection<Edge> edges) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);

			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_edges` WHERE `start_id` = ? AND `end_id` = ?")) {
				for (Edge edge : edges) {
					stmt.setInt(1, edge.getStart().getNodeId());
					stmt.setInt(2, edge.getEnd().getNodeId());
					stmt.addBatch();
				}
				stmt.executeUpdate();

				stmt.executeBatch();
			}

			con.commit();
			con.setAutoCommit(wasAuto);
		} catch (Exception e) {
			throw new DataStorageException("Could not delete edge.", e);
		}
	}

	@Override
	public Map<Integer, Node> loadNodes(RoadMap roadMap) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_nodes` WHERE `roadmap_key` = ?")) {
				stmt.setString(1, roadMap.getKey().toString());

				try (ResultSet resultSet = stmt.executeQuery()) {
					Map<Integer, Node> nodes = new LinkedHashMap<>();
					while (resultSet.next()) {
						int id = resultSet.getInt("id");
						String type = resultSet.getString("type");
						double x = resultSet.getDouble("x");
						double y = resultSet.getDouble("y");
						double z = resultSet.getDouble("z");
						String worldUid = resultSet.getString("world");
						Double curveLength = resultSet.getDouble("path_curve_length");
						if (resultSet.wasNull()) {
							curveLength = null;
						}

						NodeType<?> nodeType = NodeTypeHandler.getInstance().getNodeType(NamespacedKey.fromString(type));
						Node node = nodeType.getFactory().apply(new NodeType.NodeCreationContext(
								roadMap,
								id,
								new Location(Bukkit.getWorld(UUID.fromString(worldUid)), x, y, z)
						));
						node.setCurveLength(curveLength);
						nodes.put(id, node);
					}
					return nodes;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load nodes.", e);
		}
	}

	@Override
	public void updateNode(Node node) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_nodes` " +
					"(`id`, `type`, `roadmap_key`, `x`, `y`, `z`, `world`, `path_curve_length`) VALUES " +
					"(?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(`id`) DO UPDATE SET " +
					"`x` = ?, " +
					"`y` = ?, " +
					"`z` = ?, " +
					"`world` = ?, " +
					"`path_curve_length` = ?")) {
				stmt.setInt(1, node.getNodeId());
				stmt.setString(2, node.getType().getKey().toString());
				stmt.setString(3, node.getRoadMapKey().toString());
				stmt.setDouble(4, node.getLocation().getX());
				stmt.setDouble(5, node.getLocation().getY());
				stmt.setDouble(6, node.getLocation().getZ());
				stmt.setString(7, node.getLocation().getWorld().getUID().toString());
				if (node.getCurveLength() == null) {
					stmt.setNull(8, Types.DOUBLE);
				} else {
					stmt.setDouble(8, node.getCurveLength());
				}
				stmt.setDouble(9, node.getLocation().getX());
				stmt.setDouble(10, node.getLocation().getY());
				stmt.setDouble(11, node.getLocation().getZ());
				stmt.setString(12, node.getLocation().getWorld().getUID().toString());
				if (node.getCurveLength() == null) {
					stmt.setNull(13, Types.DOUBLE);
				} else {
					stmt.setDouble(13, node.getCurveLength());
				}
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not update node.", e);
		}
	}

	@Override
	public void deleteNodes(Integer... nodeIds) {
		deleteNodes(Arrays.asList(nodeIds));
	}

	@Override
	public void deleteNodes(Collection<Integer> nodeIds) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);

			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_nodes` WHERE `id` = ?")) {
				for (int id : nodeIds) {
					stmt.setInt(1, id);
					stmt.addBatch();
				}
				stmt.executeBatch();
			}

			con.commit();
			con.setAutoCommit(wasAuto);
		} catch (Exception e) {
			throw new DataStorageException("Could not delete node.", e);
		}
	}

	@Override
	public void assignNodesToGroup(NodeGroup group, NodeSelection selection) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);

			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_nodegroups_nodes` " +
					"(`group_key`, `node_id`) VALUES (?, ?) ON CONFLICT(`group_key`, `node_id`) DO NOTHING")) {
				for (Node node : selection) {
					stmt.setString(1, group.getKey().toString());
					stmt.setInt(2, node.getNodeId());
					stmt.addBatch();
				}
				stmt.executeBatch();
			}
			con.commit();
			con.setAutoCommit(wasAuto);
		} catch (Exception e) {
			throw new DataStorageException("Could not add node to group.", e);
		}
	}

	@Override
	public void removeNodesFromGroup(NodeGroup group, Iterable<Groupable> selection) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);

			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_nodegroups_nodes` " +
					"WHERE `group_key` = ? AND `node_id` = ?")) {
				for (Node node : selection) {
					stmt.setString(1, group.getKey().toString());
					stmt.setInt(2, node.getNodeId());
					stmt.addBatch();
				}
				stmt.executeBatch();
			}
			con.commit();
			con.setAutoCommit(wasAuto);
		} catch (Exception e) {
			throw new DataStorageException("Could not add node to group.", e);
		}
	}

	@Override
	public Map<Integer, ? extends Collection<NamespacedKey>> loadNodeGroupNodes() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_nodegroups_nodes`")) {
				try (ResultSet resultSet = stmt.executeQuery()) {
					Map<Integer, HashSet<NamespacedKey>> registry = new LinkedHashMap<>();
					while (resultSet.next()) {
						String keyString = resultSet.getString("group_key");
						int nodeId = resultSet.getInt("node_id");

						HashSet<NamespacedKey> l = registry.computeIfAbsent(nodeId, id -> new HashSet<>());
						l.add(NamespacedKey.fromString(keyString));
					}
					return registry;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load node group nodes.", e);
		}
	}

	@Override
	public HashedRegistry<NodeGroup> loadNodeGroups() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_nodegroups`")) {
				try (ResultSet resultSet = stmt.executeQuery()) {
					HashedRegistry<NodeGroup> registry = new HashedRegistry<>();
					while (resultSet.next()) {
						String keyString = resultSet.getString("key");
						String nameFormat = resultSet.getString("name_format");
						String permission = resultSet.getString("permission");
						boolean navigable = resultSet.getBoolean("navigable");
						boolean discoverable = resultSet.getBoolean("discoverable");
						double findDistance = resultSet.getDouble("find_distance");

						NodeGroup group = new NodeGroup(NamespacedKey.fromString(keyString), nameFormat);
						group.setPermission(permission);
						group.setNavigable(navigable);
						group.setDiscoverable(discoverable);
						group.setFindDistance((float) findDistance);
						registry.put(group);
					}
					return registry;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load node groups.", e);
		}
	}

	@Override
	public void updateNodeGroup(NodeGroup group) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_nodegroups` " +
					"(`key`, `name_format`, `permission`, `navigable`, `discoverable`, `find_distance`) VALUES (?, ?, ?, ?, ?, ?)" +
					"ON CONFLICT(`key`) DO UPDATE SET " +
					"`name_format` = ?, " +
					"`permission` = ?, " +
					"`navigable` = ?, " +
					"`discoverable` = ?, " +
					"`find_distance` = ?")) {
				stmt.setString(1, group.getKey().toString());
				stmt.setString(2, group.getNameFormat());
				stmt.setString(3, group.getPermission());
				stmt.setBoolean(4, group.isNavigable());
				stmt.setBoolean(5, group.isDiscoverable());
				stmt.setDouble(6, group.getFindDistance());
				stmt.setString(7, group.getNameFormat());
				stmt.setString(8, group.getPermission());
				stmt.setBoolean(9, group.isNavigable());
				stmt.setBoolean(10, group.isDiscoverable());
				stmt.setDouble(11, group.getFindDistance());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not update node group.", e);
		}
	}

	@Override
	public void deleteNodeGroup(NamespacedKey key) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_nodegroups` WHERE `key` = ?")) {
				stmt.setString(1, key.toString());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete node group.", e);
		}
	}

	@Override
	public Map<NamespacedKey, Collection<String>> loadSearchTerms() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_search_terms`")) {
				try (ResultSet resultSet = stmt.executeQuery()) {
					Map<NamespacedKey, Collection<String>> registry = new HashMap<>();
					while (resultSet.next()) {
						String keyString = resultSet.getString("group_key");
						String searchTerm = resultSet.getString("search_term");

						Collection<String> l = registry.computeIfAbsent(NamespacedKey.fromString(keyString), key -> new HashSet<>());
						l.add(searchTerm);
					}
					return registry;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load search terms.", e);
		}
	}

	@Override
	public void addSearchTerms(NodeGroup group, Collection<String> searchTerms) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);

			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_search_terms` " +
					"(`group_key`, `search_term`) VALUES (?, ?)")) {
				for (String term : searchTerms) {
					stmt.setString(1, group.getKey().toString());
					stmt.setString(2, term);
					stmt.addBatch();
				}
				stmt.executeBatch();
			}
			con.commit();
			con.setAutoCommit(wasAuto);
		} catch (Exception e) {
			throw new DataStorageException("Could not add search terms.", e);
		}
	}

	@Override
	public void removeSearchTerms(NodeGroup group, Collection<String> searchTerms) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_search_terms` WHERE `group_key` = ? AND `search_term` = ?")) {

				for (String term : searchTerms) {
					stmt.setString(1, group.getKey().toString());
					stmt.setString(2, term);
					stmt.addBatch();
				}
				stmt.executeBatch();
			}
			con.commit();
			con.setAutoCommit(wasAuto);
		} catch (Exception e) {
			throw new DataStorageException("Could not remove search terms.", e);
		}
	}

	@Override
	public DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, Date foundDate) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_discoverings` " +
					"(`discover_key`, `player_id`, `date`) VALUES (?, ?, ?)")) {
				stmt.setString(1, discoverable.getKey().toString());
				stmt.setString(2, player.toString());
				stmt.setTimestamp(3, Timestamp.from(foundDate.toInstant()));
				stmt.executeUpdate();

				return new DiscoverInfo(player, discoverable.getKey(), foundDate);
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create new discover info.", e);
		}
	}

	@Override
	public Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_discoverings` WHERE `player_id` = ?")) {
				stmt.setString(1, playerId.toString());
				try (ResultSet resultSet = stmt.executeQuery()) {
					Map<NamespacedKey, DiscoverInfo> registry = new HashMap<>();
					while (resultSet.next()) {
						String keyString = resultSet.getString("discover_key");
						Date date = resultSet.getTimestamp("date");

						DiscoverInfo info = new DiscoverInfo(playerId, NamespacedKey.fromString(keyString), date);
						registry.put(info.discoverable(), info);
					}
					return registry;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load discoverings.", e);
		}
	}

	@Override
	public void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_discoverings` WHERE `player_id` = ? AND `discover_key` = ?")) {
				stmt.setString(1, playerId.toString());
				stmt.setString(2, discoverKey.toString());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete discovering.", e);
		}
	}

	@Override
	public Map<NamespacedKey, PathVisualizer<?, ?>> loadPathVisualizer() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_path_visualizer`")) {
				try (ResultSet resultSet = stmt.executeQuery()) {
					Map<PathVisualizer, String> dataMap = new HashMap<>();
					while (resultSet.next()) {
						String keyString = resultSet.getString("key");
						String typeString = resultSet.getString("type");
						String nameFormat = resultSet.getString("name_format");
						int interval = resultSet.getInt("interval");
						String permission = resultSet.getString("permission");
						String data = resultSet.getString("data");

						VisualizerType<?> type = VisualizerHandler.getInstance().getVisualizerType(NamespacedKey.fromString(typeString));
						PathVisualizer<?, ?> visualizer = type.create(NamespacedKey.fromString(keyString), nameFormat);
						visualizer.setInterval(interval);
						visualizer.setPermission(permission);
						dataMap.put(visualizer, data);
					}
					HashedRegistry<PathVisualizer<?, ?>> registry = new HashedRegistry<>();
					dataMap.forEach((visualizer, s) -> {
						YamlConfiguration cfg = new YamlConfiguration();
						try {
							cfg.loadFromString(s);
						} catch (InvalidConfigurationException e) {
							e.printStackTrace();
						}
						visualizer.getType().deserialize(visualizer, cfg.getValues(false));
						registry.put(visualizer);
					});
					return registry;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load visualizers.", e);
		}
	}

	@Override
	public <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer) {
		Map<String, Object> data = visualizer.getType().serialize(visualizer);
		if (data == null) {
			return;
		}
		YamlConfiguration cfg = new YamlConfiguration();
		data.forEach(cfg::set);
		String dataString = cfg.saveToString();

		try (Connection connection = getConnection()) {
			try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `pathfinder_path_visualizer` " +
					"( `key`, `type`, `name_format`, `interval`, `permission`, `data`) VALUES (?, ?, ?, ?, ?, ?) " +
					"ON CONFLICT(`key`) DO UPDATE SET " +
					"`name_format` = ?, " +
					"`interval` = ?, " +
					"`permission` = ?, " +
					"`data` = ? ")) {
				stmt.setString(1, visualizer.getKey().toString());
				stmt.setString(2, visualizer.getType().getKey().toString());
				stmt.setString(3, visualizer.getNameFormat());
				stmt.setInt(4, visualizer.getInterval());
				stmt.setString(5, visualizer.getPermission());
				stmt.setString(6, dataString);
				stmt.setString(7, visualizer.getNameFormat());
				stmt.setInt(8, visualizer.getInterval());
				stmt.setString(9, visualizer.getPermission());
				stmt.setString(10, dataString);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DataStorageException("Could not update pathvisualizer.", e);
		}
	}

	@Override
	public void deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_path_visualizer` WHERE `key` = ?")) {
				stmt.setString(1, visualizer.getKey().toString());
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not delete path visualizer.", e);
		}
	}

	@Override
	public Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers() {
		return null;
	}

	@Override
	public void updatePlayerVisualizer(int playerId, RoadMap roadMap, ParticleVisualizer visualizer) {

	}

	@Override
	public void loadVisualizerStyles(Collection<ParticleVisualizer> visualizers) {

	}

	@Override
	public void newVisualizerStyle(ParticleVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName) {

	}

	@Override
	public void updateVisualizerStyle(ParticleVisualizer visualizer) {

	}

	@Override
	public void deleteStyleVisualizer(int visualizerId) {

	}

	@Override
	public Map<Integer, Collection<ParticleVisualizer>> loadStyleRoadmapMap(Collection<ParticleVisualizer> visualizers) {
		return null;
	}

	@Override
	public void addStyleToRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer) {

	}

	@Override
	public void removeStyleFromRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer) {

	}
}
