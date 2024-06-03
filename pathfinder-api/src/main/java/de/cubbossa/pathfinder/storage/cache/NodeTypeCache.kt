package de.cubbossa.pathfinder.storage.cache

import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import java.util.*

interface NodeTypeCache : StorageCache<UUID> {
    fun <N : Node> getType(uuid: UUID): NodeType<N>?

    fun getTypes(uuids: Collection<UUID>): StorageCache.CacheMap<UUID, NodeType<*>>

    fun write(uuid: UUID, type: NodeType<*>)
}
