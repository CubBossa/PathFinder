package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class DiscoverableModifierType implements ModifierType<DiscoverableModifier>,
    ModifierCommandExtension<DiscoverableModifier> {

  @Override
  public Class<DiscoverableModifier> getModifierClass() {
    return DiscoverableModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "discoverable";
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<DiscoverableModifier, CommandExecutor> consumer) {
    return tree.then(CustomArgs.miniMessageArgument("name").executes((commandSender, objects) -> {
      consumer.apply(new SimpleDiscoverableModifier(objects.getUnchecked(1))).run(commandSender, objects);
    }));
  }

  @Override
  public Map<String, Object> serialize(DiscoverableModifier modifier) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("name-format", modifier.getNameFormat());
    return map;
  }

  @Override
  public DiscoverableModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("name-format") && values.get("name-format") instanceof String string) {
      return new SimpleDiscoverableModifier(string);
    }
    throw new IOException(
        "Could not deserialize DiscoverableModifier, missing 'name-format' attribute.");
  }
}
