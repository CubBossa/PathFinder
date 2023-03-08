package de.cubbossa.pathfinder.core.node;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupCreatedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDiscoverableChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupFindDistanceChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupNameChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupNavigableChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupPermissionChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemoveEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
    for (var entry : PathPlugin.getInstance().getDatabase().loadSearchTerms().entrySet()) {
      NodeGroup group = getNodeGroup(entry.getKey());
      if (group == null) {
        continue;
      }
      group.addSearchTermStrings(entry.getValue());
    }
  }

  public Collection<NodeGroup> getNodeGroups() {
    return groups.values();
  }

  public Collection<NodeGroup> getNodeGroups(RoadMap roadMap) {
    return groups.values().stream().filter(
            nodes -> nodes.stream().anyMatch(node -> node.getRoadMapKey().equals(roadMap.getKey())))
        .collect(Collectors.toSet());
  }

  public @Nullable
  NodeGroup getNodeGroup(NamespacedKey key) {
    if (key == null) {
      return null;
    }
    return groups.get(key);
  }

  /**
   * Creates a new NodeGroup. NodeGroups can be used to apply common behaviour to multiple nodes, like if they can
   * be discovered or be navigated to.
   * <br>
   * <br>
   * Invoking this method will
   * <br>- create a NodeGroup instance
   * <br>- set the key, display name and default properties.
   * <br>- call the {@code NodeGroupCreatedEvent}
   * <br>- set the key (not including the namespace of &lt;namespace&gt;:&lt;key&gt;) as initial search term
   * <br>- call the {@code NodeGroupSearchTermsChangedEvent} for the added search term
   * <br>- add the group to the groups collection of this handler
   *
   * @param key        The unique NodeGroup key. There can only ever be one NodeGroup with this key within this plugin.
   * @param nameFormat The MiniMessage format that defines the display name for the NodeGroup
   * @return the new instance of the NodeGroup
   * @throws IllegalArgumentException If another group with this key already exists.
   */
  public NodeGroup createNodeGroup(NamespacedKey key, String nameFormat)
      throws IllegalArgumentException {

    if (getNodeGroup(key) != null) {
      throw new IllegalArgumentException("Another nodegroup with this key already exists.");
    }

    NodeGroup group = new NodeGroup(key, nameFormat);
    group.addSearchTermStrings(Lists.newArrayList(key.getKey()));
    Bukkit.getPluginManager().callEvent(new NodeGroupCreatedEvent(group));
    Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
        group, NodeGroupSearchTermsChangedEvent.Action.ADD, Lists.newArrayList(key.getKey())
    ));
    groups.put(group);
    return group;
  }

  public void deleteNodeGroup(NodeGroup group) {
    groups.remove(group.getKey());

    Bukkit.getPluginManager().callEvent(new NodeGroupDeletedEvent(group));

    for (Groupable<?> node : group) {
      node.removeGroup(group);
    }
    group.clear();
  }

  public void setNodeGroupName(NodeGroup group, String newName) {
    group.setNameFormat(newName);
    NodeGroupNameChangedEvent event = new NodeGroupNameChangedEvent(group, newName);
    Bukkit.getPluginManager().callEvent(event);
  }

  public void setNodeGroupPermission(NodeGroup group, @Nullable String permission) {
    group.setPermission(permission);
    NodeGroupPermissionChangedEvent event = new NodeGroupPermissionChangedEvent(group, permission);
    Bukkit.getPluginManager().callEvent(event);
  }

  public void setNodeGroupDiscoverable(NodeGroup group, boolean value) {
    group.setDiscoverable(value);
    NodeGroupDiscoverableChangedEvent event = new NodeGroupDiscoverableChangedEvent(group, value);
    Bukkit.getPluginManager().callEvent(event);
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

  public void addNodes(NodeGroup group, Collection<Groupable<?>> nodes) {
    addNodes(Collections.singleton(group), nodes);
  }

  public void addNodes(Collection<NodeGroup> groups, Collection<Groupable<?>> nodes) {
    NodeGroupAssignEvent event = new NodeGroupAssignEvent(nodes, groups);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }
    event.getModifiedGroups().forEach(g -> event.getModifiedGroupables().forEach(g::add));
    Bukkit.getPluginManager().callEvent(
        new NodeGroupAssignedEvent(event.getModifiedGroupables(), event.getModifiedGroups()));
  }

  public void removeNodes(NodeGroup group, Collection<Groupable<?>> nodes) {
    removeNodes(Collections.singleton(group), nodes);
  }

  public void removeNodes(Collection<NodeGroup> groups, Collection<Groupable<?>> nodes) {
    NodeGroupRemoveEvent event = new NodeGroupRemoveEvent(nodes, groups);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }
    event.getModifiedGroups().forEach(g -> event.getModifiedGroupables().forEach(g::remove));
    Bukkit.getPluginManager().callEvent(
        new NodeGroupRemovedEvent(event.getModifiedGroupables(), event.getModifiedGroups()));
  }

  public float getFindDistance(Groupable<?> groupable) {
    if (groupable.getGroups().isEmpty()) {
      return 1.5f;
    }
    return (float) switch (PathPlugin.getInstance().getConfiguration().navigation.distancePolicy) {
      case SMALLEST ->
          groupable.getGroups().stream().mapToDouble(NodeGroup::getFindDistance).min().orElse(1.5);
      case LARGEST ->
          groupable.getGroups().stream().mapToDouble(NodeGroup::getFindDistance).max().orElse(1.5);
      case NATURAL -> groupable.getGroups().stream().findFirst().get().getFindDistance();
    };
  }

  public boolean hasPermission(Player player, Groupable<?> groupable) {
    if (groupable.getGroups().isEmpty()) {
      return false;
    }
    if (PathPlugin.getInstance().getConfiguration().navigation.requireAllGroupPermissions) {
      return groupable.getGroups().stream().allMatch(
          group -> group.getPermission() == null || player.hasPermission(group.getPermission()));
    }
    return groupable.getGroups().stream().anyMatch(
        group -> group.getPermission() == null || player.hasPermission(group.getPermission()));
  }

  public boolean isNavigable(Groupable<?> groupable) {
    if (groupable.getGroups().isEmpty()) {
      return false;
    }
    if (PathPlugin.getInstance().getConfiguration().navigation.requireAllGroupsNavigable) {
      return groupable.getGroups().stream().allMatch(NodeGroup::isNavigable);
    }
    return groupable.getGroups().stream().anyMatch(NodeGroup::isNavigable);
  }

  public boolean isDiscoverable(Groupable<?> groupable) {
    if (groupable.getGroups().isEmpty()) {
      return false;
    }
    if (PathPlugin.getInstance().getConfiguration().navigation.requireAllGroupsNavigable) {
      return groupable.getGroups().stream().allMatch(NodeGroup::isDiscoverable);
    }
    return groupable.getGroups().stream().anyMatch(NodeGroup::isDiscoverable);
  }

  @EventHandler
  public void onNodeDelete(NodesDeletedEvent event) {
    for (NodeGroup group : groups) {
      group.removeAll(event.getNodes());
    }
  }
}
