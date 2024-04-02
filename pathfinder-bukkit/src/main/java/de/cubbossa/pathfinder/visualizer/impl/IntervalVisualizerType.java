package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import dev.jorel.commandapi.arguments.Argument;
import java.util.Map;

public abstract class IntervalVisualizerType<T extends IntervalVisualizer<?>>
    extends AbstractVisualizerType<T>
    implements VisualizerTypeCommandExtension {


  public IntervalVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    return tree.then(subCommand("interval", Arguments.integer("ticks", 1), IntervalVisualizer.PROP_INTERVAL)
        .withPermission(PathPerms.PERM_CMD_PV_INTERVAL)
    );
  }

  @Override
  public Map<String, Object> serialize(T visualizer) {
    Map<String, Object> map = super.serialize(visualizer);
    map.put("interval", visualizer.getInterval());
    return map;
  }

  @Override
  public void deserialize(T visualizer, Map<String, Object> values) {
    super.deserialize(visualizer, values);
    loadProperty(values, visualizer, IntervalVisualizer.PROP_INTERVAL);
  }
}
