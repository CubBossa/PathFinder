package de.cubbossa.pathfinder.storage

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType
import java.util.*

interface InternalVisualizerStorageImplementation {

    fun <VisualizerT : AbstractVisualizer<*, *>> loadInternalVisualizer(
        type: AbstractVisualizerType<VisualizerT>,
        key: NamespacedKey
    ): VisualizerT?

    fun <VisualizerT : AbstractVisualizer<*, *>> loadInternalVisualizers(
        type: AbstractVisualizerType<VisualizerT>
    ): Map<NamespacedKey, VisualizerT>

    fun <VisualizerT : AbstractVisualizer<*, *>> saveInternalVisualizer(
        type: AbstractVisualizerType<VisualizerT>,
        visualizer: VisualizerT
    )

    fun <VisualizerT : AbstractVisualizer<*, *>> deleteInternalVisualizer(
        type: AbstractVisualizerType<VisualizerT>,
        visualizer: VisualizerT
    )
}
