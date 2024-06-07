package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.PathFinder
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.executors.CommandExecutor

open class PathFinderSubCommand(val pathfinder: PathFinder, commandName: String) :
    LiteralArgument(commandName) {

    fun withGeneratedHelp(): PathFinderSubCommand {
        executes(CommandExecutor { _, _ -> })
        return this
    }

    fun withGeneratedHelp(depth: Int): PathFinderSubCommand {
        executes(CommandExecutor { _, _ -> })
        return this
    }
}
