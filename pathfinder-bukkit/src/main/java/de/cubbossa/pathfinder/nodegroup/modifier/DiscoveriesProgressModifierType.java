package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.group.DiscoverProgressModifier;
import de.cubbossa.pathfinder.group.ModifierType;
import de.cubbossa.pathfinder.group.NodeGroup;
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
public class DiscoveriesProgressModifierType implements ModifierType<DiscoverProgressModifier>,
    ModifierCommandExtension<DiscoverProgressModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:discover-progress");

  @Override
  public String getSubCommandLiteral() {
    return "discover-progress";
  }

  @Override
  public ComponentLike toComponents(DiscoverProgressModifier modifier) {
    return Messages.CMD_NG_MOD_DISCOVERIES.formatted(
        Placeholder.component("name", modifier.getDisplayName())
    );
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<DiscoverProgressModifier, CommandExecutor> consumer) {
    return tree
        .then(Arguments.miniMessageArgument("name")
            .executes((commandSender, objects) -> {
              consumer.apply(new DiscoverProgressModifierImpl(
                  objects.<NodeGroup>getUnchecked(0).getKey(),
                  objects.getUnchecked(1)
              )).run(commandSender, objects);
            })
        );
  }

  @Override
  public Map<String, Object> serialize(DiscoverProgressModifier modifier) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("group-key", modifier.getOwningGroup().toString());
    map.put("name-format", modifier.getNameFormat());
    return map;
  }

  @Override
  public DiscoverProgressModifier deserialize(Map<String, Object> values) throws IOException {
    try {
      String group = (String) values.get("group-key");
      String name = (String) values.get("name-format");
      return new DiscoverProgressModifierImpl(NamespacedKey.fromString(group), name);
    } catch (Throwable t) {
      throw new IOException("Could not deserialize DiscoverProgressModifier.", t);
    }
  }
}
