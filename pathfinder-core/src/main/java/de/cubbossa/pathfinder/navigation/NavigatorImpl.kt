package de.cubbossa.pathfinder.navigation

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraph
import com.google.common.graph.ValueGraphBuilder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.event.*
import de.cubbossa.pathfinder.graph.NoPathFoundException
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.GroupedNode
import de.cubbossa.pathfinder.node.GroupedNodeImpl
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.visualizer.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.logging.Level

class NavigatorImpl @JvmOverloads constructor(
    private val constraint: (Collection<Node>) -> Collection<Node> = { o -> o }
) : Navigator {

    private val pathFinder: PathFinder = PathFinder.get()

    // Caches
    private var generatingFuture: Deferred<MutableValueGraph<Node, Double>>? = null
    private var cachedGraph: MutableValueGraph<Node, Double>? = null

    init {
        val eventDispatcher = pathFinder.eventDispatcher
        pathFinder.disposer.register(pathFinder, this)

        eventDispatcher.listen(NodeCreateEvent::class.java) { e: NodeCreateEvent? ->
            cachedGraph = null
        }
        eventDispatcher.listen(NodeGroupDeleteEvent::class.java) { e: NodeGroupDeleteEvent? ->
            cachedGraph = null
        }
        eventDispatcher.listen(NodeSaveEvent::class.java) { e: NodeSaveEvent? ->
            cachedGraph = null
        }
        eventDispatcher.listen(NodeGroupSaveEvent::class.java) { e: NodeGroupSaveEvent? ->
            cachedGraph = null
        }
        eventDispatcher.listen(NodeDeleteEvent::class.java) { e: NodeDeleteEvent? ->
            cachedGraph = null
        }
        eventDispatcher.listen(NodeGroupDeleteEvent::class.java) { e: NodeGroupDeleteEvent? ->
            cachedGraph = null
        }
    }

    @Throws(NoPathFoundException::class)
    override fun createPath(route: Route): List<Node> {
        try {
            val graph: ValueGraph<Node, Double> = fetchGraph()
            val path = route.calculatePath(graph).path
            return removeIdenticalNeighbours(path)
        } catch (e: ExecutionException) {
            if (e.cause is NoPathFoundException) {
                throw e
            }
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    private fun removeIdenticalNeighbours(path: List<Node>): List<Node> {
        val result: MutableList<Node> = ArrayList()
        var last: GroupedNode? = null
        for (node in path) {
            if (node !is GroupedNode) {
                last = null
                result.add(node)
                continue
            }
            if (last != null && last.node().location == node.node().location) {
                val n = last.clone() as GroupedNode
                n.groups().addAll(node.groups())
                result.removeAt(result.size - 1)
                result.add(n)
            } else {
                result.add(node)
            }
            last = node
        }
        return path
    }

    @Throws(NoPathFoundException::class)
    override fun <PlayerT> createRenderer(
        viewer: PathPlayer<PlayerT>, route: Route
    ): VisualizerPath<PlayerT> {
        val path: VisualizerPath<PlayerT> = GroupedVisualizerPathImpl(viewer, UpdatingPath {
            try {
                return@UpdatingPath createPath(route)
            } catch (ignored: NoPathFoundException) {
                return@UpdatingPath ArrayList<Node>()
            }
        })
        path.addViewer(viewer)

        // load config value
        path.startUpdater(0)
        return path
    }

    @Throws(NoPathFoundException::class)
    override fun <PlayerT, ViewT : PathView<PlayerT>> createRenderer(
        viewer: PathPlayer<PlayerT>, route: Route, renderer: PathVisualizer<ViewT, PlayerT>
    ): VisualizerPath<PlayerT> {
        return SingleVisualizerPathImpl(UpdatingPath {
            try {
                return@UpdatingPath createPath(route)
            } catch (ignored: NoPathFoundException) {
                return@UpdatingPath ArrayList<Node>()
            }
        }, renderer, viewer)
    }

    /**
     * Returns the (potentially completed) graph creation process or starts a new one if none exists.
     */
    private fun fetchGraph(): MutableValueGraph<Node, Double> {
        if (cachedGraph != null) {
            return cachedGraph!!
        }
        if (generatingFuture == null) {
            generatingFuture = createGraph()
        }
        return runBlocking {
            cachedGraph = generatingFuture?.await()
            generatingFuture = null
            return@runBlocking cachedGraph!!
        }
    }

    /**
     * Generates the current world into one graph representation
     */
    private fun createGraph(): Deferred<MutableValueGraph<Node, Double>> = runBlocking {
        async {
            val nodes = constraint(pathFinder.storage.loadNodes())
            val nodeMap: MutableMap<UUID, Node> = HashMap()

            nodes.forEach { node -> nodeMap[node.nodeId] = node }

            val map: MutableMap<UUID, GroupedNode> = HashMap()

            val groups = pathFinder.storage.loadGroupsOfNodes(nodeMap.values)
            groups.forEach { (node, gs) ->
                map[node.nodeId] = GroupedNodeImpl(node, gs)
            }
            val graph = ValueGraphBuilder
                .directed().allowsSelfLoops(false)
                .build<Node, Double>()

            map.values.forEach { graph.addNode(it) }
            for (entry in map.entries) {
                val node: Node = entry.value.node()
                for (e in node.edges) {
                    val endGrouped = map[e.end]
                    val end = endGrouped?.node()
                    val startGrouped = map[e.start]
                    val start = startGrouped?.node()
                    if (end == null || start == null) {
                        pathFinder.logger.log(
                            Level.WARNING,
                            """Could not resolve edge while creating graph: $e. Apparently, not all nodes are part of the global group."""
                        )
                        continue
                    }
                    graph.putEdgeValue(
                        startGrouped,
                        endGrouped,
                        node.location.distance(end.location) * e.weight
                    )
                }
            }
            graph
        }
    }
}
