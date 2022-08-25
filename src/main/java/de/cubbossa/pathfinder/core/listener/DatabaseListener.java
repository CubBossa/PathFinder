package de.cubbossa.pathfinder.core.listener;

import de.cubbossa.pathfinder.core.events.node.EdgesDeletedEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapDeletedEvent;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.util.NodeSelection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
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

	@EventHandler
	public void onGroupRemove(NodeGroupRemovedEvent event) {
		event.getGroups().forEach(group -> {
			data.removeNodesFromGroup(group, event.getGroupables().stream()
					.filter(g -> g instanceof Node)
					.map(g -> (Node) g)
					.collect(Collectors.toCollection(NodeSelection::new)));
		});
	}

	@EventHandler
	public void onSearchTermsChanged(NodeGroupSearchTermsChangedEvent event) {
		switch (event.getAction()) {
			case ADD -> data.addSearchTerms(event.getGroup(), event.getChangedTerms());
			case REMOVE -> data.removeSearchTerms(event.getGroup(), event.getChangedTerms());
			case CLEAR -> data.removeSearchTerms(event.getGroup(), event.getGroup().getSearchTerms());
		}
	}

}
