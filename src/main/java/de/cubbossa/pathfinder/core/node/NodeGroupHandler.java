package de.cubbossa.pathfinder.core.node;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.util.HashedRegistry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
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

		for (Groupable node : group) {
			node.removeGroup(group);
		}
		group.clear();
	}

	public NodeGroup createNodeGroup(NamespacedKey key, String nameFormat) {

		NodeGroup group = new NodeGroup(key, nameFormat);
		group.addSearchTerms(Lists.newArrayList(key.getKey()));
		Bukkit.getPluginManager().callEvent(new NodeGroupCreatedEvent(group));
		Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
				group, NodeGroupSearchTermsChangedEvent.Action.ADD, Lists.newArrayList(key.getKey())
		));
		groups.put(group);
		return group;
	}

	public void setNodeGroupName(NodeGroup group, String newName) {
		NodeGroupNameChangedEvent event = new NodeGroupNameChangedEvent(group, newName);
		Bukkit.getPluginManager().callEvent(event);
		group.setNameFormat(event.getNameFormat());
	}

	public void setNodeGroupPermission(NodeGroup group, @Nullable String permission) {
		NodeGroupPermissionChangedEvent event = new NodeGroupPermissionChangedEvent(group, permission);
		Bukkit.getPluginManager().callEvent(event);
		group.setPermission(event.getPermission());
	}

	public void setNodeGroupDiscoverable(NodeGroup group, boolean value) {
		NodeGroupDiscoverableChangedEvent event = new NodeGroupDiscoverableChangedEvent(group, value);
		Bukkit.getPluginManager().callEvent(event);
		group.setDiscoverable(value);
	}

	public void setNodeGroupNavigable(NodeGroup group, boolean value) {
		group.setNavigable(value);
		NodeGroupNavigableChangedEvent event = new NodeGroupNavigableChangedEvent(group, value);
		Bukkit.getPluginManager().callEvent(event);
	}

	public void setNodeGroupFindDistance(NodeGroup group, float value) {
		group.setFindDistance(value);
		NodeGroupFindDistanceChangedEvent event = new NodeGroupFindDistanceChangedEvent(group, value);
		Bukkit.getPluginManager().callEvent(event);
	}

	public float getFindDistance(Groupable groupable) {
		if (groupable.getGroups().isEmpty()) {
			return 1.5f;
		}
		return (float) switch (PathPlugin.getInstance().getConfiguration().getFindDistancePolicy()) {
			case SMALLEST_VALUE -> groupable.getGroups().stream().mapToDouble(NodeGroup::getFindDistance).min().orElse(1.5);
			case LARGEST_VALUE -> groupable.getGroups().stream().mapToDouble(NodeGroup::getFindDistance).max().orElse(1.5);
			case NATURAL_ORDER -> groupable.getGroups().stream().findFirst().get().getFindDistance();
		};
	}

	public boolean hasPermission(Player player, Groupable groupable) {
		if (groupable.getGroups().isEmpty()) {
			return false;
		}
		return switch (PathPlugin.getInstance().getConfiguration().getNavigablePolicy()) {
			case SMALLEST_VALUE -> groupable.getGroups().stream().allMatch(group -> group.getPermission() == null || player.hasPermission(group.getPermission()));
			case LARGEST_VALUE -> groupable.getGroups().stream().anyMatch(group -> group.getPermission() == null || player.hasPermission(group.getPermission()));
			case NATURAL_ORDER -> groupable.getGroups().stream().limit(1).anyMatch(group -> group.getPermission() == null || player.hasPermission(group.getPermission()));
		};
	}

	public boolean isNavigable(Groupable groupable) {
		if (groupable.getGroups().isEmpty()) {
			return false;
		}
		return switch (PathPlugin.getInstance().getConfiguration().getNavigablePolicy()) {
			case SMALLEST_VALUE -> groupable.getGroups().stream().allMatch(NodeGroup::isNavigable);
			case LARGEST_VALUE -> groupable.getGroups().stream().anyMatch(NodeGroup::isNavigable);
			case NATURAL_ORDER -> groupable.getGroups().stream().findFirst().get().isNavigable();
		};
	}

	public boolean isDiscoverable(Groupable groupable) {
		if (groupable.getGroups().isEmpty()) {
			return false;
		}
		return switch (PathPlugin.getInstance().getConfiguration().getDiscoverablePolicy()) {
			case SMALLEST_VALUE -> groupable.getGroups().stream().allMatch(NodeGroup::isDiscoverable);
			case LARGEST_VALUE -> groupable.getGroups().stream().anyMatch(NodeGroup::isDiscoverable);
			case NATURAL_ORDER -> groupable.getGroups().stream().findFirst().get().isDiscoverable();
		};
	}

	@EventHandler
	public void onNodeDelete(NodesDeletedEvent event) {
		for (NodeGroup group : groups) {
			group.removeAll(event.getNodes());
		}
	}
}
