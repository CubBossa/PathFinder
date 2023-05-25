package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import dev.jorel.commandapi.arguments.Argument;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;

public abstract class IntervalVisualizerType<T extends IntervalVisualizerType.IntervalVisualizer>
    extends AbstractVisualizerType<T>
    implements VisualizerTypeCommandExtension<T> {


  public IntervalVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    return tree.then(CustomArgs.literal("interval")
        .withPermission(PathPerms.PERM_CMD_PV_INTERVAL)
        .then(CustomArgs.integer("ticks", 1)
            .executes((commandSender, args) -> {
              if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                type.setProperty(BukkitUtils.wrap(commandSender), visualizer, args.getUnchecked(1), "interval",
                    true, visualizer::getInterval, visualizer::setInterval, Formatter::number);
              }
            })));
  }

  public static abstract class IntervalVisualizer<D, P> extends AbstractVisualizer<D, P> {

    public IntervalVisualizer(NamespacedKey key, String nameFormat) {
      super(key, nameFormat);
    }
  }
}
