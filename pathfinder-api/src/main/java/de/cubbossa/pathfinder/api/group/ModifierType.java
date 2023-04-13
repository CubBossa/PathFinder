package de.cubbossa.pathfinder.api.group;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
import java.util.Map;

public interface ModifierType<M extends Modifier> {

  Class<M> getModifierClass();

  String getSubCommandLiteral();

  ArgumentTree registerAddCommand(ArgumentTree tree, Function<M, CommandExecutor> consumer);

  Map<String, Object> serialize(M modifier);

  M deserialize(Map<String, Object> values) throws IOException;
}
