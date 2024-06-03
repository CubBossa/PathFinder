package de.cubbossa.pathfinder.node

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.storage.NodeStorageImplementation
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
abstract class AbstractNodeType<N : Node>(
    override val key: NamespacedKey,
    var storage: NodeStorageImplementation<N>? = null
) : NodeType<N> {

    abstract fun createNodeInstance(context: NodeType.Context): N

    override fun createAndLoadNode(context: NodeType.Context): N? {
        val node = createNodeInstance(context)
        saveNode(node)
        return node
    }

    // pass to storage methods.
    override fun loadNode(uuid: UUID): N? {
        return storage?.loadNode(uuid)
    }

    override fun loadNodes(ids: Collection<UUID>): Collection<N> {
        return storage?.loadNodes(ids) ?: HashSet<N>()
    }

    override fun loadAllNodes(): Collection<N> {
        return storage?.loadAllNodes() ?: HashSet<N>()
    }

    override fun saveNode(node: N) {
        storage?.saveNode(node)
    }

    override fun deleteNodes(node: Collection<N>) {
        storage?.deleteNodes(node)
    }
}
