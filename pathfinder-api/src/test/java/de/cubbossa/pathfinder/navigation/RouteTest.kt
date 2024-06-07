package de.cubbossa.pathfinder.navigation

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder
import de.cubbossa.pathfinder.Changes
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException
import de.cubbossa.pathfinder.graph.GraphEntrySolver
import de.cubbossa.pathfinder.graph.NoPathFoundException
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.misc.World
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RouteTest {

    private val world: World = object : World {
        override var uniqueId: UUID = UUID.randomUUID()

        override val name: String
            get() = uniqueId.toString()
    }

    @BeforeAll
    fun beforeAll() {
        NavigationLocationImpl.GRAPH_ENTRY_SOLVER = object : GraphEntrySolver<Node> {
            @Throws(GraphEntryNotEstablishedException::class)
            override fun solveEntry(
                `in`: Node,
                scope: MutableValueGraph<Node, Double>
            ): MutableValueGraph<Node, Double> {
                return solveBoth(`in`, scope)
            }

            @Throws(GraphEntryNotEstablishedException::class)
            override fun solveExit(
                out: Node,
                scope: MutableValueGraph<Node, Double>
            ): MutableValueGraph<Node, Double> {
                return solveBoth(out, scope)
            }

            @Throws(GraphEntryNotEstablishedException::class)
            private fun solveBoth(
                node: Node,
                scope: MutableValueGraph<Node, Double>
            ): MutableValueGraph<Node, Double> {
                val nearest = scope.nodes().stream()
                    .filter { n: Node -> n.nodeId != node.nodeId }
                    .min(Comparator.comparingDouble { o: Node -> o.location.distance(node.location) })
                    .orElse(null)
                if (!scope.nodes().contains(node)) {
                    scope.addNode(node)
                }
                val d = node.location.distance(nearest.location)
                scope.putEdgeValue(nearest, node, d)
                scope.putEdgeValue(node, nearest, d)
                return scope
            }
        }
    }

    @Test
    @Throws(NoPathFoundException::class)
    fun testA() {
        val a: Node = TestNode(UUID.randomUUID(), Location(0.0, 0.0, 0.0, world))
        val b: Node = TestNode(UUID.randomUUID(), Location(10.0, 0.0, 0.0, world))
        val graph = ValueGraphBuilder.directed().build<Node, Double>()
        graph.addNode(a)
        graph.addNode(b)
        graph.putEdgeValue(a, b, 10.0)
        graph.putEdgeValue(b, a, 10.0)
        var result = Route.from(a).to(b).calculatePath(graph)
        assertEquals(listOf(a, b), result.getPath())
        assertEquals(10.0, result.getCost())

        result = Route.from(a).to(b).to(a).to(b).calculatePath(graph)
        assertEquals(listOf(a, b, a, b), result.path)
        assertEquals(30.0, result.cost)

        result = Route.from(a).to(a).to(a).calculatePath(graph)
        assertEquals(listOf(a), result.path)
    }

    @Test
    @Throws(NoPathFoundException::class)
    fun testB() {
        val a: Node = TestNode(UUID.randomUUID(), Location(0.0, 0.0, 0.0, world))
        val b: Node = TestNode(UUID.randomUUID(), Location(10.0, 0.0, 0.0, world))
        val c: Node = TestNode(UUID.randomUUID(), Location(20.0, 0.0, 0.0, world))
        val graph = ValueGraphBuilder.directed().build<Node, Double>()
        graph.addNode(a)
        graph.addNode(b)
        graph.addNode(c)
        graph.putEdgeValue(a, b, 10.0)
        graph.putEdgeValue(b, a, 10.0)
        graph.putEdgeValue(c, b, 10.0)
        graph.putEdgeValue(b, c, 10.0)
        graph.putEdgeValue(a, c, 20.0)
        graph.putEdgeValue(c, a, 20.0)

        var result = Route
            .from(a)
            .to(Route.from(b).to(a).to(b))
            .to(c)
            .calculatePath(graph)
        assertEquals(40.0, result.cost)
        assertEquals(listOf(a, b, a, b, c), result.path)

        result = Route
            .from(a)
            .to(c)
            .to(Route.from(b).to(a).to(b))
            .calculatePath(graph)
        assertEquals(50.0, result.cost)
        assertEquals(listOf(a, c, b, a, b), result.path)

        result = Route
            .from(a)
            .to(Route.from(a).to(b).to(a))
            .to(a)
            .calculatePath(graph)
        assertEquals(20.0, result.cost)
        assertEquals(listOf(a, b, a), result.path)
    }

    @Test
    @Throws(NoPathFoundException::class)
    fun testC() {
        val a: Node = TestNode(UUID.randomUUID(), Location(-10.0, 0.0, 0.0, world))
        val b: Node = TestNode(UUID.randomUUID(), Location(0.0, 0.0, 0.0, world))
        val c: Node = TestNode(UUID.randomUUID(), Location(10.0, 0.0, 0.0, world))
        val d: Node = TestNode(UUID.randomUUID(), Location(20.0, 0.0, 0.0, world))
        val graph = ValueGraphBuilder.directed().build<Node, Double>()
        graph.addNode(b)
        graph.addNode(c)
        graph.putEdgeValue(c, b, 10.0)
        graph.putEdgeValue(b, c, 10.0)

        val result = Route
            .from(NavigationLocation.fixedExternalNode(a))
            .to(NavigationLocation.fixedExternalNode(d))
            .calculatePath(graph)

        assertEquals(30.0, result.getCost())
        assertEquals(listOf(a, b, c, d), result.getPath())
    }

    private inner class TestNode(
        override val nodeId: UUID,
        override var location: Location
    ) : Node {

        override val edgeChanges: Changes<Edge>
            get() = Changes()

        override val edges: Collection<Edge>
            get() = emptyList()

        override fun connect(other: UUID, weight: Double): Edge? {
            return null
        }

        override fun clone(): Node {
            return TestNode(nodeId, location)
        }

        override fun clone(id: UUID): Node {
            return TestNode(id, location)
        }

        override fun toString(): String {
            return nodeId.toString().substring(0, 8)
        }
    }
}