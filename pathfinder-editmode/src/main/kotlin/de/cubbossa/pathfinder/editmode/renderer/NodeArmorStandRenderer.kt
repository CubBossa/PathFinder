package de.cubbossa.pathfinder.editmode.renderer

import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.editmode.HEAD_URL_GREEN
import de.cubbossa.pathfinder.editmode.createCustomHead
import de.cubbossa.pathfinder.editmode.menu.LEFT_CLICK_NODE
import de.cubbossa.pathfinder.editmode.menu.RIGHT_CLICK_NODE
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector

class NodeArmorStandRenderer(plugin: JavaPlugin) : AbstractArmorstandRenderer<Node>(plugin),
    GraphRenderer<Player> {

    private val nodeHead: ItemStack = createCustomHead(HEAD_URL_GREEN)

    init {
        setRenderDistance(PathFinder.get().configuration.editMode.nodeArmorStandRenderDistance.toDouble())
    }

    override suspend fun retrieveFrom(element: Node): Location {
        return BukkitVectorUtils.toBukkit(element.location).add(NODE_OFFSET)
    }

    override fun handleInteract(
        player: Player?,
        slot: Int,
        left: Boolean
    ): Action<TargetContext<Node>> {
        return if (left) LEFT_CLICK_NODE else RIGHT_CLICK_NODE
    }

    override fun equals(a: Node, b: Node): Boolean {
        return a.nodeId == b.nodeId
    }

    override fun head(element: Node): ItemStack? {
        return nodeHead.clone()
    }

    override fun isSmall(element: Node): Boolean {
        return false
    }

    override fun getName(element: Node): Component? {
        return null
    }

    override suspend fun clear(player: PathPlayer<Player>) {
        hideElements(entityNodeMap.values, player.unwrap()!!)
        players.remove(player)
    }

    override suspend fun renderNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        showElements(nodes, player.unwrap())
        players.add(player)
    }

    override suspend fun eraseNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        hideElements(nodes, player.unwrap())
    }

    companion object {
        private val NODE_OFFSET = Vector(0.0, -1.75, 0.0)
    }
}
