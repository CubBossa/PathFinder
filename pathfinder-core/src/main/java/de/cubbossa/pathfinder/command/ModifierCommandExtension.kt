package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.group.Modifier;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.ComponentLike;

import java.util.function.Function;

public interface ModifierCommandExtension<M extends Modifier> {

  Argument<?> registerAddCommand(Argument<?> tree, Function<M, CommandExecutor> consumer);

  ComponentLike toComponents(M modifier);
}
