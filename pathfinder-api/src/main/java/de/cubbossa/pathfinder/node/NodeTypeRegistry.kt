package de.cubbossa.pathfinder.node

import de.cubbossa.disposables.Disposable
import de.cubbossa.pathfinder.misc.NamespacedKey

interface NodeTypeRegistry : Disposable {

    val typeKeys: Collection<NamespacedKey>
    val types: Collection<NodeType<*>>

    fun <N : Node> getType(key: NamespacedKey): NodeType<N>?

    fun <N : Node> register(type: NodeType<N>)

    fun <N : Node> unregister(type: NodeType<N>)

    fun unregister(key: NamespacedKey)
}
