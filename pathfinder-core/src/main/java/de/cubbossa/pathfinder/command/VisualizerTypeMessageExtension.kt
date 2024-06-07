package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.translations.Message

interface VisualizerTypeMessageExtension<in VisualizerT : PathVisualizer<*, *>> {
    fun getInfoMessage(element: VisualizerT): Message?
}
