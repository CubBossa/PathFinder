package de.cubbossa.pathfinder.editmode.renderer

import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderConfig.EditModeConfig
import de.cubbossa.pathfinder.PathFinderConfigImpl.EditModeConfigImpl
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.*
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import lombok.Getter
import lombok.Setter
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import xyz.xenondevs.particle.utils.ReflectionUtils
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors
import kotlin.math.pow

@Getter
@Setter
class ParticleEdgeRenderer(private val config: EditModeConfig = EditModeConfigImpl()) :
    GraphRenderer<Player> {
    private val pathFinder: PathFinder = PathFinder.get()

    private val rendered: MutableCollection<UUID> = HashSet()
    private val edges: MultiMap<UUID, UUID, ParticleEdge> = MultiMap()
    private val editModeTasks: MutableCollection<Int> = ConcurrentHashMap.newKeySet()

    override suspend fun clear(player: PathPlayer<Player>) {
        val sched = Bukkit.getScheduler()
        editModeTasks.forEach(Consumer { taskId: Int? -> sched.cancelTask(taskId!!) })
        rendered.clear()
        edges.clear()
    }

    override suspend fun renderNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        for (node in nodes) {
            edges.remove(node.nodeId)
        }
        rendered.addAll(nodes.stream().map { obj: Node -> obj.nodeId }.toList())

        // all edges from rendered nodes to adjacent nodes
        val toRender: MutableCollection<Edge> = nodes.stream()
            .map { obj: Node -> obj.edges }
            .flatMap { obj: Collection<Edge> -> obj.stream() }
            .collect(Collectors.toSet())

        val storage = PathFinder.get().storage
        val e = storage.loadEdgesTo(nodes.stream().map { obj: Node -> obj.nodeId }
            .collect(Collectors.toSet())).await()

        toRender.addAll(e.values.stream()
            .flatMap { obj: Collection<Edge> -> obj.stream() }
            .filter { edge: Edge -> rendered.contains(edge.start) }
            .toList())

        runBlocking {
            for (edge in toRender) {
                val startNodeLoader = async { edge.resolveStart().await() }
                val endNodeLoader = async { edge.resolveEnd().await() }
                val startNode: Node = startNodeLoader.await()
                val endNode: Node = endNodeLoader.await()

                val particleEdge = ParticleEdge(
                    startNode.nodeId,
                    endNode.nodeId,
                    startNode.location,
                    endNode.location,
                    true
                )
                val present = edges[edge.end, edge.start]
                if (present != null) {
                    present.start = endNode.location
                    present.end = startNode.location
                    particleEdge.directed = false
                    present.directed = false
                }
                edges.put(edge.start, edge.end, particleEdge)
            }
        }
    }

    override suspend fun eraseNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        val rendered: MutableCollection<UUID> = HashSet(this.rendered)
        clear(player)
        rendered.removeAll(nodes.stream().map { obj: Node -> obj.nodeId }.toList())
        val loadedNodes = pathFinder.storage.loadNodes(rendered).await();
        renderNodes(player, loadedNodes)
    }

    private fun updateRenderer(player: PathPlayer<Player>) {
        CompletableFuture.runAsync {
            val sched = Bukkit.getScheduler()
            HashSet(editModeTasks).forEach(Consumer { taskId: Int? -> sched.cancelTask(taskId!!) })

            val packets: MutableSet<ParticleInfo> = ConcurrentHashMap.newKeySet()
            packets.addAll(generateLocations(player))

            val packetSupplier = Function<Int, Supplier<Collection<ParticleInfo>>> { i: Int? ->
                Supplier<Collection<ParticleInfo>> {
                    val p = player.unwrap()
                    check(!(p == null || !p.isOnline)) { "Trying to render edit mode packets for offline player." }
                    val packet: MutableList<ParticleInfo> = ArrayList()
                    val distSquared: Float = config.edgeParticleRenderDistance.pow(2.0f)
                    packets.forEach(Consumer { info: ParticleInfo ->
                        if (info.location.world != p.world) {
                            return@Consumer
                        }
                        if (info.location.distanceSquared(p.location) > distSquared) {
                            return@Consumer
                        }
                        packet.add(info)
                    })
                    CollectionUtils.everyNth(packet, 2, i!!)
                }
            }
            editModeTasks.add(
                Bukkit.getScheduler()
                    .runTaskTimerAsynchronously(ReflectionUtils.getPlugin(), Runnable {
                        packets.clear()
                        packets.addAll(generateLocations(player))
                    }, config.edgeParticleTickDelay * 5L, config.edgeParticleTickDelay * 5L).taskId
            )

            editModeTasks.add(startTask(packetSupplier.apply(0), player, 0))
            editModeTasks.add(
                startTask(
                    packetSupplier.apply(1),
                    player,
                    config.edgeParticleTickDelay / 2
                )
            )
        }.exceptionally { throwable: Throwable ->
            throwable.printStackTrace()
            null
        }
    }

    private fun generateLocations(player: PathPlayer<Player>): Collection<ParticleInfo> {
        val included: MutableMap<UUID?, MutableCollection<UUID?>> = HashMap()
        val packets: MutableSet<ParticleInfo> = HashSet()
        for (edge in edges.flatValues()) {
            if (edge.start.world != edge.end.world) {
                continue
            }
            val directed: Boolean = edge.directed
            if (!directed && included.computeIfAbsent(edge.endId) { x: UUID? -> HashSet() }
                    .contains(edge.startId)) {
                continue
            }
            included.computeIfAbsent(edge.startId) { x: UUID? -> HashSet() }.add(edge.endId)

            val a: Vector = BukkitVectorUtils.toBukkit(edge.start.asVector())
            val b: Vector = BukkitVectorUtils.toBukkit(edge.end.asVector())
            val dist = a.distance(b)


            var lastLoc = a

            var i = 0f
            while (i < dist) {
                val c = if (directed
                ) LerpUtils.lerp(config.edgeParticleColorFrom, config.edgeParticleColorTo, i / dist)
                else config.edgeParticleColorFrom

                val world: World? = Bukkit.getWorld(edge.start.world.uniqueId);
                val loc = BukkitUtils.lerp(a, b, i / dist).toLocation(world!!)
                lastLoc = loc.toVector()
                // TODO conversion can be optimized
                packets.add(ParticleInfo(loc, Color.fromRGB(c.rgb and 0xffffff)))
                i += (config.edgeParticleSpacing + config.edgeParticleSpacing * 10 * lastLoc.distance(
                    player.unwrap().location.toVector()
                ) / config.edgeParticleRenderDistance).toFloat()
            }
        }
        return packets
    }

    private fun startTask(
        packets: Supplier<Collection<ParticleInfo>>,
        player: PathPlayer<Player>,
        delay: Int
    ): Int {
        return Bukkit.getScheduler()
            .runTaskTimerAsynchronously(ReflectionUtils.getPlugin(), Runnable {
                val p = player.unwrap()
                for ((location, color) in packets.get()) {
                    p.spawnParticle(
                        (SpigotConversionUtil.toBukkitParticle(ParticleTypes.DUST) as Particle),
                        location,
                        1,
                        Particle.DustOptions(color, 1f)
                    )
                }
            }, delay.toLong(), config.edgeParticleTickDelay.toLong()).taskId
    }

    @JvmRecord
    private data class ParticleInfo(val location: Location, val color: Color)

    private class ParticleEdge(
        val startId: UUID,
        val endId: UUID,
        var start: de.cubbossa.pathfinder.misc.Location,
        var end: de.cubbossa.pathfinder.misc.Location,
        var directed: Boolean = false
    )
}
