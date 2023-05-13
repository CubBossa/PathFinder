package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class PermissionModifierType implements ModifierType<PermissionModifier>,
    ModifierCommandExtension<PermissionModifier> {

  @Override
  public Class<PermissionModifier> getModifierClass() {
    return PermissionModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "permission";
  }

    @Override
    public Argument<?> registerAddCommand(Argument<?> tree, Function<PermissionModifier, CommandExecutor> consumer) {
        return tree.then(new StringArgument("node").executes((commandSender, objects) -> {
            consumer.apply(new PermissionModifier(objects.getUnchecked(1))).run(commandSender, objects);
        }));
    }

  @Override
  public Map<String, Object> serialize(PermissionModifier modifier) {
      LinkedHashMap<String, Object> map = new LinkedHashMap<>();
      map.put("node", modifier.permission());
      return map;
  }

  @Override
  public PermissionModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("node") && values.get("node") instanceof String string) {
      return new PermissionModifier(string);
    }
    throw new IOException("Could not deserialize permission modifier, missing 'node' attribute.");
  }
}
