package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import dev.jorel.commandapi.arguments.Argument;

public abstract class IntervalVisualizerType<T extends IntervalVisualizer<?>>
    extends AbstractVisualizerType<T>
    implements VisualizerTypeCommandExtension {


  public IntervalVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    return tree.then(subCommand("interval", CustomArgs.integer("ticks", 1), IntervalVisualizer.PROP_INTERVAL)
        .withPermission(PathPerms.PERM_CMD_PV_INTERVAL)
    );
  }
}
