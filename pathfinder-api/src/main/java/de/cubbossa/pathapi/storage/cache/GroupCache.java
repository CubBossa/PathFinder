package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Node;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public interface GroupCache extends StorageCache<NodeGroup> {

  Optional<NodeGroup> getGroup(NamespacedKey key, Function<NamespacedKey, NodeGroup> loader);

  <M extends Modifier> Collection<NodeGroup> getGroups(Class<M> modifier, Function<Class<M>, Collection<NodeGroup>> loader);

  List<NodeGroup> getGroups(Pagination pagination, Function<Pagination, List<NodeGroup>> loader);

  Collection<NodeGroup> getGroups(Collection<NamespacedKey> keys,
                                  Function<Collection<NamespacedKey>, Collection<NodeGroup>> loader);

  Collection<NodeGroup> getGroups(Supplier<Collection<NodeGroup>> loader);

  Collection<NodeGroup> getGroups(UUID node, Function<UUID, Collection<NodeGroup>> loader);

  void write(Node node);

  void invalidate(Node node);
}
