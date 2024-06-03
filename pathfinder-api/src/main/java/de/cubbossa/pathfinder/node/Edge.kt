package de.cubbossa.pathfinder.node

import java.util.*

/**
 * An edge that connects one node to another.
 * Edges are always directed, to create an undirected edge, create two edges instead.
 */
interface Edge {
    /**
     * @return The uuid of the start node of this edge
     */
    val start: UUID

    /**
     * @return The uuid of the end node of this edge
     */
    val end: UUID

    /**
     * @return The relative cost of this edge additionally to the actual edge length. The edge length is NOT
     * part of weight.
     */
    val weight: Float

    /**
     * @return The start node of this edge as CompletableFuture.
     */
    suspend fun resolveStart(): Node?

    /**
     * @return The end node of this edge as CompletableFuture.
     */
    suspend fun resolveEnd(): Node?
}
