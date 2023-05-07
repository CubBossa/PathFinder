package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class VisualizerModifierType implements ModifierType<VisualizerModifier>,
    ModifierCommandExtension<VisualizerModifier> {

  @Override
  public Class<VisualizerModifier> getModifierClass() {
    return VisualizerModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "visualizer";
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<VisualizerModifier, CommandExecutor> consumer) {
    return tree.then(CustomArgs.pathVisualizerArgument("visualizer").executes((commandSender, objects) -> {
      consumer.apply(new VisualizerModifier(objects.getUnchecked(1))).run(commandSender, objects);
    }));
  }

  @Override
  public Map<String, Object> serialize(VisualizerModifier modifier) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("visualizer-key", modifier.visualizer().getKey().toString());
    return map;
  }

  @Override
  public VisualizerModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("visualizer-key") && values.get("visualizer-key") instanceof String string) {
      NamespacedKey key = NamespacedKey.fromString(string);
      PathVisualizer<?, ?> visualizer = PathFinderProvider.get().getStorage().loadVisualizer(key).join().orElseThrow();
      return new VisualizerModifier(visualizer);
    }
    throw new IOException("Could not deserialize visualizer modifier, missing 'visualizer-key' attribute.");
  }
}
