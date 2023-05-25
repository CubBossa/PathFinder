package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
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

    for (VisualizerType<? extends PathVisualizer<?, ?>> type : VisualizerTypeRegistryImpl.getInstance().getTypes()) {

      if (!(type instanceof VisualizerTypeCommandExtension cmdExt)) {
        continue;
      }

      LiteralArgument set = new LiteralArgument("set");

      set.then(CustomArgs.literal("permission")
          .withPermission(PathPerms.PERM_CMD_PV_SET_PERMISSION)
          .then(new GreedyStringArgument("permission")
              .executes((commandSender, args) -> {
                if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                  type.setProperty(BukkitUtils.wrap(commandSender), visualizer, args.getUnchecked(1), "permission",
                      true,
                      visualizer::getPermission, visualizer::setPermission,
                      Messages::formatPermission);
                }
              })));

      cmdExt.appendEditCommand(set, 0, 1);
      then(new LiteralArgument(type.getCommandName())
          .then(CustomArgs.pathVisualizerArgument("visualizer", type)
              .then(set)));
    }
  }

  public <T extends PathVisualizer<?, ?>> void onInfo(CommandSender sender, T visualizer) {
    PathPlayer<CommandSender> p = BukkitUtils.wrap(sender);
    getPathfinder().getStorage().loadVisualizerType(visualizer.getKey()).thenAccept(type -> {
      if (type.isEmpty()) {
        p.sendMessage(Messages.CMD_VIS_NO_TYPE_FOUND);
        return;
      }
      if (!(type.get() instanceof VisualizerTypeMessageExtension ext)) {
        p.sendMessage(Messages.CMD_VIS_NO_INFO);
        return;
      }

      Message message = ext.getInfoMessage(visualizer).formatted(TagResolver.builder()
          .tag("key", Messages.formatKey(visualizer.getKey()))
          .tag("type", Messages.formatKey(type.get().getKey()))
          .resolver(Placeholder.component("permission", Messages.formatPermission(visualizer.getPermission())))
          .build());
      p.sendMessage(message);
    });
  }
}
