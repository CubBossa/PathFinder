package de.cubbossa.pathfinder.storage

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.visualizer.PathVisualizer

interface VisualizerStorageImplementation<VisualizerT : PathVisualizer<*, *>> {
    fun loadVisualizers(): Map<NamespacedKey, VisualizerT>

    fun loadVisualizer(key: NamespacedKey): VisualizerT?

    fun saveVisualizer(visualizer: VisualizerT)

    fun deleteVisualizer(visualizer: VisualizerT)
}
