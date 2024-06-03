package de.cubbossa.pathfinder.storage.cache

import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.Range
import de.cubbossa.pathfinder.storage.cache.StorageCache.CacheCollection
import java.util.*

interface GroupCache : StorageCache<NodeGroup?> {
    val groups: Collection<NodeGroup>?

    fun getGroup(key: NamespacedKey): NodeGroup?

    fun getGroups(modifier: NamespacedKey): Collection<NodeGroup>?

    fun getGroups(keys: Collection<NamespacedKey>): CacheCollection<NamespacedKey, NodeGroup>

    fun getGroups(node: UUID): Collection<NodeGroup>?

    fun getGroups(range: Range): Collection<NodeGroup>?

    fun write(node: UUID, groups: Collection<NodeGroup>)

    fun write(modifier: NamespacedKey, groups: Collection<NodeGroup>)

    fun writeAll(groups: Collection<NodeGroup>)

    fun invalidate(node: UUID)

    fun invalidate(modifier: NamespacedKey)
}
