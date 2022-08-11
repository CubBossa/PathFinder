package de.bossascrew.pathfinder.core.listener;

import de.bossascrew.pathfinder.core.events.node.EdgeDeletedEvent;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupDeletedEvent;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.bossascrew.pathfinder.core.events.roadmap.RoadMapDeletedEvent;
import de.bossascrew.pathfinder.core.node.Node;
import de.bossascrew.pathfinder.data.DataStorage;
import de.bossascrew.pathfinder.util.NodeSelection;
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
	public void onEdgeDeleted(EdgeDeletedEvent event) {
		data.deleteEdge(event.getEdge());
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
