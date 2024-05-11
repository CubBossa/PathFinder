package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.ModifierType;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.pf4j.Extension;

@Extension(points = ModifierType.class)
public class DiscoverableModifierType implements ModifierType<DiscoverableModifier>,
    ModifierCommandExtension<DiscoverableModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:discoverable");

  @Override
  public String getSubCommandLiteral() {
    return "discover-as";
  }

  @Override
  public ComponentLike toComponents(DiscoverableModifier modifier) {
    return Messages.CMD_NG_MOD_DISCOVER.formatted(Placeholder.component("name", modifier.getDisplayName()));
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<DiscoverableModifier, CommandExecutor> consumer) {
    return tree.then(Arguments.miniMessageArgument("name").executes((commandSender, objects) -> {
      consumer.apply(new DiscoverableModifierImpl(objects.getUnchecked(1))).run(commandSender, objects);
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
      return new DiscoverableModifierImpl(string);
    }
    throw new IOException(
        "Could not deserialize DiscoverableModifier, missing 'name-format' attribute.");
  }
}
