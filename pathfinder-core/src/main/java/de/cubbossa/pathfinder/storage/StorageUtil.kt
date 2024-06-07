package de.cubbossa.pathfinder.storage

import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.node.Node
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import java.util.*

suspend fun StorageAdapter.getGroups(node: Node): Collection<NodeGroup> {
    return getGroups(node.nodeId)
}

suspend fun StorageAdapter.getGroups(node: UUID): Collection<NodeGroup> {
    return loadGroups(node)
}

suspend fun StorageAdapter.addGroups(group: NodeGroup, node: UUID) {
    return addGroups(setOf(group), setOf(node))
}

suspend fun StorageAdapter.addGroups(group: NodeGroup, nodes: Collection<UUID>) {
    return addGroups(setOf(group), nodes)
}

suspend fun StorageAdapter.addGroups(groups: Collection<NodeGroup>, node: UUID) {
    return addGroups(groups, setOf(node))
}

suspend fun StorageAdapter.addGroups(
    groups: Collection<NodeGroup>,
    nodes: Collection<UUID>
) {
    launchIO {
        val jobs = ArrayList<Job>()
        for (group in groups) {
            jobs.add(async {
                group.addAll(nodes)
                saveGroup(group)
            })
        }
        jobs.joinAll()
    }
}

suspend fun StorageAdapter.removeGroups(group: NodeGroup, node: UUID) {
    return removeGroups(setOf(group), setOf(node))
}

suspend fun StorageAdapter.removeGroups(group: NodeGroup, nodes: Collection<UUID>) {
    return removeGroups(setOf(group), nodes)
}

suspend fun StorageAdapter.removeGroups(groups: Collection<NodeGroup>, node: UUID) {
    return removeGroups(groups, setOf(node))
}

suspend fun StorageAdapter.removeGroups(
    groups: Collection<NodeGroup>,
    nodes: Collection<UUID>
) {
    launchIO {
        val jobs = ArrayList<Job>()
        for (group in groups) {
            group.removeAll(nodes.toSet())
            jobs.add(async {
                saveGroup(group)
            })
        }
        jobs.joinAll()
    }
}

suspend fun StorageAdapter.clearGroups(vararg nodes: UUID) =
    launchIO {
        val jobs = HashSet<Job>()
        val groups = HashSet<NodeGroup>()
        for (node in nodes) {
            jobs.add(async {
                groups.addAll(loadGroups(node))
            })
        }
        jobs.joinAll()
        jobs.clear()
        groups.forEach {
            it.removeAll(nodes.toSet())
            jobs.add(async { saveGroup(it) })
        }
        jobs.joinAll()
    }
