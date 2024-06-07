package de.cubbossa.pathfinder.node.selection

import com.mojang.brigadier.arguments.ArgumentType
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser.NodeArgumentContext
import de.cubbossa.pathfinder.util.SelectionParser

abstract class AbstractNodeSelectionParser<SenderT, ContextT : NodeArgumentContext<*>>(
    identifier: String,
    vararg alias: String
) : SelectionParser<Node, ContextT>(identifier, *alias) {

    fun addResolver(argument: NodeSelectionArgument<*>) {
        super.addResolver(argument as Argument<*, out Node, out ContextT, *>)
    }

    abstract class NodeArgumentContext<ValueT>(value: ValueT, scope: MutableList<Node>) :
        ArgumentContext<ValueT, Node>(value, scope)

    abstract class NodeSelectionArgument<TypeT>(type: ArgumentType<TypeT>) :
        Argument<TypeT, Node, NodeArgumentContext<TypeT>, NodeSelectionArgument<TypeT>>(type)
}
