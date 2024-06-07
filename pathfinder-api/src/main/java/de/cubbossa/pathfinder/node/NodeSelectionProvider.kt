package de.cubbossa.pathfinder.node

abstract class NodeSelectionProvider {

    abstract suspend fun of(selection: String): NodeSelection

    abstract fun of(selection: String, scope: Iterable<Node>): NodeSelection

    abstract fun of(scope: Iterable<Node>): NodeSelection

    abstract suspend fun ofSender(selection: String, sender: Any): NodeSelection

    abstract fun ofSender(selection: String, scope: Iterable<Node>, sender: Any): NodeSelection

    companion object {
        var provider: NodeSelectionProvider? = null
    }
}
