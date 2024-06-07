package de.cubbossa.pathfinder.node.selection

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.suggestion.Suggestion
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser.NodeArgumentContext
import de.cubbossa.pathfinder.util.SelectionParser
import org.pf4j.ExtensionPoint

interface NodeSelectionAttribute<ValueT> : ExtensionPoint {

    val key: String
    val valueType: ArgumentType<ValueT>
    val attributeType: Type

    fun executeAfter(): Collection<String> {
        return emptyList()
    }

    fun execute(context: NodeArgumentContext<ValueT>): List<Node>

    fun getSuggestions(context: SelectionParser.SuggestionContext): List<Suggestion> {
        return emptyList()
    }

    fun getStringSuggestions(context: SelectionParser.SuggestionContext): List<String> {
        return emptyList()
    }

    enum class Type {
        SORT,
        FILTER,
        PEEK
    }
}
