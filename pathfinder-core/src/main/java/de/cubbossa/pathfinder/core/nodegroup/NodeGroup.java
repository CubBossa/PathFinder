package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Navigable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.modifier.GroupModifier;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.query.SearchQueryAttribute;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTermHolder;
import de.cubbossa.pathfinder.module.visualizing.query.SimpleSearchTerm;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

import de.cubbossa.pathfinder.util.HashedRegistry;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

public class NodeGroup extends HashSet<Groupable<?>>
    implements Keyed, Discoverable, Navigable, SearchTermHolder {

  private final NamespacedKey key;
  @Getter
  private final HashedRegistry<GroupModifier> modifiers;

  public NodeGroup(NamespacedKey key) {
    this(key, new HashSet<>());
  }

  public NodeGroup(NamespacedKey key, Collection<Waypoint> nodes) {
    super(nodes);
    this.key = key;
    this.modifiers = new HashedRegistry<>();
  }

  public void addModifier(GroupModifier modifier) {
    this.modifiers.put(modifier);
  }

  public boolean hasModifier(GroupModifier modifier) {
    return this.modifiers.containsKey(modifier.getKey());
  }

  public boolean hasModifier(Class<? extends GroupModifier> modifier) {

  }

  @Override
  public boolean add(Groupable node) {
    node.addGroup(this);
    return super.add(node);
  }

  @Override
  public boolean addAll(Collection<? extends Groupable<?>> c) {
    for (Groupable g : c) {
      g.addGroup(this);
    }
    return super.addAll(c);
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Groupable<?> groupable) {
      groupable.removeGroup(this);
      return super.remove(o);
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    for (Object o : c) {
      if (o instanceof Groupable<?> groupable) {
        groupable.removeGroup(this);
        remove(this);
      }
    }
    return true;
  }

  @Override
  public Collection<Node<?>> getGroup() {
    return new HashSet<>(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeGroup group)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return key.equals(group.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean fulfillsDiscoveringRequirements(Player player) {
    if (!discoverable) {
      return false;
    }
    if (permission != null && !player.hasPermission(permission)) {
      return false;
    }
    for (Node<?> node : this) {
      if (node == null) {
        PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Node is null");
        continue;
      }
      float dist = DiscoverHandler.getInstance().getDiscoveryDistance(player.getUniqueId(), node);
      if (node.getLocation().getX() - player.getLocation().getX() > dist) {
        continue;
      }
      if (node.getLocation().distance(player.getLocation()) > dist) {
        continue;
      }
      return true;
    }
    return false;
  }

  @Override
  public NamespacedKey getKey() {
    return key;
  }
}
