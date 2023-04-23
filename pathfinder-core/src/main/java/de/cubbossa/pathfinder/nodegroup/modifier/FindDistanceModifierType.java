package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathfinder.commands.ModifierCommandExtension;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class FindDistanceModifierType implements ModifierType<FindDistanceModifier>,
    ModifierCommandExtension<FindDistanceModifier> {

  @Override
  public Class<FindDistanceModifier> getModifierClass() {
    return FindDistanceModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "find-distance";
  }

  @Override
  public ArgumentTree registerAddCommand(ArgumentTree tree,
                                         Function<FindDistanceModifier, CommandExecutor> consumer) {
    return tree.then(new FloatArgument("find-distance", .1f).executes((commandSender, objects) -> {
      consumer.apply(new FindDistanceModifier((Float) objects[1])).run(commandSender, objects);
    }));
  }

  @Override
  public Map<String, Object> serialize(FindDistanceModifier modifier) {
    return new LinkedHashMapBuilder<String, Object>()
        .put("find-distance", modifier.distance())
        .build();
  }

  @Override
  public FindDistanceModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("find-distance") && values.get("find-distance") instanceof Float f) {
      return new FindDistanceModifier(f);
    }
    throw new IOException(
        "Could not deserialize FindDistanceModifier, missing 'find-distance' attribute.");
  }
}
