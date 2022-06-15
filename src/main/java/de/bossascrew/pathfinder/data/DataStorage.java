package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.cubbossa.menuframework.util.Pair;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public interface DataStorage {


	RoadMap createRoadMap(String name, World world, boolean findableNodes);


	RoadMap createRoadMap(String name, World world, boolean findableNodes, int pathVis, int editModeVis, double findDist, double tangentLength);


	Map<Integer, RoadMap> loadRoadMaps();

	void updateRoadMap(RoadMap roadMap);

	boolean deleteRoadMap(RoadMap roadMap);

	boolean deleteRoadMap(int roadMapId);

	void newEdge(Waypoint nodeA, Waypoint nodeB);

	Collection<Pair<Integer, Integer>> loadEdges(RoadMap roadMap);

	void deleteEdge(Pair<Waypoint, Waypoint> edge);

	void deleteEdge(Waypoint a, Waypoint b);

	Waypoint newFindable(RoadMap roadMap, String scope, Integer groupId, Double x, Double y, Double z, String name, Double tangentLength, String permission);

	void deleteFindable(int nodeId);

	void updateFindable(@NotNull Waypoint findable);

	Map<Integer, Waypoint> loadFindables(RoadMap roadMap);

	NodeGroup newFindableGroup(RoadMap roadMap, String name, boolean findable);

	void deleteFindableGroup(NodeGroup group);

	void deleteFindableGroup(int groupId);

	Map<Integer, NodeGroup> loadFindableGroups(RoadMap roadMap);

	void updateFindableGroup(NodeGroup group);

	FoundInfo newFoundInfo(int globalPlayerId, int foundId, boolean group, Date foundDate);

	Map<Integer, FoundInfo> loadFoundNodes(int globalPlayerId, boolean group);

	void deleteFoundNode(int globalPlayerId, int nodeId, boolean group);

	EditModeVisualizer newEditModeVisualizer(String name, @Nullable EditModeVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit, @Nullable Integer schedulerPeriod, @Nullable Integer nodeHeadId, @Nullable Integer edgeHeadId);

	Map<Integer, EditModeVisualizer> loadEditModeVisualizer();

	PathVisualizer newPathVisualizer(String name, @Nullable PathVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit, @Nullable Integer particleSteps, @Nullable Integer schedulerPeriod);

	Map<Integer, PathVisualizer> loadPathVisualizer();

	void updateEditModeVisualizer(EditModeVisualizer visualizer);

	void updatePathVisualizer(PathVisualizer visualizer);

	void deletePathVisualizer(PathVisualizer visualizer);

	void deleteEditModeVisualizer(EditModeVisualizer visualizer);

	Map<Integer, Map<Integer, Integer>> loadPlayerVisualizers();

	void createPlayerVisualizer(int playerId, RoadMap roadMap, PathVisualizer visualizer);

	void updatePlayerVisualizer(int playerId, RoadMap roadMap, PathVisualizer visualizer);

	void loadVisualizerStyles(Collection<PathVisualizer> visualizers);

	void newVisualizerStyle(PathVisualizer visualizer, @Nullable String permission, @Nullable Material iconType, @Nullable String miniDisplayName);

	void updateVisualizerStyle(PathVisualizer visualizer);

	void deleteStyleVisualizer(int visualizerId);

	Map<Integer, Collection<PathVisualizer>> loadStyleRoadmapMap(Collection<PathVisualizer> visualizers);

	void addStyleToRoadMap(RoadMap roadMap, PathVisualizer pathVisualizer);

	void removeStyleFromRoadMap(RoadMap roadMap, PathVisualizer pathVisualizer);
}
