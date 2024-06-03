package de.cubbossa.pathfinder.storage

import de.cubbossa.disposables.Disposable
import de.cubbossa.pathfinder.event.EventDispatcher
import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.Range
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Holds an instance of [StorageImplementation]. It manages caching and calls the implementation
 * methods where needed.
 */
interface StorageAdapter : Disposable {
    /**
     * Initializes the storage by initializing the underlying implementation and all caches.
     */
    @Throws(Exception::class)
    fun init()

    /**
     * Shuts down the storage by shutting down the underlying implementation and invalidating all caches.
     */
    fun shutdown()

    var eventDispatcher: EventDispatcher<*>?

    /**
     * @return The implementation instance.
     */
    val implementation: StorageImplementation?

    var cache: CacheLayer


    /**
     * Check if global group exists and if not create.
     * Global group will use default visualizer, so if default visualizer has been deleted, it will be recreated.
     */
    suspend fun createGlobalNodeGroup(defaultVisualizerType: VisualizerType<*>): NodeGroup?

    /**
     * Loads the node type for a node with given [UUID].
     *
     * @param node The [UUID]
     * @param <N>  The Node type.
     * @return The [NodeType] instance wrapped in [CompletableFuture].
    </N> */
    suspend fun <N : Node> loadNodeType(node: UUID): NodeType<N>?

    /**
     * Loads the node type for multiple nodes by their [UUID]s.
     *
     * @param nodes A set of [UUID]s to retrieve [NodeType]s for.
     * @return A map of all uuids with their found node types. If no type was found, it is not included
     * in the map. Therefore, the size of the return map must not be equal to the size of the input collection.
     */
    suspend fun loadNodeTypes(nodes: Collection<UUID>): Map<UUID, NodeType<*>?>

    // Nodes
    /**
     * Creates a [Node] of a given [NodeType] asynchronously.
     *
     * @param type     The [NodeType] instance that shall be used to create the node.
     * @param location The [Location] at which the new node shall be.
     * @param <N>      The Node type.
     * @return A node instance matching the type parameter wrapped in [CompletableFuture].
    </N> */
    suspend fun <N : Node> createAndLoadNode(
        type: NodeType<N>,
        location: Location
    ): N?

    /**
     * Loads, modifies and saves a node with given [UUID] asynchronously.
     *
     * @param id      The [UUID] of the node to edit.
     * @param updater A consumer that will be applied to the requested node once loaded.
     * @return A [CompletableFuture] indicating the completion of the process.
     */
    suspend fun <N : Node> modifyNode(id: UUID, updater: suspend (N) -> Unit)

    suspend fun <N : Node> loadNode(id: UUID): N?

    suspend fun <N : Node> insertGlobalGroupAndSave(node: N): N

    suspend fun <N : Node> loadNode(type: NodeType<N>, id: UUID): N?

    suspend fun loadNodes(): Collection<Node>

    suspend fun loadNodes(ids: Collection<UUID>): Collection<Node>

    suspend fun <M : Modifier> loadNodes(modifier: NamespacedKey): Map<Node, Collection<M>>

    suspend fun saveNode(node: Node)

    /**
     * Deletes a collection of nodes from storage asynchronously.
     * A call of this method must fire the according [NodeDeleteEvent].
     * After successfull completion, all given [Node]s, all according [Edge]s,
     * [NodeGroup]- and [NodeType] mappings must be deleted.
     *
     * @param uuids A collection of nodes to delete.
     * @return A [CompletableFuture] indicating the completion of the process.
     */
    suspend fun deleteNodes(uuids: Collection<UUID>)

    suspend fun loadEdgesTo(nodes: Collection<UUID>): Map<UUID, Collection<Edge?>?>?

    // Groups
    suspend fun createAndLoadGroup(key: NamespacedKey): NodeGroup?

    suspend fun loadGroup(key: NamespacedKey): NodeGroup?

    suspend fun loadGroups(ids: Collection<UUID>): Map<UUID, Collection<NodeGroup>>

    suspend fun loadGroupsOfNodes(ids: Collection<Node>): Map<Node, Collection<NodeGroup>>

    suspend fun loadGroups(range: Range): Collection<NodeGroup>

    suspend fun loadGroups(node: UUID): Collection<NodeGroup>

    suspend fun loadGroupsByMod(keys: Collection<NamespacedKey>): Collection<NodeGroup>

    suspend fun loadGroups(modifier: NamespacedKey): Collection<NodeGroup>

    suspend fun loadAllGroups(): Collection<NodeGroup>

    suspend fun saveGroup(group: NodeGroup)

    suspend fun deleteGroup(group: NodeGroup)

    suspend fun modifyGroup(key: NamespacedKey, update: Consumer<NodeGroup>)

    // Find Data
    suspend fun createAndLoadDiscoverInfo(
        player: UUID, key: NamespacedKey, time: LocalDateTime
    ): DiscoverInfo?

    suspend fun loadDiscoverInfo(
        player: UUID, key: NamespacedKey
    ): DiscoverInfo?

    suspend fun deleteDiscoverInfo(info: DiscoverInfo)

    // Visualizer
    suspend fun <VisualizerT : PathVisualizer<*, *>> loadVisualizerType(
        key: NamespacedKey
    ): VisualizerType<VisualizerT>?

    suspend fun loadVisualizerTypes(
        keys: Collection<NamespacedKey>
    ): Map<NamespacedKey, VisualizerType<*>>

    suspend fun <VisualizerT : PathVisualizer<*, *>> saveVisualizerType(
        key: NamespacedKey, type: VisualizerType<VisualizerT>
    )

    suspend fun <VisualizerT : PathVisualizer<*, *>> createAndLoadVisualizer(
        type: VisualizerType<VisualizerT>, key: NamespacedKey
    ): VisualizerT?

    suspend fun loadVisualizers(): Collection<PathVisualizer<*, *>>

    suspend fun <VisualizerT : PathVisualizer<*, *>> loadVisualizers(
        type: VisualizerType<VisualizerT>
    ): Collection<VisualizerT>

    suspend fun <VisualizerT : PathVisualizer<*, *>> loadVisualizer(
        key: NamespacedKey
    ): VisualizerT?

    suspend fun saveVisualizer(visualizer: PathVisualizer<*, *>)

    suspend fun deleteVisualizer(visualizer: PathVisualizer<*, *>)
}
