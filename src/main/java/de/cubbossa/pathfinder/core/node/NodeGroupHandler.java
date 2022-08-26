package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeletedEvent;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.util.HashedRegistry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class NodeGroupHandler implements Listener {

	@Getter
	private static NodeGroupHandler instance;

	private final HashedRegistry<NodeGroup> groups;

	public NodeGroupHandler() {
		instance = this;
		groups = new HashedRegistry<>();

		Bukkit.getPluginManager().registerEvents(this, PathPlugin.getInstance());
	}

	public void loadGroups() {
		groups.clear();
		groups.putAll(PathPlugin.getInstance().getDatabase().loadNodeGroups());
		for(var entry : PathPlugin.getInstance().getDatabase().loadSearchTerms().entrySet()) {
			NodeGroup group = getNodeGroup(entry.getKey());
			if(group == null) {
				continue;
			}
			group.addSearchTerms(entry.getValue());
		}
	}

	public Collection<NodeGroup> getNodeGroups() {
		return groups.values();
	}

	public Collection<NodeGroup> getNodeGroups(RoadMap roadMap) {
		return groups.values().stream().filter(nodes -> nodes.stream().anyMatch(node -> node.getRoadMapKey().equals(roadMap.getKey()))).collect(Collectors.toSet());
	}

	public boolean setGroupFindable(NodeGroup group, boolean findable) {
		//TODO implement. Refreshes the findable collection.
		return true;
	}

	public @Nullable
	NodeGroup getNodeGroup(NamespacedKey key) {
		if (key == null) {
			return null;
		}
		return groups.get(key);
	}

	public void removeNodeGroup(NodeGroup group) {
		groups.remove(group.getKey());

		Bukkit.getPluginManager().callEvent(new NodeGroupDeletedEvent(group));

		group.stream()
				.filter(node -> node instanceof Groupable)
				.map(node -> (Groupable) node)
				.forEach(node -> node.removeGroup(group));
		group.clear();
	}

	public NodeGroup createNodeGroup(NamespacedKey key, boolean findable, String nameFormat) {

		NodeGroup group = PathPlugin.getInstance().getDatabase().createNodeGroup(key, nameFormat, findable);
		groups.put(group);
		return group;
	}

	@EventHandler
	public void onNodeDelete(NodesDeletedEvent event) {
		for (NodeGroup group : groups) {
			group.removeAll(event.getNodes());
		}
	}
}
