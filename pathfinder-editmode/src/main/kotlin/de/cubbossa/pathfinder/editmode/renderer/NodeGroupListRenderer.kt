package de.cubbossa.pathfinder.editmode.renderer

import de.cubbossa.cliententities.PlayerSpace
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.event.NodeGroupSaveEvent
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import lombok.Getter
import lombok.Setter
import org.bukkit.Bukkit
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.math.cos
import kotlin.math.pow

class NodeGroupListRenderer(
    private val plugin: Plugin,
    angle: Double,
    distance: Double
) : Listener, GraphRenderer<Player> {

    @Getter
    @Setter
    private class Context(player: UUID?) {
        val playerSpace: PlayerSpace = PlayerSpace.create().withPlayer(player).build()
        val rendered: MutableCollection<Node> = HashSet()
        val displayed: MutableMap<UUID, NodeContext>

        init {
            displayed = ConcurrentHashMap()
        }

        @JvmRecord
        data class NodeContext(val node: Node, val display: TextDisplay)
    }

    private val contextMap: MutableMap<UUID, Context> = HashMap()

    private var hasHeldGroupToolsBefore = false
    private val cooldown: Long = 100
    private var lastCheck: Long = 0
    private val animationTickDuration = 4

    private val angleDot = cos(angle * Math.PI / 180)
    private val distanceSquared: Double

    private val groupChangeListener: de.cubbossa.pathfinder.event.Listener<*>

    init {
        distanceSquared = distance.pow(2.0)

        groupChangeListener =
            PathFinder.get().eventDispatcher.listen(NodeGroupSaveEvent::class.java) { e: NodeGroupSaveEvent? ->
                CompletableFuture.runAsync {
                    contextMap.forEach { (uuid: UUID, context: Context) ->
                        val player = Bukkit.getPlayer(uuid) ?: return@forEach
                        context.displayed.values.forEach(Consumer { nodeContext: Context.NodeContext ->
                            showText(nodeContext.node, player)
                        })
                    }
                }
            }

        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun dispose() {
        contextMap.values.forEach(Consumer { context: Context ->
            try {
                context.playerSpace.close()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        })
        PathFinder.get().eventDispatcher.drop(groupChangeListener)
        PlayerMoveEvent.getHandlerList().unregister(this)
    }

    private fun context(player: Player): Context {
        return contextMap.computeIfAbsent(player.uniqueId) { player: UUID? -> Context(player) }
    }

    /**
     * Lets check the following:
     * - if the cooldown of x ms is over.
     * - if the player holds group tools
     * - filter nodes that are not displayed
     * - if any of the rendered nodes matches the location criteria
     * - if the node has any groups
     * if all fulfilled ->
     */
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val now = System.currentTimeMillis()
        // lets do performance a favor
        if (now - lastCheck < cooldown) {
            return
        }
        lastCheck = now

        val player = event.player
        if (!holdsGroupTools(player)) {
            // Hide all currently visible texts.
            if (hasHeldGroupToolsBefore) {
                HashMap(context(player).displayed).forEach { (k: UUID?, v: Context.NodeContext) ->
                    hideText(
                        v.node,
                        player
                    )
                }
            }
            return
        }
        hasHeldGroupToolsBefore = true
        CompletableFuture.runAsync {
            for (node in context(player).rendered) {
                evaluate(node, player)
            }
        }
    }

    private fun evaluate(node: Node, player: Player) {
        val nodeLoc = BukkitVectorUtils.toBukkit(node.location)
        if (nodeLoc.world != player.location.world) {
            hideText(node, player)
            return
        }
        if (nodeLoc.distanceSquared(player.location) > distanceSquared) {
            hideText(node, player)
            return
        }

        // dot product divided by length = angle. positive -> viewing in same direction, the smaller the closer
        val view = player.location.direction.normalize()
        val toNode = nodeLoc.clone().subtract(player.eyeLocation.toVector()).toVector().normalize()
        val dot = view.dot(toNode)
        if (dot / (view.length() * toNode.length()) < angleDot) {
            hideText(node, player)
            return
        }
        showText(node, player)
    }

    private fun showText(node: Node, player: Player): CompletableFuture<Void?> {
        val ctx = context(player)

        if (ctx.displayed.containsKey(node.nodeId)) {
            return CompletableFuture.completedFuture(null)
        }

        return PathFinder.get().storage.loadGroupsOfNodes(setOf(node))
            .thenAccept { nodeCollectionMap: Map<Node?, Collection<NodeGroup>> ->
                val groups = nodeCollectionMap[node]!!
                if (groups.size == 0 || groups.size == 1 && groups.stream().findAny()
                        .get().key == NamespacedKey.fromString("pathfinder:global")
                ) {
                    return@thenAccept
                }

                val location = BukkitVectorUtils.toBukkit(node.location).add(0.0, 0.3, 0.0)
                location.setDirection(
                    player.location.clone().subtract(location).toVector().multiply(Vector(1, 0, 1))
                )
                val display: TextDisplay = ctx.playerSpace.spawn(location, TextDisplay::class.java)

                val nodeCtx = Context.NodeContext(node, display)
                ctx.displayed[node.nodeId] = nodeCtx

                setText(nodeCtx, groups)
                display.billboard = Display.Billboard.VERTICAL
                ctx.playerSpace.announce()
            }
    }

    private fun setText(
        context: Context.NodeContext,
        groups: Collection<NodeGroup>
    ): CompletableFuture<Void?> {
//    Component component = Component.join(
//        JoinConfiguration.newlines(),
//        StorageUtil.getGroups(node).stream()
//            .map(NodeGroup::getKey)
//            .map(NamespacedKey::toString)
//            .map(Component::text)
//            .toArray(Component[]::new)
//    );
//    display.setText(serializer.serialize(component));

        val str = groups.stream()
            .map { obj: NodeGroup -> obj.key }.map { obj: NamespacedKey -> obj.key }
            .filter { s: String -> s != "global" }
            .collect(Collectors.joining(", "))
        context.display.text = str
        return CompletableFuture.completedFuture(null)
    }

    private fun hideText(node: Node, player: Player) {
        val ctx = context(player)
        val nodeCtx = ctx.displayed.remove(node.nodeId) ?: return

        //    display.setInterpolationDelay(-1);
//    display.setTransformation(new Transformation(
//        new Vector3f(), new Quaternionf(), new Vector3f(0, 0, 0), new Quaternionf()
//    ));
//    Bukkit.getScheduler().runTaskLater(plugin, () -> {
//      if (!displayed.getOrDefault(player.getUniqueId(), Collections.emptyMap()).containsKey(node)) {
//      }
//    }, animationTickDuration);
        nodeCtx.display.remove()
        ctx.playerSpace.announce()
    }

    private fun holdsGroupTools(player: Player): Boolean {
        val slot = player.inventory.heldItemSlot
        return slot == 1 || slot == 2
    }

    override suspend fun clear(player: PathPlayer<Player>) {
        val ctx = context(player.unwrap())
        ctx.rendered.clear()
        for ((node) in ctx.displayed.values) {
            hideText(node, player.unwrap())
        }
        ctx.displayed.clear()
    }

    override suspend fun renderNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        val ctx = context(player.unwrap())
        ctx.rendered.addAll(nodes)
        val p = player.unwrap()
        for (node in nodes) {
            evaluate(node, p)
        }
    }

    override suspend fun eraseNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        val ctx = context(player.unwrap())
        ctx.rendered.removeAll(nodes)
        for (node in nodes) {
            hideText(node, player.unwrap())
        }
    }
}
