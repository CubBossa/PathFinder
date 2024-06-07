package de.cubbossa.pathfinder.visualizer

import de.cubbossa.disposables.Disposable
import de.cubbossa.pathfinder.misc.KeyedRegistry
import de.cubbossa.pathfinder.misc.NamespacedKey

interface VisualizerTypeRegistry : Disposable {

    var defaultType: VisualizerType<PathVisualizer<*, *>>

    fun <VisualizerT : PathVisualizer<*, *>> getType(typeKey: NamespacedKey): VisualizerType<VisualizerT>?

    fun <VisualizerT : PathVisualizer<*, *>> registerVisualizerType(type: VisualizerType<VisualizerT>)

    fun unregisterVisualizerType(type: VisualizerType<out PathVisualizer<*, *>>)

    val types: KeyedRegistry<VisualizerType<out PathVisualizer<*, *>>>
}
