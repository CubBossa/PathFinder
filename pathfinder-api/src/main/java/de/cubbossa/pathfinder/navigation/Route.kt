package de.cubbossa.pathfinder.navigation

import com.google.common.graph.ValueGraph
import de.cubbossa.pathfinder.graph.NoPathFoundException
import de.cubbossa.pathfinder.graph.PathSolver
import de.cubbossa.pathfinder.graph.PathSolverResult
import de.cubbossa.pathfinder.node.Node

/**
 * Paths across graphs can be described with Routes. A Route is a chain of targets to visit.
 * The first element must be a single element, but every other target can be a list of targets, of which the most
 * optimal will be picked.
 * A route "a -> (b or c) -> (d or e)" will choose between b or c and accordingly d or e so that the calculated path
 * is the shortest among all possible paths.
 * <br></br><br></br>
 * Routes can embed other Routes. The embedded Route will be resolved first and then treated as edge on the meta graph.
 * if r is a Route and "a -> (r or b) -> c" is its parent Route, r will be used if the path along r is shorter than
 * visiting b.
 * <br></br><br></br>
 * Routes will automatically be shortened.
 * "a -> a -> a" will result in a Node path "a".
 * if "r = a -> b", "a -> r -> c" will result in "a -> b -> c", not in "a -> a -> b -> c".
 */
interface Route {
    /**
     * The entry point of this Route. This is a single point. If you do need to find the shortest path among multiple
     * entry points, use one entry point that is connected to all actual entry points with an edge weight of one.
     * After solving, drop the first Node.
     * @return The entry point of the route.
     */
    val start: NavigationLocation

    /**
     * All possible target locations of this Route.
     * @return An immutable copy of all target locations of this Route.
     */
    val end: Collection<NavigationLocation>

    /**
     * Provide a solver for the path segments.
     * The route will need to find the shortest path from one target to another and by default uses a [de.cubbossa.pathfinder.graph.DynamicDijkstra].
     * If you prefer a different solving mechanic you can provide it here. Keep in mind that edges can be weighted, so that
     * implementations like AStar might fail on Portals or Highways.
     * @param solver A new solver instance
     * @return this Route instance.
     */
    fun withSolver(solver: PathSolver<Node, Double>): Route

    /**
     * Adds an object to the route. All added objects will be visited in insertion order when calculating a shortest path.
     * @param route A route object to embed. Its variation will be picked so that the upmost Route will provide the shortest path.
     * @return this Route instance.
     */
    fun to(route: Route): Route

    /**
     * Adds an object to the route. All added objects will be visited in insertion order when calculating a shortest path.
     * @param nodes A list of nodes. They will be treated as path segment as is.
     * @return this Route instance.
     */
    fun to(nodes: List<Node>): Route

    /**
     * Adds an object to the route. All added objects will be visited in insertion order when calculating a shortest path.
     * @param node A node to visit.
     * @return this Route instance.
     */
    fun to(node: Node): Route

    /**
     * Adds an object to the route. All added objects will be visited in insertion order when calculating a shortest path.
     * @param location A location to visit.
     * @return this Route instance.
     */
    fun to(location: NavigationLocation): Route

    /**
     * Adds a collection of objects to the route. At least one of these objects must be visited when finding the shortest path.
     * @param nodes A collection of objects of which one must be visited in the solve process.
     * @return this Route instance.
     */
    fun toAny(vararg nodes: Node): Route

    /**
     * Adds a collection of objects to the route. At least one of these objects must be visited when finding the shortest path.
     * @param nodes A collection of objects of which one must be visited in the solve process.
     * @return this Route instance.
     */
    fun toAny(nodes: Collection<Node>): Route

    /**
     * Adds a collection of objects to the route. At least one of these objects must be visited when finding the shortest path.
     * @param locations A collection of objects of which one must be visited in the solve process.
     * @return this Route instance.
     */
    fun toAny(vararg locations: NavigationLocation): Route

    /**
     * Adds a collection of objects to the route. At least one of these objects must be visited when finding the shortest path.
     * @param routes A collection of objects of which one must be visited in the solve process.
     * @return this Route instance.
     */
    fun toAny(vararg routes: Route): Route

    /**
     * Uses an environment graph to find the shortest path of the start point of this route to any of the target nodes of the
     * route. All route points are either Routes or NavigationLocations. NavigationLocations connect themselves to the graph
     * if not concluded. This operation will not modify the input graph but run on a copy.
     *
     * @param environment A graph of Nodes and weighted Edges to navigate on.
     * @return The shortest route from the start point to any of the target points.
     * @throws NoPathFoundException If no path was found.
     */
    @Throws(NoPathFoundException::class)
    fun calculatePath(environment: ValueGraph<Node, Double>): PathSolverResult<Node, Double>

    /**
     * Uses an environment graph to find the shortest path of the start point of this route to all target nodes of the
     * route. All route points are either Routes or NavigationLocations. NavigationLocations connect themselves to the graph
     * if not concluded. This operation will not modify the input graph but run on a copy.
     *
     * @param environment A graph of Nodes and weighted Edges to navigate on.
     * @return The shortest path from the start node to each target node. The list is sorted and the first result is the shortest among all.
     * @throws NoPathFoundException If no path was found.
     */
    @Throws(NoPathFoundException::class)
    fun calculatePaths(environment: ValueGraph<Node, Double>): List<PathSolverResult<Node, Double>>

    companion object {
        /**
         * Creates a new route object with the provided location as start point.
         * @param location A NavigateLocation to describe the start point.
         * @return A new Route. Use any "to"-method to describe it further.
         */
        fun from(location: NavigationLocation): Route {
            return RouteImpl(location)
        }

        /**
         * Creates a new route object with the provided location as start point.
         * @param fixedGraphNode A Node that will be treated as a fixed Node that is already part of the solving graph.
         * To change this behaviour, either use [.from] or let the Node implement NavigateLocation.
         * @return A new Route. Use any "to"-method to describe it further.
         */
        fun from(fixedGraphNode: Node): Route {
            if (fixedGraphNode is NavigationLocation) {
                return from(fixedGraphNode as NavigationLocation)
            }
            return from(NavigationLocation.fixedGraphNode(fixedGraphNode))
        }

        /**
         * Creates a new route object with the provided location as start point.
         * @param route The other route will provide a start point.
         * @return A new Route. Use any "to"-method to describe it further.
         */
        fun from(route: Route): Route {
            return RouteImpl(route)
        }
    }
}
