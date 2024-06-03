package de.cubbossa.pathfinder.navigation

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraph
import com.google.common.graph.ValueGraphBuilder
import de.cubbossa.pathfinder.graph.*
import de.cubbossa.pathfinder.node.GroupedNode
import de.cubbossa.pathfinder.node.Node
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

internal class RouteImpl : Route {
    private val targets: MutableList<Collection<Any>>
    private var baseGraphSolver: PathSolver<Node, Double>

    // caching
    private var modifiedBaseGraph: ValueGraph<Node, Double>? = null

    constructor(other: Route) {
        this.targets = ArrayList()
        targets.add(java.util.List.of<Any>(other))
        this.baseGraphSolver = DynamicDijkstra(Function.identity())
    }

    constructor(start: NavigationLocation) {
        this.targets = ArrayList()
        targets.add(java.util.List.of<Any>(start))
        this.baseGraphSolver = DynamicDijkstra(Function.identity())
    }

    private fun loc(node: Node): NavigationLocation {
        if (node is NavigationLocation) {
            return node
        }
        return NavigationLocation.fixedGraphNode(node)
    }

    override fun withSolver(solver: PathSolver<Node, Double>): Route {
        this.baseGraphSolver = solver
        return this
    }

    override val start: NavigationLocation
        get() {
            val first = targets[0].stream().findAny().orElse(null)
            if (first is NavigationLocation) {
                return first
            }
            throw IllegalStateException("Invalid first node: $first")
        }

    override val end: Collection<NavigationLocation>
        get() = targets[targets.size - 1].stream()
            .filter { it is NavigationLocation }
            .map { it as NavigationLocation }
            .toList()

    override fun to(route: Route): Route {
        targets.add(listOf(route))
        return this
    }

    override fun to(nodes: List<Node>): Route {
        val edges: MutableList<Double> = LinkedList()
        var cost = 0.0
        var prev: Node? = null
        for (node in nodes) {
            if (prev == null) {
                prev = node
                continue
            }
            val edge = node.location.distance(prev.location)
            edges.add(edge)
            cost += edge
            prev = node
        }

        val finalCost = cost
        targets.add(listOf<Any>(object : RouteEl(nodes[0], nodes[nodes.size - 1]) {
            override fun solve(): PathSolverResult<Node, Double> {
                return PathSolverResultImpl(nodes, edges, finalCost)
            }
        }))
        return this
    }

    override fun to(node: Node): Route {
        return to(loc(node))
    }

    override fun to(location: NavigationLocation): Route {
        targets.add(listOf<Any>(location))
        return this
    }

    override fun toAny(vararg nodes: Node): Route {
        targets.add(Arrays.stream(nodes).map { loc(it) }
            .collect(Collectors.toList()))
        return this
    }

    override fun toAny(nodes: Collection<Node>): Route {
        targets.add(nodes.stream().map { loc(it) }
            .collect(Collectors.toList()))
        return this
    }

    override fun toAny(vararg locations: NavigationLocation): Route {
        targets.add(Arrays.stream(locations).collect(Collectors.toList()))
        return this
    }

    override fun toAny(vararg routes: Route): Route {
        targets.add(Arrays.stream(routes).collect(Collectors.toList()))
        return this
    }

    @Throws(NoPathFoundException::class)
    override fun calculatePath(environment: ValueGraph<Node, Double>): PathSolverResult<Node, Double> {
        val res = calculatePaths(environment)
        if (res.isEmpty()) {
            throw NoPathFoundException()
        }
        return res.iterator().next()
    }

    @Throws(NoPathFoundException::class)
    override fun calculatePaths(environment: ValueGraph<Node, Double>): List<PathSolverResult<Node, Double>> {
        modifiedBaseGraph = prepareBaseGraph(environment)
        baseGraphSolver.setGraph(modifiedBaseGraph)

        val abstractGraph = ValueGraphBuilder
            .directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(targets.stream().mapToInt { it.size }
                .sum())
            .build<RouteEl, PathSolverResult<Node, Double>>()

        val convertedTargets: MutableList<Collection<RouteEl>> = ArrayList()
        for (target in targets) {
            val inner: MutableCollection<RouteEl> = HashSet()
            for (o in target) {
                inner.addAll(newElement(o, environment))
            }
            convertedTargets.add(inner)
        }

        var prev: Collection<RouteEl> = HashSet()
        for (target in convertedTargets) {
            for (inner in target) {
                abstractGraph.addNode(inner)

                for (p in prev) {
                    // Only make edge in abstract graph if it can actually be used. Otherwise skip
                    try {
                        val solved = solveForSection(p, inner)
                        abstractGraph.putEdgeValue(p, inner, solved)
                    } catch (_: NoPathFoundException) {
                    }
                }
                prev = target
            }
        }

        val abstractSolver =
            DynamicDijkstra<RouteEl, PathSolverResult<Node, Double>> { obj: PathSolverResult<Node, Double> -> obj.cost }
        abstractSolver.setGraph(abstractGraph)

        val results: MutableList<PathSolverResult<Node, Double>> = ArrayList()
        val start = convertedTargets[0].iterator().next()
        for (lastTarget in convertedTargets[convertedTargets.size - 1]) {
            try {
                val res = abstractSolver.solvePath(
                    start,
                    lastTarget
                )
                results.add(join(res))
            } catch (_: Throwable) {
            }
        }
        if (results.isEmpty()) {
            throw NoPathFoundException()
        }
        results.sortedWith(Comparator.comparingDouble { obj: PathSolverResult<Node, Double> -> obj.cost })
        return results
    }

    private fun merge(iterable: Iterable<PathSolverResult<Node, Double>>): PathSolverResult<Node, Double> {
        var first = true

        val nodePath: MutableList<Node> = ArrayList()
        val edges: MutableList<Double> = ArrayList()
        var cost = 0.0
        for (result in iterable) {
            if (!first && result.path.isNotEmpty()) {
                nodePath.addAll(result.path.subList(1, result.path.size))
            } else {
                nodePath.addAll(result.path)
                first = false
            }
            edges.addAll(result.edges)
            cost += result.cost
        }
        return PathSolverResultImpl(nodePath, edges, cost)
    }

    @Throws(NoPathFoundException::class)
    private fun join(els: PathSolverResult<RouteEl, PathSolverResult<Node, Double>>): PathSolverResult<Node, Double> {
        val results: MutableList<PathSolverResult<Node, Double>> = LinkedList()
        val nit: Iterator<RouteEl> = els.path.iterator()
        val eit: Iterator<PathSolverResult<Node, Double>> = els.edges.iterator()
        check(nit.hasNext())
        var el: RouteEl
        while (nit.hasNext()) {
            el = nit.next()
            results.add(el.solve())
            if (eit.hasNext()) {
                results.add(eit.next())
            }
        }
        return merge(results)
    }

    @Throws(NoPathFoundException::class)
    private fun solveForSection(a: RouteEl, b: RouteEl): PathSolverResult<Node, Double> {
        val start = if (a.end !is GroupedNode) {
            modifiedBaseGraph!!.nodes().stream()
                .filter { node: Node -> node.nodeId == a.end.nodeId }
                .findAny().get()
        } else {
            a.end
        }
        val end = if (b.start !is GroupedNode) {
            modifiedBaseGraph!!.nodes().stream()
                .filter { node: Node -> node.nodeId == b.start.nodeId }
                .findAny().get()
        } else {
            b.start
        }
        return baseGraphSolver.solvePath(start, end)
    }

    @Throws(NoPathFoundException::class)
    private fun newElement(o: Any, environment: ValueGraph<Node, Double>): Collection<RouteEl> {
        if (o is RouteEl) {
            return listOf(o)
        } else if (o is NavigationLocation) {
            val n: Node = o.node
            return setOf<RouteEl>(object : RouteEl(n, n) {
                override fun solve(): PathSolverResult<Node, Double> {
                    return PathSolverResultImpl(listOf(n), emptyList(), 0.0)
                }
            })
        } else if (o is Route) {
            val els: MutableCollection<RouteEl> = LinkedList()
            for (result in o.calculatePaths(environment)) {
                els.add(object : RouteEl(result.path[0], result.path[result.path.size - 1]) {
                    override fun solve(): PathSolverResult<Node, Double> {
                        return result
                    }
                })
            }
            return els
        }
        throw IllegalStateException("Don't know how to convert object into RouteEl")
    }

    private fun prepareBaseGraph(graph: ValueGraph<Node, Double>): ValueGraph<Node, Double> {
        var g: MutableValueGraph<Node, Double>
        if (graph is MutableValueGraph<Node, Double>) {
            g = graph
        } else {
            g = ValueGraphBuilder.from(graph).build()
            graph.nodes().forEach(Consumer { node: Node -> g.addNode(node) })
            for (e in graph.edges()) {
                g.putEdgeValue(e.nodeU(), e.nodeV(), g.edgeValue(e.nodeU(), e.nodeV()).orElse(0.0))
            }
        }

        for (target in targets) {
            for (o in target) {
                if (o is NavigationLocation) {
                    g = o.connect(g)
                }
            }
        }
        return g
    }

    private abstract class RouteEl(val start: Node, val end: Node) {
        abstract fun solve(): PathSolverResult<Node, Double>

        fun start(): Node {
            return start
        }

        fun end(): Node {
            return end
        }

        override fun toString(): String {
            return "RouteEl[start=${start}, end=${end}]"
        }
    }
}
