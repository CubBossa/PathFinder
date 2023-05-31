package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.messages.Messages;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class DiscoverableModifierType implements ModifierType<DiscoverableModifier>,
    ModifierCommandExtension<DiscoverableModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:discoverable");

  @Override
  public String getSubCommandLiteral() {
    return "discoverable";
  }

  @Override
  public ComponentLike toComponents(DiscoverableModifier modifier) {
    return Messages.CMD_NG_MOD_DISCOVER.formatted(Placeholder.component("name", modifier.getDisplayName()));
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<DiscoverableModifier, CommandExecutor> consumer) {
    return tree.then(CustomArgs.miniMessageArgument("name").executes((commandSender, objects) -> {
      consumer.apply(new CommonDiscoverableModifier(objects.getUnchecked(1))).run(commandSender, objects);
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
      return new CommonDiscoverableModifier(string);
    }
    throw new IOException(
        "Could not deserialize DiscoverableModifier, missing 'name-format' attribute.");
  }
}
