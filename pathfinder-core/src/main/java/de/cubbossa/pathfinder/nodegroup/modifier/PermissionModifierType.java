package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathfinder.commands.ModifierCommandExtension;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class PermissionModifierType implements ModifierType<PermissionModifier>,
    ModifierCommandExtension {

  @Override
  public Class<PermissionModifier> getModifierClass() {
    return PermissionModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "permission";
  }

  @Override
  public ArgumentTree registerAddCommand(ArgumentTree tree,
                                         Function<PermissionModifier, CommandExecutor> consumer) {
    return tree.then(new StringArgument("node").executes((commandSender, objects) -> {
      consumer.apply(new PermissionModifier((String) objects[1])).run(commandSender, objects);
    }));
  }

  @Override
  public Map<String, Object> serialize(PermissionModifier modifier) {
    return new LinkedHashMapBuilder<String, Object>()
        .put("node", modifier.permission())
        .build();
  }

  @Override
  public PermissionModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("node") && values.get("node") instanceof String string) {
      return new PermissionModifier(string);
    }
    throw new IOException("Could not deserialize permission modifier, missing 'node' attribute.");
  }
}
