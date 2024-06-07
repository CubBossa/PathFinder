package de.cubbossa.pathfinder.storage

import com.google.common.util.concurrent.ThreadFactoryBuilder
import de.cubbossa.pathfinder.AbstractPathFinder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.event.EventDispatcher
import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.Range
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import de.cubbossa.pathfinder.node.NodeTypeRegistry
import de.cubbossa.pathfinder.nodegroup.modifier.CurveLengthModifierImpl
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifierImpl
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifierImpl
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl
import de.cubbossa.pathfinder.util.CollectionUtils
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import lombok.Getter
import lombok.Setter
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.*
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors

@Getter
@Setter
class StorageAdapterImpl(private val nodeTypeRegistry: NodeTypeRegistry) : StorageAdapter {
    private var ioExecutor: ExecutorService? = null
    override var cache: CacheLayer = CacheLayerImpl()
    override var eventDispatcher: EventDispatcher<*>? = null
    var logger: Logger? = null
    override lateinit var implementation: StorageImplementation

    override fun dispose() {
        shutdown()
    }

    private fun eventDispatcher(): EventDispatcher<*>? {
        return eventDispatcher
    }

    @Throws(Exception::class)
    override fun init() {
        val factory = ThreadFactoryBuilder().setNameFormat("pathfinder-io-%d").build()
        ioExecutor = implementation.service(factory)
        if (ioExecutor == null) {
            ioExecutor = Executors.newCachedThreadPool(factory)
        }
        implementation.init()
    }

    override fun shutdown() {
        for (cache in this.cache) {
            cache.invalidateAll()
        }
        implementation?.shutdown()
        if (ioExecutor != null) {
            ioExecutor!!.shutdown()
            try {
                if (!ioExecutor!!.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    ioExecutor!!.shutdownNow()
                }
            } catch (e: InterruptedException) {
                ioExecutor!!.shutdownNow()
            }
        }
    }

    override suspend fun createGlobalNodeGroup(defaultVisualizerType: VisualizerType<*>): NodeGroup {
        var group = loadGroup(AbstractPathFinder.globalGroupKey())
        if (group != null) {
            return group
        }

        val vis = loadVisualizer(AbstractPathFinder.defaultVisualizerKey())
            ?: createAndLoadVisualizer(
                defaultVisualizerType,
                AbstractPathFinder.defaultVisualizerKey()
            )

        group = createAndLoadGroup(AbstractPathFinder.globalGroupKey())
        group.let {
            group.weight = 0f
            group.addModifier(CurveLengthModifierImpl(3.0))
            group.addModifier(FindDistanceModifierImpl(1.5))
            group.addModifier(VisualizerModifierImpl(vis!!.key))
            saveGroup(group)
        }
        return group
    }

    // Node Type
    override suspend fun <N : Node> loadNodeType(node: UUID): NodeType<N>? {
        val cached = cache.nodeTypeCache.getType<N>(node)
        if (cached != null) {
            return cached
        }
        val typeMapping = implementation.loadNodeTypeMapping(mutableSetOf(node))
        val type = typeMapping[node] as NodeType<N>?
        type?.let { cache.nodeTypeCache.write(node, it) }
        return type
    }

    override suspend fun loadNodeTypes(nodes: Collection<UUID>): Map<UUID, NodeType<*>> {
        val map = cache.nodeTypeCache.getTypes(nodes)
        val result: MutableMap<UUID, NodeType<*>> = HashMap(map.present)

        if (map.absent.isEmpty()) {
            return result
        }
        val newResult = implementation.loadNodeTypeMapping(map.absent)
        result.putAll(newResult)
        newResult.forEach { (uuid: UUID, type: NodeType<*>) ->
            cache.nodeTypeCache.write(uuid, type)
        }
        return result
    }

    // Nodes
    override suspend fun <N : Node> createAndLoadNode(
        type: NodeType<N>,
        location: Location
    ): N? {
        val node = type.createAndLoadNode(NodeType.Context(UUID.randomUUID(), location))
        implementation.saveNodeTypeMapping(mapOf(Pair(node!!.nodeId, type)))
        cache.nodeTypeCache.write(node.nodeId, type)

        cache.nodeCache.write(node)
        eventDispatcher().let { e: EventDispatcher<*>? -> e!!.dispatchNodeCreate(node) }
        return node
    }

    override suspend fun <N : Node> loadNode(id: UUID): N? {
        val nodes = loadNodes(listOf(id))
        return nodes.stream().findAny().map { n -> n as N? }.orElseGet(null)
    }

    override suspend fun <N : Node> insertGlobalGroupAndSave(node: N): N {
        modifyGroup(AbstractPathFinder.globalGroupKey()) { group: NodeGroup ->
            group.add(node.nodeId)
        }
        return node
    }

    private fun insertEdges(nodes: Collection<Node>): Collection<Node> {
        val edges = implementation.loadEdgesFrom(
            nodes.stream().map(
                Node::nodeId
            ).toList()
        )
        for (node in nodes) {
            if (!edges.containsKey(node.nodeId)) {
                continue
            }
            val toAdd = edges[node.nodeId]
            if (node.edges is MutableCollection && toAdd != null) {
                (node.edges as MutableCollection<Edge>).addAll(toAdd)
            }
            node.edgeChanges.flush()
        }
        return nodes
    }

    private fun prepareLoadedNode(node: Collection<Node>): Collection<Node> {
        return insertEdges(node)
    }

    override suspend fun <N : Node> loadNode(type: NodeType<N>, id: UUID): N? {
        val opt = cache.nodeCache.getNode<N>(id)
        if (opt != null) {
            return opt
        }
        val node = type.loadNode(id)
        node?.let {
            prepareLoadedNode(setOf(node))
        }
        return node
    }

    override suspend fun loadNodes(): Collection<Node> {
        val cached = cache.nodeCache.allNodes
        if (cached != null) {
            return cached
        }
        val nodes: Collection<Node> = nodeTypeRegistry.types.stream()
            .flatMap { nodeType: NodeType<*> -> nodeType.loadAllNodes().stream() }
            .collect(Collectors.toSet())
        cache.nodeCache.writeAll(nodes)
        prepareLoadedNode(nodes)
        return nodes
    }

    override suspend fun loadNodes(ids: Collection<UUID>): Collection<Node> {
        val col = cache.nodeCache.getNodes(ids)
        val result: MutableCollection<Node> = HashSet(col.present)
        if (col.absent.isEmpty()) {
            return result
        }
        val types = loadNodeTypes(col.absent)
        val revert: MutableMap<NodeType<*>, MutableCollection<UUID>> = HashMap()
        types.forEach { (uuid: UUID, nodeType: NodeType<*>) ->
            revert.computeIfAbsent(nodeType) { HashSet() }.add(uuid)
        }
        revert.forEach { (nodeType: NodeType<*>, uuids: Collection<UUID>) ->
            val nodes: Collection<Node> = HashSet(nodeType.loadNodes(uuids))
            nodes.forEach(Consumer { e: Node -> cache.nodeCache.write(e) })
            result.addAll(nodes)
        }
        return prepareLoadedNode(result)
    }

    override suspend fun saveNode(node: Node) {
        saveNodeTypeSafeBlocking(node)
    }

    private suspend fun <N : Node> saveNodeTypeSafeBlocking(node: N): N {
        val type: NodeType<N>? = loadNodeType(node.nodeId)
        type?.saveNode(node)
        return node
    }

    override suspend fun <N : Node> modifyNode(id: UUID, updater: suspend (N) -> Unit) {
        val node: N? = loadNode(id)
        node?.let {
            updater(it)
            saveNode(it)
        }
    }

    override suspend fun deleteNodes(uuids: Collection<UUID>) {
        val nodes = loadNodes(uuids)
        if (nodes.isEmpty()) {
            return
        }
        val types = loadNodeTypes(nodes.stream().map(Node::nodeId).toList())
        implementation.deleteNodeTypeMapping(uuids)
        deleteNode(nodes, types[nodes.stream().findAny().get().nodeId]!! as NodeType<in Node>)
        implementation.deleteEdgesTo(uuids)

        uuids.forEach {
            cache.nodeCache.invalidate(it)
            cache.groupCache.invalidate(it)
            cache.nodeTypeCache.invalidate(it)
        }
        eventDispatcher()?.dispatchNodesDelete(nodes)
    }

    private fun deleteNode(nodes: Collection<Node>, type: NodeType<in Node>) {
        type.deleteNodes(nodes)
    }

    override suspend fun loadEdgesTo(nodes: Collection<UUID>): Map<UUID, Collection<Edge>> {
        if (nodes.isEmpty()) {
            return HashMap()
        }
        return implementation.loadEdgesTo(nodes)
    }

    // Groups
    override suspend fun createAndLoadGroup(key: NamespacedKey): NodeGroup {
        val group = implementation.createAndLoadGroup(key)
        group?.let {
            cache.groupCache.write(group)
        }
        return group!!
    }

    override suspend fun loadGroup(key: NamespacedKey): NodeGroup? {
        val group = cache.groupCache.getGroup(key)
        if (group != null) {
            return group
        }
        val loaded = implementation.loadGroup(key)
        loaded.let { cache.groupCache.write(it) }
        return loaded
    }

    override suspend fun loadGroups(ids: Collection<UUID>): Map<UUID, Collection<NodeGroup>> {
        val result: MutableMap<UUID, Collection<NodeGroup>> = HashMap()
        val toLoad: MutableCollection<UUID> = HashSet()
        for (uuid in ids) {
            val groups = cache.groupCache.getGroups(uuid)
            if (groups == null) {
                toLoad.add(uuid)
            } else {
                result[uuid] = groups
            }
        }
        if (toLoad.isNotEmpty()) {
            result.putAll(implementation.loadGroupsByNodes(toLoad))
            toLoad.forEach(Consumer { uuid: UUID -> result.computeIfAbsent(uuid) { HashSet() } })
        }
        result.forEach { (uuid: UUID, groups: Collection<NodeGroup>) ->
            cache.groupCache.write(uuid, groups)
        }
        return CollectionUtils.sort(result, ids)
    }

    override suspend fun loadGroupsOfNodes(ids: Collection<Node>): Map<Node, Collection<NodeGroup>> {
        val nodes: MutableMap<UUID, Node> = LinkedHashMap()
        ids.forEach(Consumer { node: Node -> nodes[node.nodeId] = node })
        val col = loadGroups(nodes.keys)
        val result: LinkedHashMap<Node, Collection<NodeGroup>> = LinkedHashMap()
        col.forEach { (uuid, groups) ->
            val node = nodes[uuid]
            if (node != null) {
                result[node] = groups
            }
        }
        return result
    }

    override suspend fun loadGroups(range: Range): Collection<NodeGroup> {
        val cached = cache.groupCache.getGroups(range)
        if (cached != null) {
            return cached
        }
        return implementation.loadGroups(range)
    }

    override suspend fun loadGroupsByMod(keys: Collection<NamespacedKey>): Collection<NodeGroup> {
        val cached = cache.groupCache.getGroups(keys)
        val result: MutableCollection<NodeGroup> = HashSet(cached.present)
        if (cached.absent.isEmpty()) {
            return result
        }
        val loaded = implementation.loadGroupsByMod(cached.absent)
        loaded.forEach(Consumer { e: NodeGroup -> cache.groupCache.write(e) })
        result.addAll(loaded)
        return result
    }

    override suspend fun loadGroups(node: UUID): Collection<NodeGroup> {
        val cached = cache.groupCache.getGroups(node)
        if (cached != null) {
            return cached
        }
        val loaded = implementation.loadGroupsByNode(node)
        cache.groupCache.write(node, loaded)
        return loaded
    }

    override suspend fun loadGroups(modifier: NamespacedKey): Collection<NodeGroup> {
        val cached = cache.groupCache.getGroups(modifier)
        if (cached != null) {
            return cached
        }
        val loaded = implementation.loadGroups<Modifier>(modifier)
        cache.groupCache.write(modifier, loaded)
        return loaded
    }

    override suspend fun loadAllGroups(): Collection<NodeGroup> {
        val cached = cache.groupCache.groups
        if (cached != null) {
            return cached
        }
        val groups = implementation.loadAllGroups()
        cache.groupCache.writeAll(groups)
        return groups
    }

    override suspend fun saveGroup(group: NodeGroup) {
        val before = HashSet<UUID>()
        val mods = HashSet<Modifier>()
        synchronized(group) {
            before.addAll(group)
            before.addAll(group.contentChanges.removeList)
            mods.addAll(group.modifiers)
            mods.addAll(group.modifierChanges.removeList)
        }

        implementation.saveGroup(group)
        cache.nodeCache.write(group)
        cache.groupCache.write(group)
        for (uuid in before) {
            cache.groupCache.invalidate(uuid)
        }
        for (modifier in mods) {
            cache.groupCache.invalidate(modifier.key)
        }
        eventDispatcher()?.dispatchGroupSave(group)
    }

    override suspend fun modifyGroup(key: NamespacedKey, update: Consumer<NodeGroup>) {
        loadGroup(key)?.let {
            update.accept(it)
            saveGroup(it)
        }
    }

    override suspend fun deleteGroup(group: NodeGroup) {
        implementation.deleteGroup(group)
        cache.groupCache.invalidate(group)
    }

    // Find Data
    override suspend fun createAndLoadDiscoverInfo(
        player: UUID, key: NamespacedKey, time: LocalDateTime
    ): DiscoverInfo? {
        val info = implementation.createAndLoadDiscoverinfo(player, key, time)
        info?.let { cache.discoverInfoCache.write(it) }
        return info
    }

    override suspend fun loadDiscoverInfo(
        player: UUID,
        key: NamespacedKey
    ): DiscoverInfo? {
        val cached = cache.discoverInfoCache.getDiscovery(player, key)
        if (cached != null) {
            return cached
        }
        val info = implementation.loadDiscoverInfo(player, key)
        info?.let { cache.discoverInfoCache.write(it) }
        return info
    }

    override suspend fun deleteDiscoverInfo(info: DiscoverInfo) {
        implementation.deleteDiscoverInfo(info)
        cache.discoverInfoCache.invalidate(info)
    }

    override suspend fun <VisualizerT : PathVisualizer<*, *>> loadVisualizerType(
        key: NamespacedKey
    ): VisualizerType<VisualizerT>? {
        val cached = cache.visualizerTypeCache.getType<VisualizerT>(key)
        if (cached != null) {
            return cached
        }
        val types = implementation.loadVisualizerTypeMapping(mutableSetOf(key))
        val loaded = types[key] as VisualizerType<VisualizerT>?
        loaded?.let {
            cache.visualizerTypeCache.write(key, it)
        }
        return loaded
    }

    override suspend fun loadVisualizerTypes(keys: Collection<NamespacedKey>): Map<NamespacedKey, VisualizerType<*>> {
        val map = cache.visualizerTypeCache.getTypes(keys)
        val result = HashMap(map.present)
        if (map.absent.isEmpty()) {
            return result
        }
        val loaded = implementation.loadVisualizerTypeMapping(map.absent)
        result.putAll(loaded)
        loaded.entries.forEach(Consumer<Map.Entry<NamespacedKey, VisualizerType<*>>> { e: Map.Entry<NamespacedKey, VisualizerType<*>> ->
            cache.visualizerTypeCache.write(
                e
            )
        })
        return result
    }

    override suspend fun <VisualizerT : PathVisualizer<*, *>> saveVisualizerType(
        key: NamespacedKey, type: VisualizerType<VisualizerT>
    ) {
        implementation.saveVisualizerTypeMapping(mapOf(Pair(key, type)))
        cache.visualizerTypeCache.write(key, type)
    }

    // Visualizer
    override suspend fun <VisualizerT : PathVisualizer<*, *>> createAndLoadVisualizer(
        type: VisualizerType<VisualizerT>, key: NamespacedKey
    ): VisualizerT? {
        saveVisualizerType(key, type)
        val visualizer = type.createAndSaveVisualizer(key)
        cache.visualizerCache.write(visualizer)
        return visualizer
    }

    override suspend fun loadVisualizers(): Collection<PathVisualizer<*, *>> {
        val result: MutableCollection<PathVisualizer<*, *>> = HashSet()
        for (value in PathFinder.get().visualizerTypeRegistry.types.values) {
            result.addAll(this.loadVisualizers(value))
        }
        return result
    }

    override suspend fun <VisualizerT : PathVisualizer<*, *>> loadVisualizers(
        type: VisualizerType<VisualizerT>
    ): Collection<VisualizerT> {

        val cached = cache.visualizerCache.getVisualizers(type)
        if (cached != null) {
            return cached;
        }
        val visualizers: Collection<VisualizerT> = type.loadVisualizers().values
        cache.visualizerCache.writeAll(type, visualizers)
        return visualizers
    }

    override suspend fun <VisualizerT : PathVisualizer<*, *>> loadVisualizer(key: NamespacedKey): VisualizerT? {
        val cached = cache.visualizerCache.getVisualizer<VisualizerT>(key)
        if (cached != null) {
            return cached
        }
        val type = this.loadVisualizerType<VisualizerT>(key)
        return type?.let {
            val loaded = type.loadVisualizer(key)
            loaded?.let { cache.visualizerCache.write(it) }
            return loaded
        }
    }

    override suspend fun saveVisualizer(visualizer: PathVisualizer<*, *>) {
        val type = loadVisualizerType<PathVisualizer<*, *>>(visualizer.key)
        type?.let {
            it.saveVisualizer(visualizer)
            cache.visualizerCache.write(visualizer)
        }
    }

    override suspend fun deleteVisualizer(visualizer: PathVisualizer<*, *>) {
        val type = loadVisualizerType<PathVisualizer<*, *>>(visualizer.key)
        type?.let {
            type.deleteVisualizer(visualizer)
            cache.visualizerCache.invalidate(visualizer)
            implementation.deleteVisualizerTypeMapping(mutableSetOf(visualizer.key))
        }
    }

    override suspend fun <M : Modifier> loadNodes(modifier: NamespacedKey): Map<Node, Collection<M>> {
        val groups = loadGroups(modifier)
        val nodes =
            loadNodes(groups.stream().flatMap { obj: NodeGroup -> obj.stream() }.toList())
        val nodeMap: MutableMap<UUID, Node> = HashMap()
        nodes.forEach(Consumer { node: Node -> nodeMap[node.nodeId] = node })

        val results: MutableMap<Node, MutableCollection<M>> = HashMap()
        for (group in groups) {
            for (id in group) {
                val node = nodeMap[id]
                if (node == null) {
                    PathFinder.get().logger.log(
                        Level.WARNING,
                        "Node unexpectedly null for id $id."
                    )
                    continue
                }
                group.getModifier<M>(modifier)?.let {
                    results.computeIfAbsent(node) { ArrayList() }.add(it)
                }
            }
        }
        return results
    }
}
