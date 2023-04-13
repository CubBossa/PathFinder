package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.PathFinderProvider;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.node.Node;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class SimpleNodeGroup extends HashSet<UUID> implements NodeGroup {

  private final PathFinder pathFinder = PathFinderProvider.get();
  private final NamespacedKey key;
  @Getter
  private final Map<Class<? extends Modifier>, Modifier> modifiers;
  @Getter
  @Setter
  private float weight = 1;

  public SimpleNodeGroup(NamespacedKey key) {
    this(key, new HashSet<>());
  }

  public SimpleNodeGroup(NamespacedKey key, Collection<Node<?>> nodes) {
    super(nodes.stream().map(Node::getNodeId).toList());
    this.key = key;
    this.modifiers = new HashMap<>();
  }

  public CompletableFuture<Collection<Node<?>>> resolve() {
    return pathFinder.getStorage().loadNodes(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimpleNodeGroup group)) {
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
    return Double.compare(weight, o.getWeight());
  }
}
