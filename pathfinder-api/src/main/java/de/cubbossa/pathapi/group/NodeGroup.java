package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.Changes;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.node.Node;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.ApiStatus;

/**
 * A NodeGroup is the way to apply behaviour to a set of nodes.
 * <br/>
 * NodeGroups extend {@link Modified} and the set of modifiers apply to all contained nodes.
 * If multiple groups apply to a node, the weight of the group decides its priority. The higher the weight,
 * the more important is the group. If two groups apply two conflicting modifiers to the same node, the modifier from
 * the group with the higher weight will be preferred.
 */
public interface NodeGroup extends Keyed, Modified, Set<UUID>, Comparable<NodeGroup> {

  /**
   * If multiple groups apply to a node, the weight of the group decides its priority. The higher the weight,
   *  * the more important is the group. If two groups apply two conflicting modifiers to the same node, the modifier from
   *  * the group with the higher weight will be preferred.
   * @return The weight of the NodeGroup.
   */
  float getWeight();

  /**
   * If multiple groups apply to a node, the weight of the group decides its priority. The higher the weight,
   *  * the more important is the group. If two groups apply two conflicting modifiers to the same node, the modifier from
   *  * the group with the higher weight will be preferred.
   * @param weight The new weight of the NodeGroup.
   */
  void setWeight(float weight);

  /**
   * Turns the stored list of UUIDs into its according Node instances.
   * Internally, a storage access is necessary, therefore, the method returns a CompletableFuture.
   * Results might be cached.
   * @return The resolved collection of Node instances
   */
  CompletableFuture<Collection<Node>> resolve();

  @ApiStatus.Internal
  @Deprecated
  Changes<UUID> getContentChanges();

  @ApiStatus.Internal
  @Deprecated
  Changes<Modifier> getModifierChanges();
}
