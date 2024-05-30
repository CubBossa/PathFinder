package de.cubbossa.pathfinder.editmode.renderer

import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.editmode.HEAD_URL_ORANGE
import de.cubbossa.pathfinder.editmode.createCustomHead
import de.cubbossa.pathfinder.editmode.menu.LEFT_CLICK_EDGE
import de.cubbossa.pathfinder.editmode.menu.RIGHT_CLICK_EDGE
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

class EdgeArmorStandRenderer(plugin: JavaPlugin) : AbstractArmorstandRenderer<Edge>(plugin),
    GraphRenderer<Player> {
    private val nodeHead: ItemStack = createCustomHead(HEAD_URL_ORANGE)

    init {
        setRenderDistance(PathFinder.get().configuration.editMode.edgeArmorStandRenderDistance.toDouble())
    }

    override suspend fun retrieveFrom(element: Edge): Location = runBlocking {
        val start = async { BukkitVectorUtils.toBukkit(element.resolveStart().join().location) }
        val end = async { BukkitVectorUtils.toBukkit(element.resolveEnd().join().location) }

        val startLoc = start.await();
        val endLoc = end.await();

        return@runBlocking BukkitUtils.lerp(startLoc, endLoc, .3)
    }

    override fun handleInteract(
        player: Player?,
        slot: Int,
        left: Boolean
    ): Action<TargetContext<Edge>> {
        return if (left) LEFT_CLICK_EDGE else RIGHT_CLICK_EDGE
    }

    override fun equals(a: Edge, b: Edge): Boolean {
        return a.start == b.start && a.end == b.end
    }

    override fun head(element: Edge): ItemStack {
        return nodeHead.clone()
    }

    override suspend fun showElement(element: Edge, player: Player) {
        super.showElement(element, player)
        val start = element.resolveStart().await()
        val end = element.resolveEnd().await()
        val vector =
            BukkitVectorUtils.toBukkit(end.location.clone().subtract(start.location).asVector())
        val location = Location(null, 0.0, 0.0, 0.0)
        location.setDirection(vector)
        val e: ArmorStand? = entityNodeMap.inverse()[element]
        e!!.headPose = EulerAngle(location.pitch.toDouble(), location.yaw.toDouble(), 0.0)
        ps(player).announce()
    }

    override fun isSmall(element: Edge): Boolean {
        return true
    }

    override fun getName(element: Edge): Component? {
        return null
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
            entityNodeMap.values.stream()
                .filter(Predicate { edge: Edge -> ids.contains(edge.start) }).toList(),
            player.unwrap()
        )

        showElements(toRender, player.unwrap())
        players.add(player)
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
        private val ARMORSTAND_CHILD_OFFSET = Vector(0.0, -.9, 0.0)
    }
}
