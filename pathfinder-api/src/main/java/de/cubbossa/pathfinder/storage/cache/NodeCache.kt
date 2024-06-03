package de.cubbossa.pathfinder.storage.cache

import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.storage.cache.StorageCache.CacheCollection
import java.util.*

interface NodeCache : StorageCache<Node> {
    fun <N : Node> getNode(uuid: UUID): N?

    val allNodes: Collection<Node>?

    fun getNodes(ids: Collection<UUID>): CacheCollection<UUID, Node>

    fun writeAll(nodes: Collection<Node>)

    fun write(group: NodeGroup)

    fun invalidate(uuid: UUID)
}
