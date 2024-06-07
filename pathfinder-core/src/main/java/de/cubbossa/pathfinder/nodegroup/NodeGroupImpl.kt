package de.cubbossa.pathfinder.nodegroup

import de.cubbossa.pathfinder.Changes
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.ModifiedHashMap
import de.cubbossa.pathfinder.util.ModifiedHashSet
import lombok.Getter
import lombok.Setter
import java.util.*
import java.util.concurrent.CompletableFuture

class NodeGroupImpl(override val key: NamespacedKey, nodes: Collection<Node> = HashSet()) :
    ModifiedHashSet<UUID>(nodes.stream().map(Node::nodeId).toList()), NodeGroup {
    
    private val pathFinder: PathFinder = PathFinder.get()
    private val modifierMap: ModifiedHashMap<NamespacedKey, Modifier> =
        ModifiedHashMap()
    override val modifiers: Collection<Modifier>
        get() = modifierMap.values
    override var weight = 1f

    override val modifierChanges: Changes<Modifier>
        get() = modifierMap.changes

    override val contentChanges: Changes<UUID>
        get() = changes

    override suspend fun resolve(): Collection<Node> {
        return pathFinder.storage.loadNodes(this)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is NodeGroupImpl) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }
        return key.equals(o.key)
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun <C: Modifier> hasModifier(modifierClass: Class<C>): Boolean {
        return modifiers.stream().anyMatch { obj: Modifier? -> modifierClass.isInstance(obj) }
    }

    override fun <M : Modifier> hasModifier(modifierType: NamespacedKey): Boolean {
        return modifierMap.containsKey(modifierType)
    }

    override fun addModifier(key: NamespacedKey, modifier: Modifier) {
        modifierMap[key] = modifier
    }

    override fun <M : Modifier> getModifier(key: NamespacedKey): M? {
        return modifierMap[key] as M?
    }

    override fun <C : Modifier> removeModifier(modifierClass: Class<C>) {
        HashMap(modifierMap).forEach { (k: NamespacedKey, modifier: Modifier) ->
            if (modifier.javaClass == modifierClass || modifier.javaClass.isInstance(modifierClass.name)) {
                modifierMap.remove(k)
            }
        }
    }

    override fun <C: Modifier> removeModifier(modifier: C) {
        modifierMap.values.remove(modifier)
    }

    override fun removeModifier(key: NamespacedKey) {
        modifierMap.remove(key)
    }

    override fun clearModifiers() {
        modifierMap.clear()
    }

    override fun compareTo(other: NodeGroup): Int {
        return weight.toDouble().compareTo(other.weight.toDouble())
    }

    override fun toString(): String {
        return ("""NodeGroupImpl{, key=$key, modifiers=$modifiers, weight=$weight}""")
    }
}
