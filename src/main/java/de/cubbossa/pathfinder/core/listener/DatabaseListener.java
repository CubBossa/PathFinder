package de.cubbossa.pathfinder.core.listener;

import de.cubbossa.pathfinder.core.events.node.*;
import de.cubbossa.pathfinder.core.events.nodegroup.*;
import de.cubbossa.pathfinder.core.events.roadmap.*;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import de.cubbossa.pathfinder.module.discovering.event.PlayerForgetEvent;
import de.cubbossa.pathfinder.module.visualizing.events.CombinedVisualizerChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerPropertyChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseListener implements Listener {

	@Getter
	@Setter
	private DataStorage data;

	public DatabaseListener(DataStorage dataStorage) {
		this.data = dataStorage;
	}

	@EventHandler
	public void onRoadMapCreate(RoadMapCreatedEvent event) {
		data.updateRoadMap(event.getRoadMap());
	}

	@EventHandler
	public void onRoadMapDeleted(RoadMapDeletedEvent event) {
		data.deleteRoadMap(event.getRoadMap().getKey());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRoadMapUpdate(RoadMapSetNameEvent event) {
		data.updateRoadMap(event.getRoadMap());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRoadMapUpdate(RoadMapSetCurveLengthEvent event) {
		data.updateRoadMap(event.getRoadMap());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRoadMapUpdate(RoadMapSetVisualizerEvent event) {
		data.updateRoadMap(event.getRoadMap());
	}

	@EventHandler
	public void onNodeCreatedEvent(NodeCreatedEvent event) {
		data.updateNode(event.getNode());
	}

	@EventHandler
	public void onNodesDeleted(NodesDeletedEvent event) {
		Map<NodeGroup, Collection<Groupable>> map = new HashMap<>();
		for (Node node : event.getNodes()) {
			if (node instanceof Groupable groupable) {
				groupable.getGroups().forEach(g -> map.computeIfAbsent(g, group -> new HashSet<>()).add(groupable));
			}
		}
		for (Map.Entry<NodeGroup, Collection<Groupable>> entry : map.entrySet()) {
			data.removeNodesFromGroup(entry.getKey(), entry.getValue());
		}
		data.deleteNodes(event.getNodes().stream().map(Node::getNodeId).collect(Collectors.toSet()));
	}

	@EventHandler
	public void onNodeUpdate(NodeLocationChangedEvent event) {
		event.getNodes().forEach(data::updateNode);
	}

	@EventHandler
	public void onNodeUpdate(NodeCurveLengthChangedEvent event) {
		event.getNodes().forEach(data::updateNode);
	}

	@EventHandler
	public void onEdgesCreated(EdgesCreatedEvent event) {
		data.saveEdges(event.getEdges());
	}

	@EventHandler
	public void onEdgeDeleted(EdgesDeletedEvent event) {
		data.deleteEdges(event.getEdges());
	}

	@EventHandler
	public void onGroupCreated(NodeGroupCreatedEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler
	public void onGroupDeleted(NodeGroupDeletedEvent event) {
		data.removeSearchTerms(event.getGroup(), event.getGroup().getSearchTerms());
		data.deleteNodeGroup(event.getGroup().getKey());
	}

	@EventHandler
	public void onGroupAssign(NodeGroupAssignedEvent event) {
		event.getGroups().forEach(group -> {
			data.assignNodesToGroup(group, event.getGroupables().stream()
					.collect(Collectors.toCollection(NodeSelection::new)));
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupRemove(NodeGroupRemovedEvent event) {
		event.getGroups().forEach(group -> data.removeNodesFromGroup(group, event.getGroupables()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupNameChangedEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupPermissionChangedEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupNavigableChangedEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupDiscoverableChangedEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupFindDistanceChangedEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler
	public void onSearchTermsChanged(NodeGroupSearchTermsChangedEvent event) {
		switch (event.getAction()) {
			case ADD -> data.addSearchTerms(event.getGroup(), event.getChangedTerms());
			case REMOVE -> data.removeSearchTerms(event.getGroup(), event.getChangedTerms());
			case CLEAR -> data.removeSearchTerms(event.getGroup(), event.getGroup().getSearchTerms());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDiscover(PlayerDiscoverEvent event) {
		data.createDiscoverInfo(event.getPlayerId(), event.getDiscoverable(), event.getDate());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onForget(PlayerForgetEvent event) {
		data.deleteDiscoverInfo(event.getPlayerId(), event.getDiscoverable().getKey());
	}

	@EventHandler
	public <T> void onPropertyChanged(VisualizerPropertyChangedEvent<T> event) {
		data.updatePathVisualizer((PathVisualizer) event.getVisualizer());
	}

	@EventHandler
	public void onCombinedUpdate(CombinedVisualizerChangedEvent event) {
		data.updatePathVisualizer(event.getVisualizer());
	}
}
