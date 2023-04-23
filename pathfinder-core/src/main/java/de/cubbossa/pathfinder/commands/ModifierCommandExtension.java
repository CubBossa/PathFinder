package de.cubbossa.pathfinder.commands;

import de.cubbossa.pathapi.group.Modifier;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.util.function.Function;

public interface ModifierCommandExtension<M extends Modifier> {

  ArgumentTree registerAddCommand(ArgumentTree tree, Function<M, CommandExecutor> consumer);

}
