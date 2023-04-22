package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathfinder.commands.CustomArgs;
import de.cubbossa.pathfinder.commands.ModifierCommandExtension;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
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
  public ArgumentTree registerAddCommand(ArgumentTree tree,
                                         Function<DiscoverableModifier, CommandExecutor> consumer) {
    return tree.then(CustomArgs.miniMessageArgument("name").executes((commandSender, objects) -> {
      consumer.apply(new DiscoverableModifier((String) objects[1])).run(commandSender, objects);
    }));
  }

  @Override
  public Map<String, Object> serialize(DiscoverableModifier modifier) {
    return new LinkedHashMapBuilder<String, Object>()
        .put("name-format", modifier.getNameFormat())
        .build();
  }

  @Override
  public DiscoverableModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("name-format") && values.get("name-format") instanceof String string) {
      return new DiscoverableModifier(string);
    }
    throw new IOException("Could not deserialize DiscoverableModifier, missing 'name-format' attribute.");
  }
}
