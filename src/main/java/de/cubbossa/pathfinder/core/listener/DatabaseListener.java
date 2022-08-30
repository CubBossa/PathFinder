package de.cubbossa.pathfinder.core.listener;

import de.cubbossa.pathfinder.core.events.node.EdgesDeletedEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.*;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapCurveLengthChangedEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapDeletedEvent;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import de.cubbossa.pathfinder.module.discovering.event.PlayerForgetEvent;
import de.cubbossa.pathfinder.util.NodeSelection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

public class DatabaseListener implements Listener {

	@Getter
	@Setter
	private DataStorage data;

	public DatabaseListener(DataStorage dataStorage) {
		this.data = dataStorage;
	}

	@EventHandler
	public void onRoadMapDeleted(RoadMapDeletedEvent event) {
		data.deleteRoadMap(event.getRoadMap().getKey());
	}

	@EventHandler
	public void onRoadMapDefaultCurveLengthChanged(RoadMapCurveLengthChangedEvent event) {
		data.updateRoadMap(event.getRoadMap());
	}

	@EventHandler
	public void onNodesDeleted(NodesDeletedEvent event) {
		data.deleteNodes(event.getNodes().stream().map(Node::getNodeId).collect(Collectors.toSet()));
	}

	@EventHandler
	public void onEdgeDeleted(EdgesDeletedEvent event) {
		data.deleteEdges(event.getEdges());
	}

	@EventHandler
	public void onGroupDeleted(NodeGroupDeletedEvent event) {
		data.deleteNodeGroup(event.getGroup().getKey());
	}

	@EventHandler
	public void onGroupAssign(NodeGroupAssignedEvent event) {
		event.getGroups().forEach(group -> {
			data.assignNodesToGroup(group, event.getGroupables().stream()
					.filter(g -> g instanceof Node)
					.map(g -> (Node) g)
					.collect(Collectors.toCollection(NodeSelection::new)));
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupRemove(NodeGroupRemovedEvent event) {
		event.getGroups().forEach(group -> {
			data.removeNodesFromGroup(group, event.getGroupables().stream()
					.collect(Collectors.toCollection(NodeSelection::new)));
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupSetNameEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupSetPermissionEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupSetNavigableEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupSetDiscoverableEvent event) {
		data.updateNodeGroup(event.getGroup());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupUpdate(NodeGroupSetFindDistanceEvent event) {
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
		data.deleteDiscoverInfo(event.getPlayerId(), event.getDiscoverable().getUniqueKey());
	}
}
