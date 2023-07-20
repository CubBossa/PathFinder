package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Range;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface GroupCache extends StorageCache<NodeGroup> {

  Optional<NodeGroup> getGroup(NamespacedKey key);

  Optional<Collection<NodeGroup>> getGroups(NamespacedKey modifier);

  CacheCollection<NamespacedKey, NodeGroup> getGroups(Collection<NamespacedKey> keys);

  Optional<Collection<NodeGroup>> getGroups();

  Optional<Collection<NodeGroup>> getGroups(UUID node);

  Optional<Collection<NodeGroup>> getGroups(Range range);

  void write(UUID node, Collection<NodeGroup> groups);

  void write(NamespacedKey modifier, Collection<NodeGroup> groups);

  void writeAll(Collection<NodeGroup> groups);

  void invalidate(UUID node);

  void invalidate(NamespacedKey modifier);
}
