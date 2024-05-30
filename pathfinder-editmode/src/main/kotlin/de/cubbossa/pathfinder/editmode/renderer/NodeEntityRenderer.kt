package de.cubbossa.pathfinder.editmode.renderer

import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.editmode.menu.LEFT_CLICK_NODE
import de.cubbossa.pathfinder.editmode.menu.RIGHT_CLICK_NODE
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.joml.Vector3f

class NodeEntityRenderer(plugin: JavaPlugin) :
    AbstractEntityRenderer<Node, BlockDisplay>(plugin, BlockDisplay::class.java) {
    init {
        setRenderDistance(PathFinder.get().configuration.editMode.nodeArmorStandRenderDistance.toDouble())
    }

    override suspend fun clear(player: PathPlayer<Player>) {
        hideElements(entityNodeMap.values, player.unwrap()!!)
        players.remove(player)
    }

    override suspend fun renderNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        showElements(nodes, player.unwrap()!!)
        players.add(player)
    }

    override suspend fun eraseNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        hideElements(nodes, player.unwrap())
    }

    override fun equals(a: Node, b: Node): Boolean {
        return a == b
    }

    override suspend fun location(element: Node): Location {
        return BukkitVectorUtils.toBukkit(element.location)
    }

    override fun handleInteract(
        player: Player,
        slot: Int,
        left: Boolean
    ): Action<TargetContext<Node>> {
        return if (left) LEFT_CLICK_NODE else RIGHT_CLICK_NODE
    }

    override fun render(element: Node, entity: BlockDisplay) {
        entity.block = Material.LIME_CONCRETE.createBlockData()
        val t = entity.transformation
        t.translation.sub(Vector3f(NODE_SCALE, NODE_SCALE, NODE_SCALE).mul(0.5f))
        t.scale.set(NODE_SCALE)
        entity.transformation = t
    }

    override fun hitbox(element: Node, entity: Interaction) {
        entity.interactionWidth = NODE_SCALE
        entity.interactionHeight = NODE_SCALE
        entity.teleport(entity.location.subtract(0.0, NODE_SCALE / 2.0, 0.0))
    }

    companion object {
        private const val NODE_SCALE = .4f
    }
}
