package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.executors.CommandExecutor;

import java.io.IOException;
import java.util.LinkedHashMap;
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
    public Argument<?> registerAddCommand(Argument<?> tree, Function<FindDistanceModifier, CommandExecutor> consumer) {
        return tree.then(new FloatArgument("find-distance", .1f).executes((commandSender, objects) -> {
            consumer.apply(new FindDistanceModifier(objects.getUnchecked(1))).run(commandSender, objects);
        }));
    }

  @Override
  public Map<String, Object> serialize(FindDistanceModifier modifier) {
      LinkedHashMap<String, Object> map = new LinkedHashMap<>();
      map.put("find-distance", modifier.distance());
      return map;
  }

  @Override
  public FindDistanceModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("find-distance") && values.get("find-distance") instanceof Double f) {
      return new FindDistanceModifier(f);
    }
    throw new IOException("Could not deserialize FindDistanceModifier, missing 'find-distance' attribute.");
  }
}
