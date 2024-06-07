package de.cubbossa.pathfinder.command

import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import de.cubbossa.pathfinder.*
import de.cubbossa.pathfinder.command.Arguments.Companion.LIST_SYMBOLS
import de.cubbossa.pathfinder.command.Arguments.Companion.TAGS
import de.cubbossa.pathfinder.command.util.CommandUtils
import de.cubbossa.pathfinder.group.DiscoverableModifier
import de.cubbossa.pathfinder.group.NavigableModifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.group.hasModifier
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.Pagination
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.navigation.NavigationModule
import de.cubbossa.pathfinder.navigation.query.FindQueryParser
import de.cubbossa.pathfinder.node.*
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import de.cubbossa.pathfinder.visualizer.query.SearchTermHolder
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.SuggestionInfo
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentInfo
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import java.util.stream.Collectors

private class Arguments {
    companion object {
        val TAGS: MutableCollection<String> = ArrayList(
            listOf(
                "<rainbow>", "<gradient>", "<click>", "<hover>",
                "<rainbow:", "<gradient:", "<click:", "<hover:"
            )
        )
        val MINI_FINISH: Pattern = Pattern.compile(".*(</?[^<>]*)")
        val MINI_CLOSE: Pattern = Pattern.compile(".*<([^/<>:]+)(:[^/<>]+)?>[^/<>]*")
        val LIST_SYMBOLS: List<Char> = listOf('!', '&', '|', ')', '(')
        val LIST_SYMBOLS_STRING: List<String> = listOf("!", "&", "|", ")", "(")

        init {
            TAGS.addAll(
                NamedTextColor.NAMES.keys().stream()
                    .map { "<${it}>" }
                    .toList())
            TAGS.addAll(
                TextDecoration.NAMES.keys().stream()
                    .map { "<${it}>" }
                    .toList())
        }
    }
}

private fun <B, T> customArgument(
    base: Argument<B>,
    parser: suspend (info: CustomArgumentInfo<B>) -> T
): CustomArgument<T, B> {
    return CustomArgument(base) { runBlocking { parser(it) } }
}

fun <S, T : Argument<S>> pathFinderArgument(argument: T): CommandArgument<S, T> {
    return CommandArgument(argument)
}

fun player(node: String): CommandArgument<Player, PlayerArgument> {
    return CommandArgument(PlayerArgument(node))
}

fun pathPlayer(node: String): CommandArgument<PathPlayer<Player>, CustomArgument<PathPlayer<Player>, Player>> {
    return CommandArgument(CustomArgument(PlayerArgument(node)) { info ->
        BukkitUtils.wrap(info.currentInput())
    })
}

fun pathPlayers(node: String): CommandArgument<Collection<PathPlayer<Player>>, CustomArgument<Collection<PathPlayer<Player>>, Collection<*>>> {
    return CommandArgument(CustomArgument(EntitySelectorArgument.ManyPlayers(node)) { info ->
        (info.currentInput() as Collection<*>).stream()
            .filter { it is Player }
            .map { it as Player }
            .map { it.asPathPlayer() }
            .collect(Collectors.toList())
    })
}

fun Argument<*>.locationArgument(
    node: String,
    type: LocationType = LocationType.PRECISE_POSITION,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    pathFinderArgument(
        CustomArgument(LocationArgument(node, type)) { info ->
            info.currentInput().toPathFinder()
        }
    ).apply(block)
)


fun CommandTree.literalArgument(literal: String, block: Argument<*>.() -> Unit = {}): CommandTree =
    then(CustomLiteralArgument(literal).apply(block))

fun Argument<*>.literalArgument(
    literal: String,
    block: Argument<*>.() -> Unit = {}
): CustomLiteralArgument =
    then(CustomLiteralArgument(literal).apply(block)) as CustomLiteralArgument

fun integer(node: String): CommandArgument<Int, IntegerArgument> {
    return CommandArgument(IntegerArgument(node))
}

fun integer(node: String, min: Int): CommandArgument<Int, IntegerArgument> {
    return CommandArgument(IntegerArgument(node, min))
}

fun integer(node: String, min: Int, max: Int): CommandArgument<Int, IntegerArgument> {
    return CommandArgument(IntegerArgument(node, min, max))
}

/**
 * An argument that suggests and resolves enum fields in lower case syntax.
 *
 * @param nodeName The name of the command argument in the command structure
 * @param scope    The enum class instance
 * @param <E>      The enum type
 * @return The argument
 */
inline fun <reified E : Enum<E>> enumArgument(nodeName: String): Argument<E> {
    return pathFinderArgument(CustomArgument(StringArgument(nodeName)) {
        try {
            enumValueOf<E>(it.input().uppercase())
        } catch (e: IllegalArgumentException) {
            throw CustomArgument.CustomArgumentException.fromString("Invalid input value: ${it.input()}")
        }
    }).includeSuggestions { _, suggestionsBuilder ->
        Arrays.stream(E::class.java.getEnumConstants())
            .map { it.toString() }
            .map { it.lowercase() }
            .forEach { suggestionsBuilder.suggest(it) }
        suggestionsBuilder.buildFuture()
    }
}

fun Argument<*>.paginationArgument(size: Int, block: Argument<*>.() -> Unit = {}): Argument<*> =
    then(pathFinderArgument(CustomArgument(IntegerArgument("page", 1)) {
        Pagination.page(it.currentInput() - 1, size)
    })).apply(block)

/**
 * Provides a MiniMessage BukkitNodeSelectionArgument, which autocompletes xml tags and contains all default tags
 * that come along with MiniMessage.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return a MiniMessage argument instance
 */
fun miniMessageArgument(nodeName: String): Argument<String> {
    return miniMessageArgument(nodeName) { ArrayList() }
}

/**
 * Provides a MiniMessage BukkitNodeSelectionArgument, which autocompletes xml tags and contains all default tags
 * that come along with MiniMessage.
 *
 * @param nodeName The name of the command argument in the command structure
 * @param supplier Used to insert custom tags into the suggestions
 * @return a MiniMessage argument instance
 */
fun miniMessageArgument(
    nodeName: String,
    supplier: (SuggestionInfo<*>) -> Collection<String>
): Argument<String> {
    return pathFinderArgument(GreedyStringArgument(nodeName))
        .includeSuggestions { info, builder ->

            if (info.currentArg.isEmpty()) {
                TAGS.forEach(builder::suggest)
                return@includeSuggestions builder.buildFuture()
            }

            val offset: Int = builder.input.length
            val splits = info.currentInput.split(" ") ?: listOf("")
            val input = splits[splits.size - 1]
            var range = StringRange.between(offset - input.length, offset)

            val suggestions = ArrayList<Suggestion>()
            supplier(info).stream()
                .filter { it.startsWith(input) }
                .map { Suggestion(range, it) }
                .forEach { suggestions.add(it) }

            val m = MINI_FINISH.matcher(info.currentInput())
            if (m.matches()) {
                val result = m.toMatchResult()
                range = StringRange.between(result.start(1), result.end(1))
                val filter = result.group(1)
                val finalRange = range
                TAGS.stream()
                    .filter { it.startsWith(filter) }
                    .map { Suggestion(finalRange, it) }
                    .forEach { suggestions.add(it) }

                suggestions.add(Suggestion(range, m.group(1) + ">"))
            } else {
                val matcher = MINI_CLOSE.matcher(info.currentArg)
                if (matcher.matches()) {
                    suggestions.add(
                        Suggestion(StringRange.at(offset), "</${matcher.group(1)}>")
                    )
                }
            }
            CompletableFuture.completedFuture(
                Suggestions.create(builder.input, suggestions)
            )
        }
}

/**
 * Provides a path visualizer argument, which suggests the namespaced keys for all path
 * visualizers and resolves the user input into the actual visualizer instance.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return a path visualizer argument instance
 */
fun Argument<*>.pathVisualizerArgument(
    nodeName: String,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    pathFinderArgument(
        customArgument(NamespacedKeyArgument(nodeName)) { customArgumentInfo ->
            runBlocking {
                val vis = PathFinder.get().getStorage()
                    .loadVisualizer<PathVisualizer<*, *>>(
                        BukkitPathFinder.convert(
                            customArgumentInfo.currentInput()
                        )
                    )
                if (vis == null) {
                    throw CustomArgument.CustomArgumentException.fromString("There is no visualizer with this key.")
                }
                vis
            }
        }
    ).includeSuggestions(suggestNamespacedKeys {
        PathFinder.get().getStorage().loadVisualizers().stream()
            .map { it.key }
            .toList()
    }).apply(block)
)

/**
 * Provides a path visualizer argument, which suggests the namespaced keys for all path
 * visualizers of the provided visualizer type and resolves the user input into the actual
 * visualizer instance.
 *
 * @param nodeName The name of the command argument in the command structure
 * @param type     The type that all suggested and parsed visualizers are required to have
 * @return a path visualizer argument instance
 */
fun Argument<*>.pathVisualizerArgument(
    nodeName: String,
    type: VisualizerType<PathVisualizer<*, *>>,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    pathFinderArgument(
        customArgument(NamespacedKeyArgument(nodeName)) { customArgumentInfo ->
            PathFinder.get().getStorage()
                .loadVisualizer<PathVisualizer<*, *>>(
                    BukkitPathFinder.convert(
                        customArgumentInfo.currentInput()
                    )
                )
                ?: throw CustomArgument.CustomArgumentException.fromString("There is no visualizer with this key.")
        }
    ).includeSuggestions(suggestNamespacedKeys {
        PathFinder.get().getStorage().loadVisualizers(type).stream()
            .map { it.key }
            .toList()
    }).apply(block)
)

/**
 * Suggests a set of NamespacedKeys where the completion also includes matches for only the
 * key.
 *
 * @return the argument suggestions object to insert
 */
private fun suggestNamespacedKeys(supplier: suspend (CommandSender) -> Collection<NamespacedKey>): ArgumentSuggestions<CommandSender> {
    return ArgumentSuggestions { suggestionInfo, suggestionsBuilder ->
        runBlocking {
            val keys = supplier(suggestionInfo.sender)
            val splits = suggestionInfo.currentInput.split(" ")
            val input = splits[splits.size - 1]
            val len = suggestionInfo.currentInput().length
            val range = StringRange.between(len - input.length, len)

            val suggestions = keys.stream()
                .filter { it.key.startsWith(input) || it.namespace.startsWith(input) }
                .map { it.toString() }
                .map { Suggestion(range, it) }
                .collect(Collectors.toList())

            CompletableFuture.completedFuture(
                Suggestions.create(suggestionsBuilder.input, suggestions)
            )
        }
    }
}

/**
 * Provides a node type argument, which suggests the keys of all registered node types and
 * resolves the user input into the node type instance.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return a node type argument instance
 */
fun Argument<*>.nodeTypeArgument(
    nodeName: String,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    pathFinderArgument(
        customArgument(NamespacedKeyArgument(nodeName)) { customArgumentInfo ->
            val type = PathFinder.get().getNodeTypeRegistry()
                .getType<Node>(BukkitPathFinder.convert(customArgumentInfo.currentInput()))
            if (type == null) {
                throw CustomArgument.CustomArgumentException.fromString(
                    "Node type with key '${customArgumentInfo.currentInput}' does not exist."
                )
            }
            type
        }
    ).includeSuggestions(
        suggestNamespacedKeys {
            PathFinder.get().getNodeTypeRegistry().typeKeys
        }
    ).apply(block)
)

/**
 * Provides a node selection argument.
 * This comes with a custom syntax that is a copy of the vanilla entity selectors.
 * There are a variety of filters to apply to the search, an example user input could be
 * "@n[distance=..10]", which returns all nodes within a range of 10 blocks.
 * This includes ALL nodes of all roadmaps.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return a node selection argument instance
 */
fun Argument<*>.nodeSelectionArgument(
    nodeName: String,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(pathFinderArgument(
    CustomArgument(TextArgument(nodeName)) { info ->
        if (info.sender is Player) {
            try {
                NodeSelection.ofSender(
                    info.input().substring(1, info.input().length - 1),
                    info.sender
                )
            } catch (e: ParseCancellationException) {
                throw CustomArgument.CustomArgumentException.fromString(e.message)
            }
        }
        NodeSelectionImpl()
    }
).includeSuggestions { suggestionInfo, _ ->
    val offset =
        suggestionInfo.currentInput().length - suggestionInfo.currentArg().length

    val suggestions =
        NodeSelectionProviderImpl.getNodeSelectionSuggestions(suggestionInfo).join()
    //  add quotations to suggestions
    CommandUtils.wrapWithQuotation(
        suggestionInfo.currentArg(),
        suggestions,
        suggestionInfo.currentArg(),
        offset
    )
    // shift suggestions toward actual command argument offset
    CommandUtils.offsetSuggestions(suggestionInfo.currentArg(), suggestions, offset)
    CompletableFuture.completedFuture(suggestions)
}.apply(block)
)

/**
 * Provides a node group argument, which suggests the keys of all node groups and resolves the
 * user input into the node group instance.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return a node group argument instance
 */
fun Argument<*>.nodeGroupArgument(
    nodeName: String,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(pathFinderArgument(
    CustomArgument<NodeGroup, org.bukkit.NamespacedKey>(
        NamespacedKeyArgument(
            nodeName
        )
    ) { info ->
        runBlocking {
            PathFinder.get().getStorage()
                .loadGroup(BukkitPathFinder.convert(info.currentInput()))
        }
    }
).replaceSuggestions(
    suggestNamespacedKeys {
        val nodeGroups = PathFinder.get().getStorage().loadAllGroups()
        nodeGroups.stream().map { it.key }.toList()
    }
).apply(block)
)

/**
 * A command argument that resolves and suggests Discoverables as their namespaced keys.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return The CustomArgument instance
 */
fun discoverableArgument(nodeName: String): CommandArgument<NamespacedKey, Argument<NamespacedKey>> {
    return pathFinderArgument(CustomArgument(NamespacedKeyArgument(nodeName)) {
        BukkitPathFinder.convert(it.currentInput())
    }.includeSuggestions(suggestNamespacedKeys {
        val nodeGroups = PathFinder.get().getStorage().loadAllGroups()
        nodeGroups.stream()
            .filter { it.hasModifier<DiscoverableModifier>() }
            .map { it.key }
            .collect(Collectors.toList())
    }))
}

fun CommandTree.navigateSelectionArgument(
    nodeName: String,
    block: Argument<*>.() -> Unit
): Argument<*> = navigateSelectionArgument(nodeName).apply(block)

fun Argument<*>.navigateSelectionArgument(
    nodeName: String,
    block: Argument<*>.() -> Unit
): Argument<*> = navigateSelectionArgument(nodeName).apply(block)

/**
 * A command argument that resolves a navigation query and suggests the according search terms.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return The CustomArgument instance
 */
fun navigateSelectionArgument(nodeName: String): Argument<*> = pathFinderArgument(
    customArgument(GreedyStringArgument(nodeName)) { context ->
        if (context.sender !is Player) {
            throw CustomArgument.CustomArgumentException.fromString("Only for players")
        }
        val search = context.currentInput().replace(" ", "")
        val storage = PathFinder.get().getStorage()
        val scope = storage.loadNodes<NavigableModifier>(NavigableModifier.KEY)
        val valids = NavigationModule.get<Player>().applyNavigationConstraints(
            (context.sender as Player).uniqueId,
            scope.keys
        )

        val result = HashMap<Node, MutableCollection<SearchTermHolder>>()
        valids.forEach { result[it]?.addAll(scope[it] as Collection<SearchTermHolder>) }

        try {
            val target = FindQueryParser().parse(
                search,
                ArrayList(valids)
            ) { n ->
                result[n]?.stream()
                    ?.map { it.getSearchTerms() }
                    ?.flatMap { it.stream() }
                    ?.collect(Collectors.toList())
            }
            NodeSelectionImpl(target)
        } catch (t: Throwable) {
            t.printStackTrace()
            throw RuntimeException(t)
        }
    })
    .includeSuggestions { suggestionInfo, suggestionsBuilder ->
        if (suggestionInfo.sender !is Player) {
            return@includeSuggestions suggestionsBuilder.buildFuture()
        }
        val input = suggestionsBuilder.input

        var lastIndex = LIST_SYMBOLS.stream()
            .map { input.lastIndexOf(it) }
            .mapToInt { it }
            .max()
            .orElse(0)
        lastIndex = Integer.max(
            suggestionsBuilder.input.length - suggestionsBuilder.remaining.length,
            lastIndex + 1
        )

        val range = StringRange.between(lastIndex, input.length)
        val inRange = range.get(input)

        launchIO {
            val map = PathFinder.get().getStorage()
                .loadNodes<NavigableModifier>(NavigableModifier.KEY)
            map.values.stream()
                .flatMap { it.stream() }
                .map(NavigableModifier::getSearchTermStrings)
                .flatMap { it.stream() }
                .filter { it.startsWith(inRange) }
                .forEach(suggestionsBuilder::suggest)
        }
        suggestionsBuilder.buildFuture()
    }

/**
 * Provides a visualizer type argument, which suggests the keys of all registered visualizer types
 * and resolves the user input into the visualizer type instance.
 *
 * @param nodeName The name of the command argument in the command structure
 * @return a visualizer type argument instance
 */
inline fun Argument<*>.visualizerTypeArgument(
    nodeName: String,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(pathFinderArgument(
    CustomArgument(NamespacedKeyArgument(nodeName)) { customArgumentInfo ->

        val type = PathFinder.get().visualizerTypeRegistry
            .getType<PathVisualizer<*, *>>(
                BukkitPathFinder.convert(
                    customArgumentInfo.currentInput()
                )
            )
        if (type == null) {
            throw CustomArgument.CustomArgumentException.fromString(
                "Unknown type: '" + customArgumentInfo.currentInput() + "'."
            )
        }
        type
    }
).includeSuggestions(suggestNamespacedKeys {
    PathFinder.get().getVisualizerTypeRegistry().types.keys
}).apply(block)
)

