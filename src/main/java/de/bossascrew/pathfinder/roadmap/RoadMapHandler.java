package de.bossascrew.pathfinder.roadmap;

import de.bossascrew.pathfinder.NodeType;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.util.HashedRegistry;
import de.bossascrew.pathfinder.util.StringUtils;
import de.bossascrew.pathfinder.visualizer.VisualizerHandler;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class RoadMapHandler {

	public static NodeType<Waypoint> WAYPOINT_TYPE = new NodeType<>("<color:#ff0000>Waypoint</color>", new ItemStack(Material.MAP), (roadMap, integer) ->
			new Waypoint(integer, roadMap));

	@Getter
	private static RoadMapHandler instance;
	private final HashedRegistry<RoadMap> roadMaps;
	private final HashedRegistry<RoadMapEditor> roadMapEditors;
	private final Collection<NodeType<?>> nodeTypes;

	public RoadMapHandler() {
		instance = this;
		roadMaps = new HashedRegistry<>();
		roadMapEditors = new HashedRegistry<>();
		nodeTypes = new ArrayList<>();
		registerNodeType(WAYPOINT_TYPE);
	}

	public <T extends Node> void registerNodeType(NodeType<T> type) {
		nodeTypes.add(type);
	}

	public <T extends Node> void unregisterNodeType(NodeType<T> type) {
		nodeTypes.remove(type);
	}

	public RoadMapEditor getRoadMapEditor(NamespacedKey key) {
		RoadMapEditor editor = roadMapEditors.get(key);
		if (editor == null) {
			RoadMap roadMap = roadMaps.get(key);
			if (roadMap == null) {
				throw new IllegalArgumentException("No roadmap exists with key '" + key + "'. Cannot create editor.");
			}
			editor = new RoadMapEditor(roadMap);
			roadMapEditors.put(editor);
		}
		return editor;
	}

	public void loadRoadMaps() {
		roadMaps.clear();
		roadMaps.putAll(PathPlugin.getInstance().getDatabase().loadRoadMaps());
	}

	public RoadMap createRoadMap(NamespacedKey key, World world, boolean findableNodes) {
		RoadMap rm = PathPlugin.getInstance().getDatabase().createRoadMap(key, StringUtils.getRandHexString() + StringUtils.capizalize(key.getKey()), world, findableNodes);
		roadMaps.put(rm);
		rm.setVisualizer(VisualizerHandler.getInstance().getDefaultSimpleCurveVisualizer());
		return rm;
	}

	public void deleteRoadMap(RoadMap roadMap) {
		//TODO delete all nodes and edges
		//TODO deselect roadmap for all players
		PathPlugin.getInstance().getDatabase().deleteRoadMap(roadMap);
		roadMaps.remove(roadMap.getKey());
	}

	public @Nullable
	RoadMap getRoadMap(NamespacedKey key) {
		return roadMaps.get(key);
	}

	public Collection<World> getRoadMapWorlds() {
		return getRoadMapsStream().map(RoadMap::getWorld).collect(Collectors.toSet());
	}

	public Collection<RoadMap> getRoadMaps(World world) {
		return getRoadMapsStream().filter(roadMap -> roadMap.getWorld().equals(world)).collect(Collectors.toSet());
	}

	public Stream<RoadMap> getRoadMapsStream() {
		return roadMaps.values().stream();
	}

	public Collection<RoadMap> getRoadMapsFindable(World world) {
		return getRoadMapsStream().filter(RoadMap::isFindableNodes).filter(roadMap -> roadMap.getWorld().equals(world)).collect(Collectors.toSet());
	}

	public boolean isKeyUnique(NamespacedKey key) {
		return !roadMaps.containsKey(key);
	}

	public void cancelAllEditModes() {
		roadMapEditors.values().forEach(RoadMapEditor::cancelEditModes);
	}
}
