package de.cubbossa.pathfinder.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import de.cubbossa.pathfinder.command.util.CommandUtils
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.command.CommandSender

class CommandArgument<S, T : Argument<S>?>(private val argument: T) : Argument<S>(
    argument!!.nodeName, argument.rawType
) {
    var wiki: String? = null
        private set
    var description: String? = null
        private set
    private var optional = false

    @Throws(CommandSyntaxException::class)
    override fun <Source> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments
    ): S {
        return argument!!.parseArgument(cmdCtx, key, previousArgs)
    }

    fun withGeneratedHelp(): CommandArgument<S, T> {
        executes(CommandExecutor { sender: CommandSender?, args: CommandArguments? ->
            CommandUtils.sendHelp(sender, this)
        })
        return this
    }

    fun withGeneratedHelp(depth: Int): CommandArgument<S, T> {
        executes(CommandExecutor { sender: CommandSender?, args: CommandArguments? ->
            CommandUtils.sendHelp(sender, this, depth)
        })
        return this
    }

    fun withWiki(url: String?): CommandArgument<S, T> {
        this.wiki = url
        return this
    }

    fun withDescription(description: String?): CommandArgument<S, T> {
        this.description = description
        return this
    }

    fun displayAsOptional(): CommandArgument<S, T> {
        this.optional = true
        return this
    }

    override fun isOptional(): Boolean {
        return optional
    }

    override fun getPrimitiveType(): Class<S> {
        return argument!!.primitiveType
    }

    override fun getArgumentType(): CommandAPIArgumentType {
        return argument!!.argumentType
    }

    companion object {
        fun <S, T : Argument<S>> arg(argument: T): CommandArgument<S, T> {
            return CommandArgument(argument)
        }
    }
}
