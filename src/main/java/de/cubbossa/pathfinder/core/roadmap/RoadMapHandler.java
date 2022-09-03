package de.cubbossa.pathfinder.core.roadmap;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodeCurveLengthChangedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeLocationChangedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeTeleportEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapDeletedEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapSetCurveLengthEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapSetNameEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapSetVisualizerEvent;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeHandler;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.StringUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.stream.Stream;

@Getter
public class RoadMapHandler {

	public static NodeType<Waypoint> WAYPOINT_TYPE = new NodeType<>(new NamespacedKey(PathPlugin.getInstance(), "waypoint"),
			"<color:#ff0000>Waypoint</color>", new ItemStack(Material.MAP), (roadMap, integer) -> new Waypoint(integer, roadMap));

	@Getter
	private static RoadMapHandler instance;
	private final HashedRegistry<RoadMap> roadMaps;
	private final HashedRegistry<RoadMapEditor> roadMapEditors;

	public RoadMapHandler() {
		instance = this;
		roadMaps = new HashedRegistry<>();
		roadMapEditors = new HashedRegistry<>();
		NodeTypeHandler.getInstance().registerNodeType(WAYPOINT_TYPE);
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
		roadMaps.forEach(RoadMap::loadNodesAndEdges);
	}

	public RoadMap createRoadMap(NamespacedKey key) {
		RoadMap rm = PathPlugin.getInstance().getDatabase().createRoadMap(key,
				StringUtils.getRandHexString() + StringUtils.capizalize(key.getKey()),
				null);
		roadMaps.put(rm);
		return rm;
	}

	public void deleteRoadMap(RoadMap roadMap) {

		roadMaps.remove(roadMap.getKey());
		Bukkit.getPluginManager().callEvent(new RoadMapDeletedEvent(roadMap));
		//TODO delete all nodes and edges
		//TODO deselect roadmap for all players
	}

	public @Nullable
	RoadMap getRoadMap(NamespacedKey key) {
		return roadMaps.get(key);
	}

	public Stream<RoadMap> getRoadMapsStream() {
		return roadMaps.values().stream();
	}

	public boolean isKeyUnique(NamespacedKey key) {
		return !roadMaps.containsKey(key);
	}

	public void cancelAllEditModes() {
		roadMapEditors.values().forEach(RoadMapEditor::cancelEditModes);
	}

	public boolean setRoadMapName(RoadMap roadMap, String nameFormat) {
		String old = roadMap.getNameFormat();
		RoadMapSetNameEvent event = new RoadMapSetNameEvent(roadMap, nameFormat);
		roadMap.setNameFormat(nameFormat);

		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			roadMap.setNameFormat(old);
			return false;
		}
		return true;
	}

	public boolean setRoadMapCurveLength(RoadMap roadMap, double value) {
		double old = roadMap.getDefaultBezierTangentLength();
		RoadMapSetCurveLengthEvent event = new RoadMapSetCurveLengthEvent(roadMap, value);
		roadMap.setDefaultBezierTangentLength(event.getValue());

		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			roadMap.setDefaultBezierTangentLength(old);
			return false;
		}
		return true;
	}

	public boolean setRoadMapVisualizer(RoadMap roadMap, PathVisualizer<?, ?> visualizer) {
		PathVisualizer<?, ?> old = roadMap.getVisualizer();
		RoadMapSetVisualizerEvent event = new RoadMapSetVisualizerEvent(roadMap, visualizer);
		roadMap.setVisualizer(event.getVisualizer());

		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			roadMap.setVisualizer(old);
			return false;
		}
		return true;
	}

	/**
	 * This method changes the position of the node and calls the corresponding event.
	 * If the event is not cancelled, the change will be updated to the database.
	 * Don't call this method asynchronous, events can only be called in the main thread.
	 * <p>
	 * TO only modify the position without event or database update, simply call {@link Node#setLocation(Location)}
	 *
	 * @param nodes    The nodes to change the position for.
	 * @param location The position to set. No world attribute is required, the roadmap attribute is used. Use {@link Location#toVector()}
	 *                 to set a location.
	 * @return true if the position was successfully set, false if the event was cancelled
	 */
	public boolean setNodeLocation(NodeSelection nodes, Location location) {

		NodeTeleportEvent event = new NodeTeleportEvent(nodes, location);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}
		for (Node node : nodes) {
			node.setLocation(event.getNewPositionModified());
		}
		Bukkit.getPluginManager().callEvent(new NodeLocationChangedEvent(nodes, event.getNewPositionModified()));
		return true;
	}

	public void setNodeCurveLength(NodeSelection nodes, Double length) {
		nodes.forEach(node -> node.setCurveLength(length));
		Bukkit.getPluginManager().callEvent(new NodeCurveLengthChangedEvent(nodes, length));
	}
}
