package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.group.ModifierType;
import de.cubbossa.pathfinder.group.VisualizerModifier;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;
import org.pf4j.Extension;

@Extension(points = ModifierType.class)
public class VisualizerModifierType implements ModifierType<VisualizerModifier>,
    ModifierCommandExtension<VisualizerModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:visualizer");

  @Override
  public String getSubCommandLiteral() {
    return "visualizer";
  }

  @Override
  public ComponentLike toComponents(VisualizerModifier modifier) {
    return modifier.getVisualizer().join().map(visualizer -> Messages.CMD_NG_MOD_VIS
        .insertObject("visualizer", visualizer)
        .insertObject("vis", visualizer)
    ).orElse(Messages.CMD_NG_MOD_VIS);
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<VisualizerModifier, CommandExecutor> consumer) {
    return tree.then(Arguments.pathVisualizerArgument("visualizer").executes((commandSender, objects) -> {
      consumer.apply(new VisualizerModifierImpl(objects.<PathVisualizer<?, ?>>getUnchecked(1).getKey())).run(commandSender, objects);
    }));
  }

  @Override
  public Map<String, Object> serialize(VisualizerModifier modifier) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("visualizer-key", modifier.getVisualizerKey().toString());
    return map;
  }

  @Override
  public VisualizerModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("visualizer-key") && values.get("visualizer-key") instanceof String string) {
      NamespacedKey key = NamespacedKey.fromString(string);
      return new VisualizerModifierImpl(key);
    }
    throw new IOException("Could not deserialize visualizer modifier, missing 'visualizer-key' attribute.");
  }
}
