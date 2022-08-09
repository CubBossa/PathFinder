package de.bossascrew.pathfinder.core.listener;

import de.bossascrew.pathfinder.data.DataStorage;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupAssignEvent;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupDeletedEvent;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.bossascrew.pathfinder.core.events.roadmap.RoadMapDeletedEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
	public void onGroupDeleted(NodeGroupDeletedEvent event) {
		data.deleteNodeGroup(event.getGroup().getKey());
	}

	@EventHandler
	public void onGroupAssign(NodeGroupAssignEvent event) {
		data.assignNodesToGroup(event.getGroup(), event.getNodes());
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
