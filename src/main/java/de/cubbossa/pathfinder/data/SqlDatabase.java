package de.cubbossa.pathfinder.data;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.DataUtils;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Triple;
import xyz.xenondevs.particle.ParticleBuilder;

import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class SqlDatabase implements DataStorage {

	abstract Connection getConnection();

	@Override
	public void connect() {
		createParticleVisualizerDatabase();
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
					"`path_visualizer` VARCHAR(64) NOT NULL ," +
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
					"`id` INTEGER PRIMARY KEY AUTOINCREMENT ," +
					"`type` VARCHAR(64) NOT NULL ," +
					"`roadmap_key` VARCHAR(64) NOT NULL ," +
					"`x` DOUBLE NOT NULL ," +
					"`y` DOUBLE NOT NULL ," +
					"`z` DOUBLE NOT NULL ," +
					"`permission` VARCHAR(64) NULL ," +
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
					"`findable` TINYINT(1) NULL )")) {
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

	private void createParticleVisualizerDatabase() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `pathfinder_path_visualizer` (" +
					"`key` VARCHAR(64) NOT NULL PRIMARY KEY ," +
					"`name_format` TEXT NOT NULL ," +
					"`permission` VARCHAR(64) NULL ," +
					"`display_item` TEXT NOT NULL ," +
					"`particle` VARCHAR(128) NOT NULL ," +
					"`particle_steps` INT NOT NULL ," +
					"`particle_distance` DOUBLE NOT NULL ," +
					"`scheduler_period` INT NOT NULL ," +
					"`curve_length` DOUBLE NOT NULL )")) {
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create path visualizer table.", e);
		}
	}

	@Override
	public RoadMap createRoadMap(NamespacedKey key, String nameFormat, World world, boolean findableNodes, PathVisualizer<?> pathVis, double findDist, double tangentLength) {
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
				stmt.executeUpdate();

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
								nodesFindable,
								VisualizerHandler.getInstance().getDefaultVisualizer(),
								//VisualizerHandler.getInstance().getPathVisualizerMap().get(NamespacedKey.fromString(pathVisualizerKeyString)),
								nodeFindDistance, pathCurveLength));
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
					"`name_format` = ?, " +
					"`world` = ?, " +
					"`nodes_findable` = ?, " +
					"`path_visualizer` = ?, " +
					"`nodes_find_distance` = ?, " +
					"`path_curve_length` = ?, " +
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
	public Edge createEdge(Node start, Node end, float weight) {
		return createEdges(Lists.newArrayList(new Triple<>(start, end, weight))).get(0);
	}

	@Override
	public List<Edge> createEdges(List<Triple<Node, Node, Float>> edges) {
		try (Connection con = getConnection()) {
			boolean wasAuto = con.getAutoCommit();
			con.setAutoCommit(false);

			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_edges` " +
					"(`start_id`, `end_id`, `weight_modifier`) VALUES " +
					"(?, ?, ?)")) {
				for (var triple : edges) {
					stmt.setInt(1, triple.getFirst().getNodeId());
					stmt.setInt(2, triple.getSecond().getNodeId());
					stmt.setDouble(3, triple.getThird());
					stmt.addBatch();
				}
				stmt.executeBatch();
			}

			con.commit();
			con.setAutoCommit(wasAuto);

			return edges.stream().map(t -> new Edge(t.getFirst(), t.getSecond(), t.getThird())).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DataStorageException("Could not create new edge.", e);
		}
	}

	@Override
	public Collection<Edge> loadEdges(RoadMap roadMap) {
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
							deleteEdge(startId, endId);
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

	public void deleteEdge(int start, int end) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `pathfinder_edges` WHERE `start_id` = ? AND `end_id` = ?")) {
				stmt.setInt(1, start);
				stmt.setInt(2, end);
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
	public <T extends Node> T createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, double x, double y, double z, Double tangentLength, String permission) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_nodes` " +
					"(`type`, `roadmap_key`, `x`, `y`, `z`, `permission`, `path_curve_length`) VALUES " +
					"(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
				stmt.setString(1, type.getKey().toString());
				stmt.setString(2, roadMap.getKey().toString());
				stmt.setDouble(3, x);
				stmt.setDouble(4, y);
				stmt.setDouble(5, z);
				stmt.setString(6, permission);
				if (tangentLength == null) {
					stmt.setNull(7, Types.DOUBLE);
				} else {
					stmt.setDouble(7, tangentLength);
				}
				stmt.executeUpdate();

				try (ResultSet res = stmt.getGeneratedKeys()) {
					T node = type.getFactory().apply(roadMap, res.getInt(1));
					node.setPosition(new Vector(x, y, z));
					node.setPermission(permission);
					node.setCurveLength(tangentLength);
					if (node instanceof Groupable groupable) {
						groups.forEach(groupable::addGroup);
					}
					return node;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create new node.", e);
		}
	}

	@Override
	public NodeBatchCreator newNodeBatch() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_nodes` " +
					"(`type`, `roadmap_key`, `x`, `y`, `z`, `permission`, `path_curve_length`) VALUES " +
					"(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

				AtomicBoolean closed = new AtomicBoolean(false);
				List<Function<Integer, Node>> nodes = new ArrayList<>();

				return new NodeBatchCreator() {

					@Override
					public <T extends Node> void createNode(RoadMap roadMap, NodeType<T> type, Collection<NodeGroup> groups, double x, double y, double z, Double tangentLength, String permission) throws SQLException {
						if (closed.get()) {
							throw new IllegalStateException("BatchCreator already closed");
						}
						stmt.setString(1, type.getKey().toString());
						stmt.setString(2, roadMap.getKey().toString());
						stmt.setDouble(3, x);
						stmt.setDouble(4, y);
						stmt.setDouble(5, z);
						stmt.setString(6, permission);
						stmt.setDouble(7, tangentLength);
						stmt.addBatch();

						nodes.add(integer -> {
							T node = type.getFactory().apply(roadMap, integer);
							node.setPermission(permission);
							node.setPosition(new Vector(x, y, z));
							node.setCurveLength(tangentLength);
							if (node instanceof Groupable groupable) {
								groups.forEach(groupable::addGroup);
							}
							return node;
						});

					}

					@Override
					public Collection<? extends Node> commit() throws SQLException {
						if (closed.get()) {
							throw new IllegalStateException("BatchCreator already closed");
						}
						List<Node> nodeList = new ArrayList<>();

						stmt.executeBatch();
						try (ResultSet res = stmt.getGeneratedKeys()) {
							int counter = 0;
							while (res.next()) {
								nodes.get(counter++).apply(res.getInt(1));
							}
						}
						closed.set(true);
						return nodeList;
					}
				};
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create new node.", e);
		}
	}

	@Override
	public Map<Integer, Node> loadNodes(RoadMap roadMap) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_nodes` WHERE `roadmap_key` = ?")) {
				stmt.setString(1, roadMap.getKey().toString());

				try (ResultSet resultSet = stmt.executeQuery()) {
					Map<Integer, Node> nodes = new TreeMap<>();
					while (resultSet.next()) {
						int id = resultSet.getInt("id");
						String type = resultSet.getString("type");
						double x = resultSet.getDouble("x");
						double y = resultSet.getDouble("y");
						double z = resultSet.getDouble("z");
						String permission = resultSet.getString("permission");
						double curveLength = resultSet.getDouble("path_curve_length");

						NodeType<?> nodeType = NodeTypeHandler.getInstance().getNodeType(NamespacedKey.fromString(type));
						Node node = nodeType.getFactory().apply(roadMap, id);
						node.setPosition(new Vector(x, y, z));
						node.setPermission(permission);
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
			try (PreparedStatement stmt = con.prepareStatement("UPDATE `pathfinder_nodes` SET " +
					"`x` = ?, " +
					"`y` = ?, " +
					"`z` = ?, " +
					"`permission` = ?, " +
					"`path_curve_length` = ? " +
					"WHERE `id` = ?")) {
				stmt.setDouble(1, node.getPosition().getX());
				stmt.setDouble(2, node.getPosition().getY());
				stmt.setDouble(3, node.getPosition().getZ());
				stmt.setString(4, node.getPermission());
				if (node.getCurveLength() == null) {
					stmt.setNull(5, Types.DOUBLE);
				} else {
					stmt.setDouble(5, node.getCurveLength());
				}
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not update node.", e);
		}
	}

	@Override
	public void deleteNodes(int... nodeIds) {
		deleteNodes(Arrays.stream(nodeIds).boxed().collect(Collectors.toList()));
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
					"(`group_key`, `node_id`) VALUES (?, ?)")) {
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
	public void removeNodesFromGroup(NodeGroup group, NodeSelection selection) {
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
	public Map<NamespacedKey, List<Integer>> loadNodeGroupNodes() {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `pathfinder_nodegroups_nodes`")) {
				try (ResultSet resultSet = stmt.executeQuery()) {
					Map<NamespacedKey, List<Integer>> registry = new HashMap<>();
					while (resultSet.next()) {
						String keyString = resultSet.getString("group_key");
						int nodeId = resultSet.getInt("node_id");

						List<Integer> l = registry.computeIfAbsent(NamespacedKey.fromString(keyString), key -> new ArrayList<>());
						l.add(nodeId);
					}
					return registry;
				}
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not load node group nodes.", e);
		}
	}

	@Override
	public NodeGroup createNodeGroup(NamespacedKey key, String nameFormat, boolean findable) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_nodegroups` " +
					"(`key`, `name_format`, `findable`) VALUES " +
					"(?, ?, ?)")) {
				stmt.setString(1, key.toString());
				stmt.setString(2, nameFormat);
				stmt.setBoolean(3, findable);
				stmt.executeUpdate();

				NodeGroup group = new NodeGroup(key, nameFormat);
				group.setDiscoverable(findable);
				return group;
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create new node group.", e);
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
						boolean nodesFindable = resultSet.getBoolean("findable");

						NodeGroup group = new NodeGroup(NamespacedKey.fromString(keyString), nameFormat);
						group.setDiscoverable(nodesFindable);
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
			try (PreparedStatement stmt = con.prepareStatement("UPDATE `pathfinder_nodegroups` SET " +
					"`name_format` = ?, " +
					"`findable` = ?, " +
					"WHERE `key` = ?")) {
				stmt.setString(1, group.getNameFormat());
				stmt.setBoolean(2, group.isDiscoverable());
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
			throw new DataStorageException("Could not load node group nodes.", e);
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
	public DiscoverInfo createFoundInfo(UUID player, Discoverable discoverable, Date foundDate) {
		return null;
	}

	@Override
	public Map<Integer, DiscoverInfo> loadFoundInfo(int globalPlayerId, boolean group) {
		return null;
	}

	@Override
	public void deleteFoundInfo(int globalPlayerId, int nodeId, boolean group) {

	}

	@Override
	public ParticleVisualizer newPathVisualizer(NamespacedKey key, String nameFormat, ParticleBuilder particle, ItemStack displayIcon, double particleDistance, int particleSteps, int schedulerPeriod, double curveLength) {
		try (Connection con = getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement("INSERT INTO `pathfinder_path_visualizer` " +
					"(`key`, `name_format`, `permission`, `display_item`, `particle`, `particle_steps`, `particle_distance`, `particle_period`, `curve_length`) VALUES " +
					"(?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				stmt.setString(1, key.toString());
				stmt.setString(2, nameFormat);
				stmt.setNull(3, Types.VARCHAR);
				stmt.setString(4, DataUtils.serializeItemStack(displayIcon));
				stmt.setString(5, DataUtils.serializeParticle(particle));
				stmt.setDouble(6, particleSteps);
				stmt.setDouble(7, particleDistance);
				stmt.setInt(8, schedulerPeriod);
				stmt.setDouble(9, curveLength);

				ParticleVisualizer vis = new ParticleVisualizer(key, nameFormat);
				//vis.setParticle(particle);
				vis.setDisplayItem(displayIcon);
				vis.setSchedulerSteps(particleSteps);
				vis.setPointDistance((float) particleDistance);
				return vis;
			}
		} catch (Exception e) {
			throw new DataStorageException("Could not create new path visualizer.", e);
		}
	}

	@Override
	public Map<Integer, PathVisualizer> loadPathVisualizer() {
		return null;
	}

	@Override
	public void updatePathVisualizer(PathVisualizer<?> visualizer) {

	}

	@Override
	public void deletePathVisualizer(PathVisualizer<?> visualizer) {
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
	public void createPlayerVisualizer(int playerId, RoadMap roadMap, ParticleVisualizer visualizer) {

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
