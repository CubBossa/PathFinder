package de.cubbossa.pathfinder.storage.cache

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType

interface VisualizerTypeCache : StorageCache<Map.Entry<NamespacedKey, VisualizerType<*>>> {
    fun getTypes(key: Collection<NamespacedKey>): StorageCache.CacheMap<NamespacedKey, VisualizerType<*>>

    fun <VisualizerT : PathVisualizer<*, *>> getType(key: NamespacedKey): VisualizerType<VisualizerT>?

    fun <VisualizerT : PathVisualizer<*, *>> write(
        key: NamespacedKey,
        type: VisualizerType<VisualizerT>
    )
}
