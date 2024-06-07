package de.cubbossa.pathfinder.node

import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser.NodeArgumentContext
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser.NodeSelectionArgument
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute
import de.cubbossa.pathfinder.util.ExtensionPoint
import de.cubbossa.pathfinder.util.SelectionParser
import de.cubbossa.pathfinder.util.SelectionParser.SelectionModification
import dev.jorel.commandapi.SuggestionInfo
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

class NodeSelectionProviderImpl<SenderT, ContextT : NodeArgumentContext<*>>(
    private val parser: AbstractNodeSelectionParser<SenderT, ContextT>
) : NodeSelectionProvider() {

    private val pathFinder = PathFinder.get()

    init {
        EXTENSION_POINT.extensions.forEach{ this.add(it) }
        provider = this
    }

    private fun <T> add(i: NodeSelectionAttribute<T>) {
        val arg = object: NodeSelectionArgument<T>(i.valueType) {

            override val key: String
                get() = i.key

            override fun modificationType(): SelectionModification {
                return SelectionModification.valueOf(i.attributeType.name)
            }

            override fun executeAfter(): Collection<String> {
                return i.executeAfter()
            }
        }
        arg.execute = { i.execute(it).toMutableList() }
        arg.suggest = { c: SelectionParser.SuggestionContext ->
                val suggestions = ArrayList(i.getSuggestions(c))
                suggestions.addAll(i.getStringSuggestions(c).stream()
                    .map { Suggestion(StringRange.between(0, c.input.length), it) }
                    .toList())
                suggestions
        }
        parser.addResolver(arg)
    }

    override suspend fun of(selection: String): NodeSelection {
        var scope: MutableList<Node> = ArrayList(pathFinder.storage.loadNodes())
        scope = parser.parse<Any>(selection, scope, null)
        return NodeSelectionImpl(scope, selection)
    }

    override fun of(selection: String, scope: Iterable<Node>): NodeSelection {
        var s: MutableList<Node> = ArrayList()
        scope.forEach(Consumer { e: Node -> s.add(e) })
        s = parser.parse<Node>(selection, s, null)
        return NodeSelectionImpl(s, selection)
    }

    override fun of(scope: Iterable<Node>): NodeSelection {
        val s: MutableList<Node> = ArrayList()
        scope.forEach(Consumer { e: Node -> s.add(e) })
        return NodeSelectionImpl(s)
    }

    override suspend fun ofSender(selection: String, sender: Any): NodeSelection {
        var scope: MutableList<Node> = ArrayList(pathFinder.storage.loadNodes())
        scope = parser.parse<Node>(selection, scope, sender)
        return NodeSelectionImpl(scope, selection)
    }

    override fun ofSender(selection: String, scope: Iterable<Node>, sender: Any): NodeSelection {
        var _scope: MutableList<Node> = ArrayList()
        scope.forEach(Consumer { e: Node -> _scope.add(e) })
        _scope = parser.parse<Any>(selection, _scope, sender)
        return NodeSelectionImpl(_scope, selection)
    }

    companion object {
        val EXTENSION_POINT: ExtensionPoint<NodeSelectionAttribute<*>> = ExtensionPoint(
            NodeSelectionAttribute::class.java
        )

        @JvmStatic
        fun getNodeSelectionSuggestions(suggestionInfo: SuggestionInfo<*>): CompletableFuture<Suggestions> {
            return (provider as NodeSelectionProviderImpl<*, *>).parser.applySuggestions(
                suggestionInfo.currentArg(), if (suggestionInfo.currentArg().length > 0
                ) suggestionInfo.currentArg().substring(1)
                else ""
            )
        }
    }
}
