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

public class CurveLengthModifierType implements ModifierType<CurveLengthModifier>,
    ModifierCommandExtension<CurveLengthModifier> {

  @Override
  public Class<CurveLengthModifier> getModifierClass() {
    return CurveLengthModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "curve-length";
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<CurveLengthModifier, CommandExecutor> consumer) {
    return tree.then(new FloatArgument("curve-length").executes((commandSender, objects) -> {
      consumer.apply(new CurveLengthModifier(objects.getUnchecked(1))).run(commandSender, objects);
    }));
  }

  @Override
  public Map<String, Object> serialize(CurveLengthModifier modifier) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("curve-length", modifier.curveLength());
    return map;
  }

  @Override
  public CurveLengthModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("curve-length") && values.get("curve-length") instanceof Float f) {
      return new CurveLengthModifier(f);
    }
    throw new IOException(
        "Could not deserialize CurveLengthModifier, missing 'curve-length' attribute.");
  }
}
