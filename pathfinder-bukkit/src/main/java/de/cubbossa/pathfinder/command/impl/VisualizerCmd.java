package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
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


    for (VisualizerType<? extends PathVisualizer<?, ?>> type : VisualizerTypeRegistryImpl.getInstance().getTypes()) {

      if (!(type instanceof VisualizerTypeCommandExtension cmdExt)) {
        continue;
      }

      LiteralArgument set = (LiteralArgument) new LiteralArgument("set")
          .withPermission(PathPerms.PERM_CMD_PV_MODIFY);

      set.then(Arguments.literal("permission")
          .then(new GreedyStringArgument("permission")
              .executes((commandSender, args) -> {
                if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {

                  // just what we do with internal visualizers, but we cannot use property objects here because
                  // we want this code to be abstract and to work with all kind of visualizers.

                  String old = visualizer.getPermission();
                  String perm = args.getUnchecked(1);
                  visualizer.setPermission(perm);

                  pathFinder.getStorage().saveVisualizer(visualizer).thenRun(() -> {
                    BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_SET_PROP.formatted(
                        Messages.formatter().namespacedKey("key", visualizer.getKey()),
                        Messages.formatter().namespacedKey("type", type.getKey()),
                        Placeholder.parsed("property", "permission"),
                        Messages.formatter().permission("old-value", old),
                        Messages.formatter().permission("value", perm)
                    ));
                  });
                }
              })));

      cmdExt.appendEditCommand(set, 0, 1);
      then(new LiteralArgument(type.getCommandName())
          .then(Arguments.pathVisualizerArgument("visualizer", type)
              .then(set)
              .then(Arguments.literal("info")
                  .withPermission(PathPerms.PERM_CMD_PV_INFO)
                  .executes((commandSender, objects) -> {
                    onInfo(commandSender, (PathVisualizer<?, ?>) objects.getUnchecked(0));
                  })
              )
          )
      );
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
          .resolver(Messages.formatter().namespacedKey("key", visualizer.getKey()))
          .resolver(Messages.formatter().namespacedKey("type", type.get().getKey()))
          .resolver(Messages.formatter().permission("permission", visualizer.getPermission()))
          .build());
      p.sendMessage(message);
    });
  }
}
