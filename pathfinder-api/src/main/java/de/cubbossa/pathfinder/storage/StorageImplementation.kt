package de.cubbossa.pathfinder.storage

import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.Range
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import de.cubbossa.pathfinder.visualizer.VisualizerType
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadFactory
import java.util.logging.Logger

/**
 * A [StorageImplementation] handles the actual serializing and deserializing of the given objects.
 * To access pathfinder data, use an instance of [StorageAdapter] instead, which also handles caching and
 * combines different loading methods (e.g. loading a node, its edges and its groups) into one.
 */
interface StorageImplementation {
    fun service(factory: ThreadFactory?): ExecutorService? {
        return null
    }

    /**
     * Initializes this storage implementation. Will be called by [StorageAdapter.init] and will create
     * necessary files or objects. It assures that the instance can be used without issues afterward.
     *
     * @throws Exception Might call an exception. Not specified due to different implementations.
     */
    @Throws(Exception::class)
    fun init()

    fun shutdown()

    var logger: Logger

    fun setWorldLoader(worldLoader: WorldLoader?)

    // ################################
    // #   Node Types
    // ################################
    fun saveNodeTypeMapping(typeMapping: Map<UUID, NodeType<*>>)

    fun loadNodeTypeMapping(nodes: Collection<UUID>): Map<UUID, NodeType<out Node>>

    fun deleteNodeTypeMapping(nodes: Collection<UUID>)

    // ################################
    // #   Edges
    // ################################
    fun loadEdgesFrom(start: Collection<UUID>): Map<UUID, Collection<Edge>>

    fun loadEdgesTo(end: Collection<UUID>): Map<UUID, Collection<Edge>>

    fun deleteEdgesTo(end: Collection<UUID>)

    // ################################
    // #   Groups
    // ################################
    fun createAndLoadGroup(key: NamespacedKey): NodeGroup?

    fun loadGroupsByMod(key: Collection<NamespacedKey>): Collection<NodeGroup>

    fun loadGroup(key: NamespacedKey): NodeGroup? {
        return loadGroups(mutableSetOf(key)).stream().findAny().orElse(null)
    }

    fun loadGroups(keys: Collection<NamespacedKey>): Collection<NodeGroup>

    fun loadGroupsByNodes(ids: Collection<UUID>): Map<UUID, Collection<NodeGroup>>

    fun loadGroupsByNode(node: UUID): Collection<NodeGroup>

    fun loadGroups(range: Range): List<NodeGroup>

    fun <M : Modifier> loadGroups(modifier: NamespacedKey): Collection<NodeGroup>

    fun loadAllGroups(): Collection<NodeGroup>

    fun loadGroupNodes(group: NodeGroup): Collection<UUID>

    fun saveGroup(group: NodeGroup)

    fun deleteGroup(group: NodeGroup)

    // ################################
    // #   Find Data
    // ################################
    fun createAndLoadDiscoverinfo(
        player: UUID,
        key: NamespacedKey,
        time: LocalDateTime
    ): DiscoverInfo?

    fun loadDiscoverInfo(player: UUID, key: NamespacedKey): DiscoverInfo?

    fun deleteDiscoverInfo(info: DiscoverInfo?)

    // ################################
    // #   Visualizer Types
    // ################################
    fun saveVisualizerTypeMapping(types: Map<NamespacedKey, VisualizerType<*>>)

    fun loadVisualizerTypeMapping(keys: Collection<NamespacedKey>): Map<NamespacedKey, VisualizerType<*>>

    fun deleteVisualizerTypeMapping(keys: Collection<NamespacedKey>)
}
