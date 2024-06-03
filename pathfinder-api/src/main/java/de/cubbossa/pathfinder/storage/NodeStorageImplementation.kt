package de.cubbossa.pathfinder.storage

import de.cubbossa.pathfinder.node.Node
import java.util.*

interface NodeStorageImplementation<N : Node> {

    fun loadNode(uuid: UUID): N?

    fun loadNodes(ids: Collection<UUID>): Collection<N>

    fun loadAllNodes(): Collection<N>

    fun saveNode(node: N)

    fun deleteNodes(node: Collection<N>)
}
