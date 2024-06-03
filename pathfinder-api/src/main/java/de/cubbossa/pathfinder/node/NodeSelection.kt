package de.cubbossa.pathfinder.node

import org.jetbrains.annotations.Contract
import java.util.*

interface NodeSelection : MutableList<Node> {

    val selectionString: String?
    val ids: Collection<UUID>
        get() = stream().map(Node::nodeId).toList()

    @Contract(pure = true)
    fun apply(selectionFilter: String): NodeSelection {
        return of(selectionFilter, this)
    }

    companion object {
        fun of(selection: String): NodeSelection {
            checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
            return NodeSelectionProvider.provider.of(selection)
        }

        fun of(selection: String, scope: Iterable<Node>): NodeSelection {
            checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
            return NodeSelectionProvider.provider.of(selection, scope)
        }

        fun of(scope: Iterable<Node>): NodeSelection {
            checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
            return NodeSelectionProvider.provider.of(scope)
        }

        fun ofSender(selection: String, sender: Any): NodeSelection {
            checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
            return NodeSelectionProvider.provider.ofSender(selection, sender)
        }

        fun ofSender(selection: String, scope: Iterable<Node>, sender: Any): NodeSelection {
            checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
            return NodeSelectionProvider.provider.ofSender(selection, scope, sender)
        }
    }
}
