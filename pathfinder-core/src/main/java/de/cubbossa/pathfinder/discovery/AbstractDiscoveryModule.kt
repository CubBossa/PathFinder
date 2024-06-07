package de.cubbossa.pathfinder.discovery

import de.cubbossa.disposables.Disposable
import de.cubbossa.pathfinder.AbstractPathFinder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderExtension
import de.cubbossa.pathfinder.PathFinderExtensionBase
import de.cubbossa.pathfinder.event.EventDispatcher
import de.cubbossa.pathfinder.group.*
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.navigation.NavigationModule.Companion.get
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifierImpl
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import kotlin.math.pow

open class AbstractDiscoveryModule<PlayerT>
    : PathFinderExtensionBase(), PathFinderExtension, Disposable {
    override val key: NamespacedKey = AbstractPathFinder.pathfinder("discovery")

    val pathFinder: PathFinder
    val eventDispatcher: EventDispatcher<PlayerT>

    init {
        instance = this
        this.pathFinder = PathFinder.get()
        pathFinder.disposer.register(this.pathFinder, this)
        this.eventDispatcher = pathFinder.eventDispatcher as EventDispatcher<PlayerT>
    }

    init {
        if (pathFinder.configuration.moduleConfig.isDiscoveryModule && pathFinder.configuration.navigation.isRequireDiscovery) {
            get<Any>().registerNavigationConstraint { playerId: UUID, scope: Collection<Node> ->
                runBlocking {
                    val map = PathFinder.get().storage.loadGroupsOfNodes(scope)
                    map.entries.stream()
                        .filter { e ->
                            e.value.stream().allMatch { group: NodeGroup ->
                                runBlocking {
                                    (!group.hasModifier(DiscoverableModifierImpl::class.java)
                                            || !hasDiscovered(playerId, group))
                                }
                            }
                        }
                        .map { e -> e.key }
                        .collect(Collectors.toSet())
                }
            }
        }
    }

    override fun dispose() {
        instance = null
    }

    suspend fun getFulfillingGroups(player: PathPlayer<*>): Collection<NodeGroup> {
        val storage = PathFinder.get().storage
        val groups = storage.loadGroups(DiscoverableModifier.KEY)
        val allNodes: Collection<UUID> = groups.stream()
            .flatMap { l -> l.stream() }
            .toList()
        if (allNodes.isEmpty()) {
            return HashSet()
        }
        val nodes = storage.loadNodes(allNodes)
        val nodeGroupMap = storage.loadGroups(allNodes)
        val nodeMap: MutableMap<UUID, Node> = HashMap()

        nodes.forEach { node -> nodeMap[node.nodeId] = node }

        return groups.stream()
            .filter { group ->
                val perm = group.getModifier<PermissionModifier>(PermissionModifier.key)
                perm == null || player.hasPermission(perm.permission)
            }
            .filter { group ->
                group.stream().anyMatch { uuid ->
                    val location = nodeMap[uuid]?.location ?: return@anyMatch false
                    if (player.location.world != location.world) {
                        return@anyMatch false
                    }
                    val dist = nodeGroupMap[uuid]?.let { getDiscoveryDistance(it) } ?: 0f
                    if (location.x - player.location.x > dist || location.y - player.location.y > dist) {
                        return@anyMatch false
                    }
                    !(location.distanceSquared(player.location) > dist.pow(2.0f))
                }
            }
            .collect(Collectors.toSet())
    }

    suspend fun discover(
        player: PathPlayer<PlayerT>,
        group: NodeGroup,
        date: LocalDateTime
    ) {
        if (!group.hasModifier<Modifier>(DiscoverableModifier.KEY)) {
            return
        }
        val playerId = player.uniqueId
        val info = pathFinder.storage.loadDiscoverInfo(playerId, group.key)
        if (info != null) {
            return
        }

        val discoverable = group.getModifier<DiscoverableModifier>(DiscoverableModifier.KEY)
        if (!eventDispatcher.dispatchPlayerFindEvent(player, group, discoverable, date)) {
            return
        }
        pathFinder.storage.createAndLoadDiscoverInfo(playerId, group.key, date)
    }

    suspend fun forget(player: PathPlayer<PlayerT>, group: NodeGroup) {
        if (!group.hasModifier(DiscoverableModifierImpl::class.java)) {
            return
        }

        val info = pathFinder.storage.loadDiscoverInfo(player.uniqueId, group.key)
        if (!eventDispatcher.dispatchPlayerForgetEvent(player, info?.discoverable)) {
            return
        }
        info?.let {
            pathFinder.storage.deleteDiscoverInfo(it)
        }
    }

    suspend fun hasDiscovered(playerId: UUID, group: NodeGroup): Boolean {
        return pathFinder.storage.loadDiscoverInfo(playerId, group.key) == null
    }

    private fun getDiscoveryDistance(groups: Collection<NodeGroup>): Float {
        val mod = groups.stream()
            .filter { group: NodeGroup -> group.hasModifier<Modifier>(FindDistanceModifier.key) }
            .sorted()
            .findFirst()
            .map { group: NodeGroup ->
                group.getModifier<FindDistanceModifier>(FindDistanceModifier.key)
            }
            .orElse(null)
        return mod?.distance?.toFloat() ?: 1.5f
    }

    companion object {
        fun <T> getInstance(): AbstractDiscoveryModule<T>? {
            return instance as AbstractDiscoveryModule<T>?
        }

        private var instance: AbstractDiscoveryModule<*>? = null
    }
}
