package de.cubbossa.pathfinder.storage.cache

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import java.util.*

interface VisualizerCache : StorageCache<PathVisualizer<*, *>> {
    fun <VisualizerT : PathVisualizer<*, *>> getVisualizer(key: NamespacedKey): VisualizerT?

    val visualizers: Optional<Collection<PathVisualizer<*, *>>>

    fun <VisualizerT : PathVisualizer<*, *>> getVisualizers(type: VisualizerType<VisualizerT>): Collection<VisualizerT>?

    fun <VisualizerT : PathVisualizer<*, *>> writeAll(
        type: VisualizerType<VisualizerT>,
        v: Collection<VisualizerT>
    )

    fun writeAll(visualizers: Collection<PathVisualizer<*, *>>)
}
