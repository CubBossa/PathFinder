package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.Modified;
import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public class NodeGroup extends HashSet<Groupable<?>> implements Keyed, Modified, Comparable<NodeGroup> {

  private final NamespacedKey key;
  @Getter
  private final Map<Class<? extends Modifier>, Modifier> modifiers;
  @Getter
  @Setter
  private double weight = 1;

  public NodeGroup(NamespacedKey key) {
    this(key, new HashSet<>());
  }

  public NodeGroup(NamespacedKey key, Collection<Waypoint> nodes) {
    super(nodes);
    this.key = key;
    this.modifiers = new HashMap<>();
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
  public NamespacedKey getKey() {
    return key;
  }

  @Override
  public <C extends Modifier> boolean hasModifier(Class<C> modifierClass) {
    return modifiers.containsKey(modifierClass);
  }

  @Override
  public void addModifier(Modifier modifier) {
    modifiers.put(modifier.getClass(), modifier);
  }

  @Override
  public <C extends Modifier> C getModifier(Class<C> modifierClass) {
    return (C) modifiers.get(modifierClass);
  }

  @Override
  public <C extends Modifier> C removeModifier(Class<C> modifierClass) {
    return (C) modifiers.remove(modifierClass);
  }

  @Override
  public <C extends Modifier> C removeModifier(C modifier) {
    return (C) modifiers.remove(modifier.getClass());
  }

  @Override
  public void clearModifiers() {
    modifiers.clear();
  }

  @Override
  public int compareTo(@NotNull NodeGroup o) {
    return Integer.compare(weight, o.weight);
  }
}
