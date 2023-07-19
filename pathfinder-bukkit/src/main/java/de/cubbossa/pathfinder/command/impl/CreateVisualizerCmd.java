package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.logging.Level;

public class CreateVisualizerCmd extends PathFinderSubCommand {
  public CreateVisualizerCmd(PathFinder pathFinder) {
    super(pathFinder, "createvisualizer");

    withGeneratedHelp();
    withPermission(PathPerms.PERM_CMD_PV_CREATE);
    then(Arguments.visualizerTypeArgument("type")
        .then(new StringArgument("key")
            .executes((commandSender, objects) -> {
              onCreate(commandSender,
                  objects.getUnchecked(0),
                  CommonPathFinder.pathfinder(objects.getUnchecked(1)));
            })
        )
    );
  }

  public void onCreate(CommandSender sender, VisualizerType<? extends PathVisualizer<?, ?>> type, NamespacedKey key) {

    Optional<?> opt = getPathfinder().getStorage().loadVisualizer(key).join();
    if (opt.isPresent()) {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_NAME_EXISTS);
      return;
    }
    getPathfinder().getStorage().createAndLoadVisualizer(type, key).thenAccept(visualizer -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_CREATE_SUCCESS.formatted(
          Messages.formatter().namespacedKey("key", visualizer.getKey()),
          Placeholder.component("type", Component.text(type.getCommandName()))
      ));
    }).exceptionally(throwable -> {
      getPathfinder().getLogger().log(Level.SEVERE, "Error while creating new visualizer", throwable);
      return null;
    });
  }
}
