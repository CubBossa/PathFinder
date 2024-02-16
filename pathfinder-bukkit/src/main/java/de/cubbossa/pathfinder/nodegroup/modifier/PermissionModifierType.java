package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.group.PermissionModifier;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.messages.Messages;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class PermissionModifierType implements ModifierType<PermissionModifier>,
    ModifierCommandExtension<PermissionModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:permission");

  @Override
  public String getSubCommandLiteral() {
    return "permission";
  }

  @Override
  public ComponentLike toComponents(PermissionModifier modifier) {
    return Messages.CMD_NG_MOD_PERM.insertString("permission", modifier.permission());
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<PermissionModifier, CommandExecutor> consumer) {
    return tree.then(new StringArgument("node").executes((commandSender, objects) -> {
      consumer.apply(new CommonPermissionModifier(objects.getUnchecked(1))).run(commandSender, objects);
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
      return new CommonPermissionModifier(string);
    }
    throw new IOException("Could not deserialize permission modifier, missing 'node' attribute.");
  }
}
