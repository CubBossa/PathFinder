package de.cubbossa.pathfinder.node

import de.cubbossa.pathfinder.group.NodeGroup

interface GroupedNode : Node {
    fun node(): Node

    fun groups(): MutableCollection<NodeGroup>

    fun merge(other: GroupedNode): GroupedNode
}
