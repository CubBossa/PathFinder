package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import javax.annotation.Nullable;
import java.util.*;

public interface DataStorage {

	void connect();

	void disconnect();

	Map<NamespacedKey, RoadMap> loadRoadMaps();

	void updateRoadMap(RoadMap roadMap);

	default boolean deleteRoadMap(RoadMap roadMap) {
		return deleteRoadMap(roadMap.getKey());
	}

	boolean deleteRoadMap(NamespacedKey key);


	void saveEdges(Collection<Edge> edges);

	Collection<Edge> loadEdges(RoadMap roadMap);

	void deleteEdgesFrom(Node start);

	void deleteEdgesTo(Node end);

	void deleteEdges(Collection<Edge> edges);

	default void deleteEdge(Edge edge) {
		deleteEdge(edge.getStart(), edge.getEnd());
	}

	void deleteEdge(Node start, Node end);

	Map<Integer, Node> loadNodes(RoadMap roadMap);

	void updateNode(Node node);

	default void deleteNodes(Integer... nodeId) {
		deleteNodes(Arrays.asList(nodeId));
	}

	void deleteNodes(Collection<Integer> nodeIds);


	void assignNodesToGroup(NodeGroup group, NodeSelection selection);

	void removeNodesFromGroup(NodeGroup group, Iterable<Groupable> selection);

	Map<NamespacedKey, ? extends Collection<Integer>> loadNodeGroupNodes();

	HashedRegistry<NodeGroup> loadNodeGroups();

	void updateNodeGroup(NodeGroup group);

	default void deleteNodeGroup(NodeGroup group) {
		deleteNodeGroup(group.getKey());
	}

	void deleteNodeGroup(NamespacedKey key);

	Map<NamespacedKey, Collection<String>> loadSearchTerms();

	void addSearchTerms(NodeGroup group, Collection<String> searchTerms);

	void removeSearchTerms(NodeGroup group, Collection<String> searchTerms);

	DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, Date foundDate);

	Map<UUID, Map<NamespacedKey, DiscoverInfo>> loadDiscoverInfo();

	void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey);

	Map<NamespacedKey, PathVisualizer<?, ?>> loadPathVisualizer();

	<T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer);

	void deletePathVisualizer(PathVisualizer<?, ?> visualizer);

	Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers();

	void updatePlayerVisualizer(int playerId, RoadMap roadMap, ParticleVisualizer visualizer);

	void loadVisualizerStyles(Collection<ParticleVisualizer> visualizers);

	void newVisualizerStyle(ParticleVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName);

	void updateVisualizerStyle(ParticleVisualizer visualizer);

	void deleteStyleVisualizer(int visualizerId);

	Map<Integer, Collection<ParticleVisualizer>> loadStyleRoadmapMap(Collection<ParticleVisualizer> visualizers);

	void addStyleToRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer);

	void removeStyleFromRoadMap(RoadMap roadMap, ParticleVisualizer ParticleVisualizer);
}
