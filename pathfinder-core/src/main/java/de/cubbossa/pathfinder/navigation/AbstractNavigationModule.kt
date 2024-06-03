package de.cubbossa.pathfinder.navigation

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import de.cubbossa.pathfinder.AbstractPathFinder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderExtension
import de.cubbossa.pathfinder.PathFinderExtensionBase
import de.cubbossa.pathfinder.event.EventCancelledException
import de.cubbossa.pathfinder.event.EventDispatcher
import de.cubbossa.pathfinder.graph.NoPathFoundException
import de.cubbossa.pathfinder.group.FindDistanceModifier
import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.group.PermissionModifier
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.misc.pathPlayer
import de.cubbossa.pathfinder.node.GroupedNode
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver
import de.cubbossa.pathfinder.util.ExtensionPoint
import de.cubbossa.pathfinder.visualizer.VisualizerPath
import kotlinx.coroutines.runBlocking
import java.lang.Double.doubleToLongBits
import java.util.*
import java.util.concurrent.CompletionException
import java.util.function.Consumer

open class AbstractNavigationModule<PlayerT>
    : PathFinderExtensionBase(), PathFinderExtension, NavigationModule<PlayerT> {

    override val key: NamespacedKey = AbstractPathFinder.pathfinder("navigation")

    protected val pathFinder: PathFinder

    @JvmField
    protected var eventDispatcher: EventDispatcher<PlayerT>? = null

    private var navigators: Cache<UUID, Navigator>

    @JvmField
    protected val activePaths: MutableMap<UUID, NavigationContext?>
    private val navigationConstraints: MutableList<NavigationConstraint>

    init {
        NavigationModuleProvider.set(this)

        this.activePaths = HashMap()
        this.pathFinder = PathFinder.get()
        pathFinder.disposer.register(this.pathFinder, this)
        this.navigationConstraints = ArrayList()

        navigators = Caffeine.newBuilder()
            .maximumSize(128)
            .build()

        val extensionPoint = ExtensionPoint(
            NavigationConstraint::class.java
        )
        extensionPoint.extensions.forEach(Consumer { filter: NavigationConstraint ->
            this.registerNavigationConstraint(
                filter
            )
        })

        NavigationLocationImpl.GRAPH_ENTRY_SOLVER = EdgeBasedGraphEntrySolver()
    }

    override fun onLoad(pathPlugin: PathFinder) {
        if (!pathFinder.configuration.moduleConfig.isNavigationModule) {
            disable()
        }
        this.eventDispatcher = pathFinder.eventDispatcher as EventDispatcher<PlayerT>
    }

    override fun onEnable(pathPlugin: PathFinder) {
        registerNavigationConstraint { playerId: UUID, scope: Collection<Node> ->
            runBlocking {
                val player: PathPlayer<*> = pathPlayer<Any>(playerId)
                val groups: Map<Node, Collection<NodeGroup>> =
                    PathFinder.get().storage.loadGroupsOfNodes(scope)

                if (player.unwrap() == null) {
                    return@runBlocking HashSet<Node>()
                }
                groups.entries.stream()
                    .filter { e ->
                        e.value.stream().allMatch { group: NodeGroup ->
                            val mod = group.getModifier<PermissionModifier>(PermissionModifier.KEY)
                            mod.isEmpty || player.hasPermission(mod.get().permission())
                        }
                    }
                    .map { e -> e.key }
                    .toList()
            }
        }
    }

    override fun dispose() {
        NavigationModuleProvider.set(null)
    }

    override suspend fun navigate(
        viewer: PathPlayer<PlayerT>,
        route: Route
    ): VisualizerPath<PlayerT>? {

        val current = activePaths[viewer.uniqueId]
        if (current != null) {
            unset(current)
        }

        val path: VisualizerPath<PlayerT>
        try {
            path = navigators[viewer.uniqueId, { uuid: UUID ->
                NavigatorImpl { c: Collection<Node> ->
                    var nodes = c
                    for (navigationConstraint in navigationConstraints) {
                        nodes = navigationConstraint.filterTargetNodes(uuid, nodes)
                    }
                    nodes
                }
            }].createRenderer(viewer, route)
        } catch (noPathFoundException: NoPathFoundException) {
            throw CompletionException(noPathFoundException)
        }
        val result = eventDispatcher!!.dispatchPathStart(viewer, path)
        if (!result) {
            throw EventCancelledException()
        }

        activePaths[viewer.uniqueId] = context(viewer.uniqueId, path)
        return path
    }

    private fun context(playerId: UUID, path: VisualizerPath<PlayerT>): NavigationContext {
        val last = path.path[path.path.size - 1]
            ?: throw IllegalStateException("Path containing no nodes")

        var dist = 1.5

        if (last is GroupedNode) {
            val highest: NodeGroup = last.groups().stream()
                .filter { g: NodeGroup -> g.hasModifier<Modifier>(FindDistanceModifier.KEY) }
                .max { obj: NodeGroup, o: NodeGroup -> obj.compareTo(o) }
                .orElse(null)

            dist = highest.getModifier<FindDistanceModifier>(FindDistanceModifier.KEY)
                .map { obj: FindDistanceModifier -> obj.distance() }
                .orElse(1.5)
        }
        return NavigationContext(playerId, path, last, dist)
    }

    private fun unset(context: NavigationContext) {
        if (activePaths.remove(context.playerId) != null) {
            eventDispatcher!!.dispatchPathStopped(pathPlayer(context.playerId), context.path)
            PathFinder.get().disposer.dispose(context.path)
        }
        context.path.removeViewer(pathPlayer(context.playerId))
    }

    override fun unset(viewer: UUID) {
        val context = activePaths[viewer]
        if (context != null) {
            unset(context)
        }
    }

    override fun cancel(viewer: UUID) {
        val context = activePaths[viewer] ?: return
        val path = context.path()
        if (!eventDispatcher!!.dispatchPathCancel(path.targetViewer, path)) {
            return
        }
        unset(context)
    }

    override fun reach(viewer: UUID) {
        val context = activePaths[viewer] ?: return
        val path = context.path()
        if (!eventDispatcher!!.dispatchPathTargetReached(path.targetViewer, path)) {
            return
        }
        unset(context)
    }

    override fun registerNavigationConstraint(filter: NavigationConstraint) {
        navigationConstraints.add(filter)
    }

    override fun canNavigateTo(uuid: UUID, node: Node, scope: Collection<Node>): Boolean {
        return applyNavigationConstraints(uuid, scope).contains(node)
    }

    override fun applyNavigationConstraints(
        player: UUID,
        nodes: Collection<Node>
    ): Collection<Node> {
        var nodeSet: Collection<Node> = HashSet(nodes)
        for (f in navigationConstraints) {
            nodeSet = f.filterTargetNodes(player, nodeSet)
        }
        return nodeSet
    }

    override fun getActivePath(player: PathPlayer<PlayerT>): VisualizerPath<PlayerT>? {
        return activePaths[player.uniqueId]?.path
    }

    override fun cancelPathWhenTargetReached(path: VisualizerPath<PlayerT>) {
    }

    protected inner class NavigationContext(
        val playerId: UUID,
        val path: VisualizerPath<PlayerT>,
        private val target: Node,
        private val dist: Double
    ) {
        fun playerId(): UUID {
            return playerId
        }

        fun path(): VisualizerPath<PlayerT> {
            return path
        }

        fun target(): Node {
            return target
        }

        fun dist(): Double {
            return dist
        }

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }
            if (other == null || other.javaClass != this.javaClass) {
                return false
            }
            val that = other as AbstractNavigationModule<*>.NavigationContext
            return this.playerId == that.playerId && (this.path == that.path) && (this.target == that.target)
                    && (doubleToLongBits(this.dist) == doubleToLongBits(that.dist))
        }

        override fun hashCode(): Int {
            return Objects.hash(playerId, path, target, dist)
        }

        override fun toString(): String {
            return "NavigationContext[" +
                    "playerId=" + playerId + ", " +
                    "path=" + path + ", " +
                    "target=" + target + ", " +
                    "dist=" + dist + ']'
        }
    }
}