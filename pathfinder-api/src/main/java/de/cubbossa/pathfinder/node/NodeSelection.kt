package de.cubbossa.pathfinder.node

import org.jetbrains.annotations.Contract
import java.util.*

suspend fun NodeSelection(selection: String): NodeSelection {
    checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
    return NodeSelectionProvider.provider!!.of(selection)
}

fun NodeSelection(selection: String, scope: Iterable<Node>): NodeSelection {
    checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
    return NodeSelectionProvider.provider!!.of(selection, scope)
}

fun NodeSelection(scope: Iterable<Node>): NodeSelection {
    checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
    return NodeSelectionProvider.provider!!.of(scope)
}

suspend fun NodeSelection(selection: String, sender: Any): NodeSelection {
    checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
    return NodeSelectionProvider.provider!!.ofSender(selection, sender)
}

fun NodeSelection(selection: String, scope: Iterable<Node>, sender: Any): NodeSelection {
    checkNotNull(NodeSelectionProvider.provider) { "NodeSelectionProvider not yet assigned!" }
    return NodeSelectionProvider.provider!!.ofSender(selection, scope, sender)
}

interface NodeSelection : MutableList<Node> {

    val selectionString: String?
    val ids: Collection<UUID>
        get() = stream().map(Node::nodeId).toList()

    @Contract(pure = true)
    fun apply(selectionFilter: String): NodeSelection {
        return NodeSelection(selectionFilter, this)
    }
}
