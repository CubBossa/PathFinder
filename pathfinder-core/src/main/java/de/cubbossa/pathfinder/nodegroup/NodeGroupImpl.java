package de.cubbossa.pathfinder.nodegroup;

import de.cubbossa.pathapi.Changes;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.util.ModifiedHashMap;
import de.cubbossa.pathfinder.util.ModifiedHashSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NodeGroupImpl extends ModifiedHashSet<UUID> implements NodeGroup {

  private final PathFinder pathFinder = PathFinderProvider.get();
  private final NamespacedKey key;
  private final ModifiedHashMap<NamespacedKey, Modifier> modifiers;
  @Getter
  @Setter
  private float weight = 1;

  public NodeGroupImpl(NamespacedKey key) {
    this(key, new HashSet<>());
  }

  public NodeGroupImpl(NamespacedKey key, Collection<Node> nodes) {
    super(nodes.stream().map(Node::getNodeId).toList());
    this.key = key;
    this.modifiers = new ModifiedHashMap<>();
  }

  @Override
  public Changes<Modifier> getModifierChanges() {
    return modifiers.getChanges();
  }

  @Override
  public Changes<UUID> getContentChanges() {
    return getChanges();
  }

  @Override
  public CompletableFuture<Collection<Node>> resolve() {
    return pathFinder.getStorage().loadNodes(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeGroupImpl group)) {
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

  public Collection<Modifier> getModifiers() {
    return modifiers.values();
  }

  @Override
  public <C extends Modifier> boolean hasModifier(Class<C> modifierClass) {
    return modifiers.values().stream().anyMatch(modifierClass::isInstance);
  }

  @Override
  public <M extends Modifier> boolean hasModifier(NamespacedKey modifierType) {
    return modifiers.containsKey(modifierType);
  }

  @Override
  public void addModifier(NamespacedKey key, Modifier modifier) {
    modifiers.put(key, modifier);
  }

  @Override
  public <M extends Modifier> Optional<M> getModifier(NamespacedKey key) {
    return Optional.ofNullable((M) modifiers.get(key));
  }

  @Override
  public <C extends Modifier> void removeModifier(Class<C> modifierClass) {
    new HashMap<>(modifiers).forEach((k, modifier) -> {
      if (modifier.getClass().equals(modifierClass) || modifier.getClass().isInstance(modifierClass.getName())) {
        modifiers.remove(k);
      }
    });
  }

  @Override
  public <C extends Modifier> void removeModifier(C modifier) {
    modifiers.values().remove(modifier);
  }

  @Override
  public <C extends Modifier> void removeModifier(NamespacedKey key) {
    modifiers.remove(key);
  }

  @Override
  public void clearModifiers() {
    modifiers.clear();
  }

  @Override
  public int compareTo(@NotNull NodeGroup o) {
    return Double.compare(weight, o.getWeight());
  }

  @Override
  public String toString() {
    return "NodeGroupImpl{"
        + ", key=" + key
        + ", modifiers=" + modifiers
        + ", weight=" + weight
        + '}';
  }
}
