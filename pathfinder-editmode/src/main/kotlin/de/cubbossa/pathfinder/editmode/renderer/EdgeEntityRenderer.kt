package de.cubbossa.pathfinder.editmode.renderer

import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.editmode.menu.LEFT_CLICK_EDGE
import de.cubbossa.pathfinder.editmode.menu.RIGHT_CLICK_EDGE
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import de.cubbossa.pathfinder.util.FutureUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.joml.Matrix4f
import org.joml.Vector3f
import java.util.*
import java.util.stream.Collectors

class EdgeEntityRenderer(plugin: JavaPlugin) :
    AbstractEntityRenderer<Edge, BlockDisplay>(plugin, BlockDisplay::class.java),
    GraphRenderer<Player> {
    init {
        setRenderDistance(PathFinder.get().configuration.editMode.edgeArmorStandRenderDistance.toDouble())
    }

    override suspend fun location(element: Edge): Location = runBlocking {

        val start = async { BukkitVectorUtils.toBukkit(element.resolveStart().join().location) }
        val end = async { BukkitVectorUtils.toBukkit(element.resolveEnd().join().location) }

        val startLoc = start.await();
        val endLoc = end.await();

        return@runBlocking BukkitUtils.lerp(startLoc, endLoc, .3)
    }

    override fun handleInteract(
        player: Player,
        slot: Int,
        left: Boolean
    ): Action<TargetContext<Edge>> {
        return if (left) LEFT_CLICK_EDGE else RIGHT_CLICK_EDGE
    }

    override fun equals(a: Edge, b: Edge): Boolean {
        return a.start == b.start && a.end == b.end
    }

    override fun render(element: Edge, entity: BlockDisplay) {
        val dir = FutureUtils.both(element.resolveStart(), element.resolveEnd())
            .thenApply { entry: Map.Entry<Node, Node> ->
                BukkitVectorUtils.toBukkit(
                    entry.key.location.clone().subtract(entry.value.location).asVector()
                )
            }
            .join()
        entity.block = Material.ORANGE_CONCRETE.createBlockData()

        entity.setTransformationMatrix(
            Matrix4f()
                .rotateTowards(
                    Vector3f(dir.x.toFloat(), dir.y.toFloat(), dir.z.toFloat()),
                    Vector3f(0f, 1f, 0f)
                )
                .translate(Vector3f(.5f).mul(NODE_SCALE).mul(-1f))
                .scale(NODE_SCALE)
        )
    }

    override fun hitbox(element: Edge, entity: Interaction) {
        entity.interactionWidth = java.lang.Float.max(NODE_SCALE.x, NODE_SCALE.z)
        entity.interactionHeight = NODE_SCALE.y
        entity.teleport(entity.location.subtract(0.0, NODE_SCALE.y / 2.0, 0.0))
    }

    override suspend fun clear(player: PathPlayer<Player>) {
        hideElements(entityNodeMap.values, player.unwrap())
        players.remove(player)
    }

    override suspend fun renderNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        // all edges from rendered nodes to adjacent nodes
        val toRender: Collection<Edge> = nodes.stream()
            .map { obj: Node -> obj.edges }.flatMap { obj: Collection<Edge> -> obj.stream() }
            .collect(Collectors.toSet())

        val ids: Collection<UUID> = nodes.stream().map { obj: Node -> obj.nodeId }.toList()

        hideElements(
            ArrayList(entityNodeMap.values).stream()
                .filter { edge: Edge -> ids.contains(edge.start) }.toList(), player.unwrap()
        )

        players.add(player)
        return showElements(toRender, player.unwrap())
    }

    override suspend fun eraseNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        val nodeIds: Collection<UUID> =
            nodes.stream().map { obj: Node -> obj.nodeId }.collect(Collectors.toSet())
        val toErase: Collection<Edge> = entityNodeMap.values.stream()
            .filter { edge: Edge -> nodeIds.contains(edge.start) || nodeIds.contains(edge.end) }
            .collect(Collectors.toSet())
        hideElements(toErase, player.unwrap())
    }

    companion object {
        private val NODE_SCALE: Vector3f = Vector3f(1f, 1f, 1.618f).mul(0.25f)
    }
}
