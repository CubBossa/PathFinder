package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public class VisualizerCmd extends PathFinderSubCommand {

  public VisualizerCmd(PathFinder pathFinder) {
    super(pathFinder, "visualizer");
    withGeneratedHelp();

    then(CustomArgs.literal("info")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PV_INFO)
        .then(CustomArgs.pathVisualizerArgument("visualizer")
            .executes((commandSender, objects) -> {
              onInfo(commandSender, (PathVisualizer<?, ?>) objects.getUnchecked(0));
            })));

    for (VisualizerType<? extends PathVisualizer<?, ?>> type : VisualizerHandler.getInstance().getTypes()) {

      if (!(type instanceof VisualizerTypeCommandExtension cmdExt)) {
        continue;
      }

      LiteralArgument set = new LiteralArgument("set");

      set.then(CustomArgs.literal("name")
          .withPermission(PathPerms.PERM_CMD_PV_SET_NAME)
          .then(CustomArgs.miniMessageArgument("name")
              .executes((commandSender, args) -> {
                if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(BukkitUtils.wrap(commandSender), visualizer, args.getUnchecked(1), "name", true,
                          visualizer::getNameFormat, visualizer::setNameFormat);
                }
              })));
      set.then(CustomArgs.literal("permission")
          .withPermission(PathPerms.PERM_CMD_PV_SET_PERMISSION)
          .then(new GreedyStringArgument("permission")
              .executes((commandSender, args) -> {
                if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(BukkitUtils.wrap(commandSender), visualizer, args.getUnchecked(1), "permission",
                          true,
                          visualizer::getPermission, visualizer::setPermission,
                          Messages::formatPermission);
                }
              })));
      set.then(CustomArgs.literal("interval")
          .withPermission(PathPerms.PERM_CMD_PV_INTERVAL)
          .then(CustomArgs.integer("ticks", 1)
              .executes((commandSender, args) -> {
                if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(BukkitUtils.wrap(commandSender), visualizer, args.getUnchecked(1), "interval",
                          true,
                          visualizer::getInterval, visualizer::setInterval, Formatter::number);
                }
              })));

      cmdExt.appendEditCommand(set, 0, 1);
      then(new LiteralArgument(type.getCommandName())
          .then(CustomArgs.pathVisualizerArgument("visualizer", type)
              .then(set)));
    }
  }

  public <T extends PathVisualizer<?, ?>> void onInfo(CommandSender sender,
                                                      T visualizer) {
    if (!(getPathfinder().getStorage().loadVisualizerType(visualizer.getKey())
        .join().orElseThrow() instanceof VisualizerTypeMessageExtension<?> msgExt)) {
      return;
    }
    // can be safely assumed, the type was extracted from the visualizer
    VisualizerTypeMessageExtension<T> cast = (VisualizerTypeMessageExtension<T>) msgExt;

    Message message = cast.getInfoMessage(visualizer).formatted(TagResolver.builder()
        .tag("key", Messages.formatKey(visualizer.getKey()))
        .resolver(Placeholder.component("name", visualizer.getDisplayName()))
        .resolver(
            Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
        .resolver(Placeholder.component("type", Component.text(visualizer.getNameFormat())))
        .resolver(Placeholder.component("permission",
            Messages.formatPermission(visualizer.getPermission())))
        .resolver(Placeholder.component("interval", Component.text(visualizer.getInterval())))
        .build());

    BukkitUtils.wrap(sender).sendMessage(message);
  }
}
