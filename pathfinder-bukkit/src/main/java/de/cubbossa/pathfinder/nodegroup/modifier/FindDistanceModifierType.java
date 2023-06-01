package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.messages.Messages;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class FindDistanceModifierType implements ModifierType<FindDistanceModifier>,
    ModifierCommandExtension<FindDistanceModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:find-distance");

  @Override
  public String getSubCommandLiteral() {
    return "find-distance";
  }

  @Override
  public ComponentLike toComponents(FindDistanceModifier modifier) {
    return Messages.CMD_NG_MOD_FINDDIST.formatted(Messages.formatter().number("distance", modifier.distance()));
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<FindDistanceModifier, CommandExecutor> consumer) {
    return tree.then(new FloatArgument("find-distance", .1f).executes((commandSender, objects) -> {
      consumer.apply(new CommonFindDistanceModifier(objects.<Float>getUnchecked(1).doubleValue())).run(commandSender, objects);
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
      return new CommonFindDistanceModifier(f);
    }
    throw new IOException("Could not deserialize FindDistanceModifier, missing 'find-distance' attribute.");
  }
}
