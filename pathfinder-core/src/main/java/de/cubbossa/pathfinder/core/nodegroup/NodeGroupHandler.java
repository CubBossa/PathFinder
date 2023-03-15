package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupCreatedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDiscoverableChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupFindDistanceChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupNavigableChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupPermissionChangedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemoveEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.core.nodegroup.modifier.FindDistanceModifier;
import de.cubbossa.pathfinder.core.nodegroup.modifier.NavigableModifier;
import de.cubbossa.pathfinder.core.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import de.cubbossa.pathfinder.module.visualizing.query.SimpleSearchTerm;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.function.ToDoubleFunction;
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

  private final NodeGroup global = new NodeGroup(new NamespacedKey(PathPlugin.getInstance(), "global"));
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
      group.addModifier(new NavigableModifier(entry.getValue().stream()
          .map(SimpleSearchTerm::new)
          .toArray(SearchTerm[]::new))
      );
    }
  }

  public Collection<NodeGroup> getNodeGroups() {
    return groups.values();
  }

  public Collection<NodeGroup> getNodeGroups(Collection<Class<Modifier>> withModifiers) {
    return groups.values().stream()
            .filter(group -> withModifiers.stream().allMatch(group::hasModifier))
            .collect(Collectors.toList());
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
   * @return the new instance of the NodeGroup
   * @throws IllegalArgumentException If another group with this key already exists.
   */
  public NodeGroup createNodeGroup(NamespacedKey key)
      throws IllegalArgumentException {

    if (getNodeGroup(key) != null) {
      throw new IllegalArgumentException("Another nodegroup with this key already exists.");
    }

    NodeGroup group = new NodeGroup(key);
    Bukkit.getPluginManager().callEvent(new NodeGroupCreatedEvent(group));
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

  public void setNodeGroupPermission(NodeGroup group, @Nullable String permission) {
    group.addModifier(new PermissionModifier(permission));
    NodeGroupPermissionChangedEvent event = new NodeGroupPermissionChangedEvent(group, permission);
    Bukkit.getPluginManager().callEvent(event);
  }

  public void setNodeGroupDiscoverable(NodeGroup group, boolean value) {
    if (value) {
      group.addModifier(new DiscoverableModifier());
    } else {
      group.removeModifier(DiscoverableModifier.class);
    }
    group.addModifier(new DiscoverableModifier());
    NodeGroupDiscoverableChangedEvent event = new NodeGroupDiscoverableChangedEvent(group, value);
    Bukkit.getPluginManager().callEvent(event);
  }

  public void setNodeGroupNavigable(NodeGroup group, boolean value) {
    if (value) {
      group.addModifier(new NavigableModifier());
    } else {
      group.removeModifier(NavigableModifier.class);
    }
    NodeGroupNavigableChangedEvent event = new NodeGroupNavigableChangedEvent(group, value);
    Bukkit.getPluginManager().callEvent(event);
  }

  public void setNodeGroupFindDistance(NodeGroup group, float value) {
    group.addModifier(new FindDistanceModifier(value));
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

    ToDoubleFunction<NodeGroup> doubleExtractor = g -> {
      FindDistanceModifier mod = g.getModifier(FindDistanceModifier.class);
      return mod == null ? 1.5f : mod.distance();
    };

    return (float) switch (PathPlugin.getInstance().getConfiguration().navigation.distancePolicy) {
      case SMALLEST -> groupable.getGroups().stream().mapToDouble(doubleExtractor).min().orElse(1.5);
      case LARGEST -> groupable.getGroups().stream().mapToDouble(doubleExtractor).max().orElse(1.5);
      case NATURAL -> doubleExtractor.applyAsDouble(groupable.getGroups().stream().findFirst().get());
    };
  }

  public boolean hasPermission(Player player, Groupable<?> groupable) {
    if (groupable.getGroups().isEmpty()) {
      return false;
    }
    if (PathPlugin.getInstance().getConfiguration().navigation.requireAllGroupPermissions) {
      return groupable.getGroups().stream().allMatch(group -> {
        PermissionModifier mod = group.getModifier(PermissionModifier.class);
        return mod == null || player.hasPermission(mod.permission());
      });
    }
    return groupable.getGroups().stream().anyMatch(group -> {
      PermissionModifier mod = group.getModifier(PermissionModifier.class);
      return mod == null || player.hasPermission(mod.permission());
    });
  }

  public boolean isNavigable(Groupable<?> groupable) {
    if (groupable.getGroups().isEmpty()) {
      return false;
    }
    if (PathPlugin.getInstance().getConfiguration().navigation.requireAllGroupsNavigable) {
      return groupable.getGroups().stream().allMatch(g -> g.hasModifier(NavigableModifier.class));
    }
    return groupable.getGroups().stream().anyMatch(g -> g.hasModifier(NavigableModifier.class));
  }

  public boolean isDiscoverable(Groupable<?> groupable) {
    if (groupable.getGroups().isEmpty()) {
      return false;
    }
    if (PathPlugin.getInstance().getConfiguration().navigation.requireAllGroupsNavigable) {
      return groupable.getGroups().stream().allMatch(g -> g.hasModifier(DiscoverableModifier.class));
    }
    return groupable.getGroups().stream().anyMatch(g -> g.hasModifier(DiscoverableModifier.class));
  }

  @EventHandler
  public void onNodeDelete(NodesDeletedEvent event) {
    for (NodeGroup group : groups) {
      group.removeAll(event.getNodes());
    }
  }
}
