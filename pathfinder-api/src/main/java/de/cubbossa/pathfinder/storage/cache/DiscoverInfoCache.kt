package de.cubbossa.pathfinder.storage.cache

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.storage.DiscoverInfo
import java.util.*

interface DiscoverInfoCache : StorageCache<DiscoverInfo> {
    fun getDiscovery(player: UUID, key: NamespacedKey): DiscoverInfo?

    fun getDiscovery(player: UUID): Collection<DiscoverInfo>?

    fun invalidate(player: UUID)
}
