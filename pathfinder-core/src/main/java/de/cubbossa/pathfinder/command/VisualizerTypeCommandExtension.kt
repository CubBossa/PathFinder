package de.cubbossa.pathfinder.command

import dev.jorel.commandapi.arguments.Argument

interface VisualizerTypeCommandExtension {
    fun appendEditCommand(tree: Argument<*>, visualizerIndex: Int, argumentOffset: Int): Argument<*>
}
