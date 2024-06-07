package de.cubbossa.pathfinder.node.selection.attribute

import com.mojang.brigadier.arguments.ArgumentType
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.NamespacedKey.Companion.fromString
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser.NodeArgumentContext
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute
import de.cubbossa.pathfinder.storage.StorageUtil
import de.cubbossa.pathfinder.util.SelectionParser
import kotlinx.coroutines.runBlocking
import lombok.Getter
import org.pf4j.Extension
import java.util.stream.Collectors

@Getter
@Extension(points = [NodeSelectionAttribute::class])
class GroupSelectionAttribute : NodeSelectionAttribute<Collection<NodeGroup>> {

    override val key = "group"

    override val valueType: ArgumentType<Collection<NodeGroup>>
        get() {
            return ArgumentType<Collection<NodeGroup>> {
                runBlocking {
                    val input = it.remaining
                    val groups: MutableCollection<NodeGroup> = HashSet()
                    val key: NamespacedKey
                    try {
                        key = fromString(input)
                    } catch (t: Throwable) {
                        throw IllegalArgumentException("Invalid namespaced key: '$input'.")
                    }
                    val group = PathFinder.get().storage.loadGroup(key)
                        ?: throw IllegalArgumentException("There is no group with the key '$key'")
                    groups.add(group)
                    groups
                }
            }
        }

    override val attributeType: NodeSelectionAttribute.Type
        get() = NodeSelectionAttribute.Type.FILTER

    override fun execute(context: NodeArgumentContext<Collection<NodeGroup>>): List<Node> {
        return context.scope.stream()
            .filter { StorageUtil.getGroups(it).containsAll(context.value) }
            .collect(Collectors.toList())
    }

    override fun getStringSuggestions(context: SelectionParser.SuggestionContext): List<String> =
        runBlocking {
            PathFinder.get().storage.loadAllGroups().stream()
                .map(NodeGroup::key)
                .map(NamespacedKey::toString)
                .collect(Collectors.toList())

        }
}
