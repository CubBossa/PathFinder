package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.group.Modifier
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.executors.CommandExecutor
import net.kyori.adventure.text.ComponentLike

interface ModifierCommandExtension<M : Modifier> {
    fun registerAddCommand(
        tree: Argument<*>,
        consumer: (M) -> CommandExecutor
    ): Argument<*>

    fun toComponents(modifier: M): ComponentLike
}
